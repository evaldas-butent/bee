package com.butent.bee.server.modules.finance;

import com.google.common.base.Stopwatch;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.FinanceUtils;
import com.butent.bee.shared.modules.finance.TradeAccounts;
import com.butent.bee.shared.modules.finance.TradeAccountsPrecedence;
import com.butent.bee.shared.modules.finance.TradeDimensionsPrecedence;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class FinancePostingBean {

  private static BeeLogger logger = LogUtils.getLogger(FinancePostingBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  DataEditorBean deb;

  public ResponseObject postTradeDocument(long docId) {
    Stopwatch stopwatch = Stopwatch.createStarted();

    BeeRowSet docData = qs.getViewDataById(VIEW_TRADE_DOCUMENTS, docId);

    if (DataUtils.isEmpty(docData)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.trdDocument(), docId, dictionary.nothingFound());
    }

    BeeRowSet docLines = qs.getViewData(VIEW_TRADE_DOCUMENT_ITEMS,
        Filter.and(Filter.equals(COL_TRADE_DOCUMENT, docId),
            Filter.notEquals(COL_TRADE_ITEM_QUANTITY, BeeConst.DOUBLE_ZERO)));

    if (DataUtils.isEmpty(docLines)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.trdDocumentItems(), docId,
          dictionary.nothingFound());
    }

    BeeRowSet config = qs.getViewData(VIEW_FINANCE_CONFIGURATION);
    if (DataUtils.isEmpty(config)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.dataNotAvailable(dictionary.finDefaultAccounts()));
    }

    BeeRowSet docPayments = qs.getViewData(VIEW_TRADE_PAYMENTS,
        Filter.and(Filter.equals(COL_TRADE_DOCUMENT, docId),
            Filter.or(Filter.notNull(COL_TRADE_PAYMENT_ACCOUNT),
                Filter.notNull(COL_TRADE_PAYMENT_TYPE_ACCOUNT)),
            Filter.notEquals(COL_TRADE_PAYMENT_AMOUNT, BeeConst.DOUBLE_ZERO)));

    int rowIndex = 0;
    TradeAccounts defaultAccounts = TradeAccounts.createAvailable(config, config.getRow(rowIndex));

    Long defaultJournal = config.getLong(rowIndex, COL_DEFAULT_JOURNAL);
    Long costOfMerchandise = config.getLong(rowIndex, COL_COST_OF_MERCHANDISE);

    List<TradeDimensionsPrecedence> dimensionsPrecedence = TradeDimensionsPrecedence
        .parse(config.getString(rowIndex, COL_TRADE_DIMENSIONS_PRECEDENCE));
    List<TradeAccountsPrecedence> accountsPrecedence =
        TradeAccountsPrecedence.parse(config.getString(rowIndex, COL_TRADE_ACCOUNTS_PRECEDENCE));

    rowIndex = 0;
    DateTime date = docData.getDateTime(rowIndex, COL_TRADE_DOCUMENT_RECEIVED_DATE);
    if (date == null) {
      date = docData.getDateTime(rowIndex, COL_TRADE_DATE);
    }

    String series = docData.getString(rowIndex, COL_SERIES);
    String number = docData.getString(rowIndex, COL_TRADE_NUMBER);
    if (BeeUtils.isEmpty(number)) {
      number = docData.getString(rowIndex, COL_TRADE_DOCUMENT_NUMBER_1);
    }
    if (BeeUtils.isEmpty(number)) {
      number = docData.getString(rowIndex, COL_TRADE_DOCUMENT_NUMBER_2);
    }

    Long operation = docData.getLong(rowIndex, COL_TRADE_OPERATION);

    OperationType operationType = docData.getEnum(rowIndex, COL_OPERATION_TYPE,
        OperationType.class);
    if (operationType == null) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.error(dictionary.valueEmpty(dictionary.trdOperationType()));
    }

    Long supplier = docData.getLong(rowIndex, COL_TRADE_SUPPLIER);
    Long customer = docData.getLong(rowIndex, COL_TRADE_CUSTOMER);

    Long currency = docData.getLong(rowIndex, COL_TRADE_CURRENCY);
    Long payer = docData.getLong(rowIndex, COL_TRADE_PAYER);

    Long company;
    if (DataUtils.isId(payer)) {
      company = payer;
    } else if (operationType.consumesStock()) {
      company = BeeUtils.nvl(customer, supplier);
    } else {
      company = BeeUtils.nvl(supplier, customer);
    }

    Long warehouseFrom = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_FROM);
    Long warehouseTo = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_TO);

    Long manager = docData.getLong(rowIndex, COL_TRADE_MANAGER);

    EnumMap<TradeDimensionsPrecedence, Dimensions> dimensions =
        new EnumMap<>(TradeDimensionsPrecedence.class);
    EnumMap<TradeAccountsPrecedence, TradeAccounts> accounts =
        new EnumMap<>(TradeAccountsPrecedence.class);

    dimensions.put(TradeDimensionsPrecedence.DOCUMENT,
        Dimensions.create(docData, docData.getRow(rowIndex)));
    accounts.put(TradeAccountsPrecedence.DOCUMENT,
        TradeAccounts.create(docData, docData.getRow(rowIndex)));

    dimensions.put(TradeDimensionsPrecedence.OPERATION,
        getDimensions(VIEW_TRADE_OPERATIONS, operation));
    accounts.put(TradeAccountsPrecedence.OPERATION,
        getTradeAccounts(VIEW_TRADE_OPERATIONS, operation));

    dimensions.put(TradeDimensionsPrecedence.COMPANY, getDimensions(VIEW_COMPANIES, company));
    accounts.put(TradeAccountsPrecedence.COMPANY, getTradeAccounts(VIEW_COMPANIES, company));

    Dimensions warehouseFromDimensions = getDimensions(VIEW_WAREHOUSES, warehouseFrom);
    TradeAccounts warehouseFromAccounts = getTradeAccounts(VIEW_WAREHOUSES, warehouseFrom);

    Dimensions warehouseToDimensions = getDimensions(VIEW_WAREHOUSES, warehouseTo);
    TradeAccounts warehouseToAccounts = getTradeAccounts(VIEW_WAREHOUSES, warehouseTo);

    int itemIndex = docLines.getColumnIndex(COL_ITEM);
    int isServiceIndex = docLines.getColumnIndex(COL_ITEM_IS_SERVICE);

    int quantityIndex = docLines.getColumnIndex(COL_TRADE_ITEM_QUANTITY);

    int warehouseIndex = docLines.getColumnIndex(COL_TRADE_ITEM_WAREHOUSE);
    int employeeIndex = docLines.getColumnIndex(COL_TRADE_ITEM_EMPLOYEE);

    int parentIndex = docLines.getColumnIndex(COL_TRADE_ITEM_PARENT);

    int costIndex = docLines.getColumnIndex(COL_TRADE_ITEM_COST);
    int costCurrencyIndex = docLines.getColumnIndex(ALS_COST_CURRENCY);

    int parentCostIndex = docLines.getColumnIndex(ALS_PARENT_COST);
    int parentCostCurrencyIndex = docLines.getColumnIndex(ALS_PARENT_COST_CURRENCY);

    TradeDocumentSums tdSums = TradeDocumentSums.of(docData, docLines);
    double docTotal = tdSums.getTotal();

    Map<Long, Long> itemCategoryTree = getItemCategoryTree();

    BeeRowSet buffer = new BeeRowSet(VIEW_FINANCIAL_RECORDS,
        sys.getView(VIEW_FINANCIAL_RECORDS).getRowSetColumns());

    for (BeeRow row : docLines) {
      Long item = row.getLong(itemIndex);
      boolean isService = BeeUtils.isTrue(row.getBoolean(isServiceIndex));

      Double quantity = row.getDouble(quantityIndex);

      Long employee = row.getLong(employeeIndex);
      if (!DataUtils.isId(employee)) {
        employee = manager;
      }

      Long parent = row.getLong(parentIndex);

      Double cost = row.getDouble(costIndex);
      Long costCurrency = row.getLong(costCurrencyIndex);

      Double parentCost = row.getDouble(parentCostIndex);
      Long parentCostCurrency = row.getLong(parentCostCurrencyIndex);

      Long lineWarehouse = row.getLong(warehouseIndex);
      Long parentWarehouse = getParentWarehouse(parent);

      dimensions.put(TradeDimensionsPrecedence.DOCUMENT_LINE, Dimensions.create(docLines, row));
      accounts.put(TradeAccountsPrecedence.DOCUMENT_LINE, TradeAccounts.create(docLines, row));

      dimensions.put(TradeDimensionsPrecedence.ITEM, getDimensions(VIEW_ITEMS, item));
      accounts.put(TradeAccountsPrecedence.ITEM, getTradeAccounts(VIEW_ITEMS, item));

      Long itemGroup = qs.getLongById(TBL_ITEMS, item, COL_ITEM_GROUP);
      Long itemType = qs.getLongById(TBL_ITEMS, item, COL_ITEM_TYPE);
      Set<Long> itemCategories = getItemCategories(item);

      dimensions.put(TradeDimensionsPrecedence.ITEM_GROUP,
          getItemCategoryDimensions(itemGroup, itemCategoryTree));
      accounts.put(TradeAccountsPrecedence.ITEM_GROUP,
          getItemCategoryTradeAccounts(itemGroup, itemCategoryTree));

      dimensions.put(TradeDimensionsPrecedence.ITEM_TYPE,
          getItemCategoryDimensions(itemType, itemCategoryTree));
      accounts.put(TradeAccountsPrecedence.ITEM_TYPE,
          getItemCategoryTradeAccounts(itemType, itemCategoryTree));

      dimensions.put(TradeDimensionsPrecedence.ITEM_CATEGORY,
          getItemCategoriesDimensions(itemCategories, itemCategoryTree));
      accounts.put(TradeAccountsPrecedence.ITEM_CATEGORY,
          getItemCategoriesTradeAccounts(itemCategories, itemCategoryTree));

      dimensions.put(TradeDimensionsPrecedence.WAREHOUSE,
          getWarehouseDimensions(operationType,
              warehouseFrom, warehouseFromDimensions, warehouseTo, warehouseToDimensions,
              lineWarehouse, parentWarehouse));
      accounts.put(TradeAccountsPrecedence.WAREHOUSE,
          getWarehouseTradeAccounts(operationType,
              warehouseFrom, warehouseFromAccounts, warehouseTo, warehouseToAccounts,
              lineWarehouse, parentWarehouse));

      Dimensions dim = computeTradeDimensions(dimensionsPrecedence, dimensions);
      TradeAccounts acc = computeTradeAccounts(accountsPrecedence, accounts, defaultAccounts);

      Double costAmount = BeeUtils.nonZero(cost) && DataUtils.isId(costCurrency)
          ? cost * quantity : null;
      Double parentCostAmount = DataUtils.isId(parent) && BeeUtils.nonZero(parentCost)
          && DataUtils.isId(parentCostCurrency) ? parentCost * quantity : null;

      double lineVat = tdSums.getItemVat(row.getId());
      double lineTotal = tdSums.getItemTotal(row.getId());

      if (BeeUtils.nonZero(lineTotal - lineVat)) {
        write(buffer, date, operationType.getAmountDebit(acc), operationType.getAmountCredit(acc),
            lineTotal - lineVat, currency, employee, dim);
      }

      if (BeeUtils.nonZero(parentCostAmount)) {
        write(buffer, date, acc.getCostOfGoodsSold(), costOfMerchandise,
            parentCostAmount, parentCostCurrency, employee, dim);
      }

      if (BeeUtils.nonZero(lineVat)) {
        write(buffer, date, operationType.getVatDebit(acc), operationType.getVatCredit(acc),
            lineVat, currency, employee, dim);
      }

      if (!DataUtils.isEmpty(docPayments)
          && BeeUtils.nonZero(lineTotal) && BeeUtils.nonZero(docTotal)) {

        postPayments(buffer, docPayments, operationType.consumesStock(),
            operationType.getDebtAccount(acc), lineTotal / docTotal, currency, employee, dim);
      }
    }

    BeeRowSet output = aggregate(buffer);

    updateJournal(output, defaultJournal);
    updateCompany(output, company);
    updateDocumentNumbers(output, series, number);
    updateContents(output, operation);

    ResponseObject response = commitTradeDocument(docId, output);
    stopwatch.stop();

    if (!response.hasErrors()) {
      logger.info(SVC_POST_TRADE_DOCUMENT, docId,
          buffer.getNumberOfRows(), output.getNumberOfRows(), response.getResponse(),
          BeeUtils.bracket(stopwatch.toString()));
    }

    return response;
  }

  private Dimensions getDimensions(String viewName, Long id) {
    if (DataUtils.isId(id)) {
      BeeRowSet rowSet = qs.getViewDataById(viewName, id);

      if (!DataUtils.isEmpty(rowSet)) {
        return Dimensions.create(rowSet, rowSet.getRow(0));
      }
    }
    return null;
  }

  private Set<Long> getItemCategories(Long item) {
    return qs.getDistinctLongs(TBL_ITEM_CATEGORIES, COL_CATEGORY,
        SqlUtils.equals(TBL_ITEM_CATEGORIES, COL_ITEM, item));
  }

  private Map<Long, Long> getItemCategoryTree() {
    Map<Long, Long> result = new HashMap<>();

    String idName = sys.getIdName(TBL_ITEM_CATEGORY_TREE);
    SqlSelect treeQuery = new SqlSelect()
        .addFields(TBL_ITEM_CATEGORY_TREE, idName, COL_CATEGORY_PARENT)
        .addFrom(TBL_ITEM_CATEGORY_TREE)
        .setWhere(SqlUtils.notNull(TBL_ITEM_CATEGORY_TREE, COL_CATEGORY_PARENT));

    SimpleRowSet treeData = qs.getData(treeQuery);
    if (!DataUtils.isEmpty(treeData)) {
      for (SimpleRowSet.SimpleRow row : treeData) {
        result.put(row.getLong(idName), row.getLong(COL_CATEGORY_PARENT));
      }
    }

    return result;
  }

  private Dimensions getItemCategoryDimensions(Long id, Map<Long, Long> parents) {
    if (!DataUtils.isId(id)) {
      return null;
    }

    Dimensions dimensions = getDimensions(VIEW_ITEM_CATEGORY_TREE, id);

    Long parent = parents.get(id);
    if (!DataUtils.isId(parent)) {
      return dimensions;
    }

    List<Dimensions> list = new ArrayList<>();
    list.add(dimensions);
    list.add(getItemCategoryDimensions(parent, parents));

    return Dimensions.merge(list);
  }

  private Dimensions getItemCategoriesDimensions(Collection<Long> ids, Map<Long, Long> parents) {
    if (BeeUtils.isEmpty(ids)) {
      return null;
    } else {
      return Dimensions.merge(ids.stream()
          .map(id -> getItemCategoryDimensions(id, parents))
          .collect(Collectors.toList()));
    }
  }

  private TradeAccounts getItemCategoryTradeAccounts(Long id, Map<Long, Long> parents) {
    if (!DataUtils.isId(id)) {
      return null;
    }

    TradeAccounts tradeAccounts = getTradeAccounts(VIEW_ITEM_CATEGORY_TREE, id);

    Long parent = parents.get(id);
    if (!DataUtils.isId(parent)) {
      return tradeAccounts;
    }

    List<TradeAccounts> list = new ArrayList<>();
    list.add(tradeAccounts);
    list.add(getItemCategoryTradeAccounts(parent, parents));

    return TradeAccounts.merge(list);
  }

  private TradeAccounts getItemCategoriesTradeAccounts(Collection<Long> ids,
      Map<Long, Long> parents) {

    if (BeeUtils.isEmpty(ids)) {
      return null;
    } else {
      return TradeAccounts.merge(ids.stream()
          .map(id -> getItemCategoryTradeAccounts(id, parents))
          .collect(Collectors.toList()));
    }
  }

  private TradeAccounts getTradeAccounts(String viewName, Long id) {
    if (DataUtils.isId(id)) {
      BeeRowSet rowSet = qs.getViewDataById(viewName, id);

      if (!DataUtils.isEmpty(rowSet)) {
        return TradeAccounts.create(rowSet, rowSet.getRow(0));
      }
    }
    return null;
  }

  private Long getParentWarehouse(Long parent) {
    if (DataUtils.isId(parent)) {
      return qs.getLong(TBL_TRADE_STOCK, COL_STOCK_WAREHOUSE, COL_TRADE_DOCUMENT_ITEM, parent);
    } else {
      return null;
    }
  }

  private Dimensions getWarehouseDimensions(OperationType operationType,
      Long warehouseFrom, Dimensions warehouseFromDimensions,
      Long warehouseTo, Dimensions warehouseToDimensions,
      Long lineWarehouse, Long parentWarehouse) {

    if (operationType.consumesStock()) {
      if (!DataUtils.isId(parentWarehouse) || Objects.equals(parentWarehouse, warehouseFrom)) {
        return warehouseFromDimensions;
      } else {
        return getDimensions(VIEW_WAREHOUSES, parentWarehouse);
      }

    } else {
      if (!DataUtils.isId(lineWarehouse) || Objects.equals(lineWarehouse, warehouseTo)) {
        return warehouseToDimensions;
      } else {
        return getDimensions(VIEW_WAREHOUSES, lineWarehouse);
      }
    }
  }

  private TradeAccounts getWarehouseTradeAccounts(OperationType operationType,
      Long warehouseFrom, TradeAccounts warehouseFromAccounts,
      Long warehouseTo, TradeAccounts warehouseToAccounts,
      Long lineWarehouse, Long parentWarehouse) {

    if (operationType.consumesStock()) {
      if (!DataUtils.isId(parentWarehouse) || Objects.equals(parentWarehouse, warehouseFrom)) {
        return warehouseFromAccounts;
      } else {
        return getTradeAccounts(VIEW_WAREHOUSES, parentWarehouse);
      }

    } else {
      if (!DataUtils.isId(lineWarehouse) || Objects.equals(lineWarehouse, warehouseTo)) {
        return warehouseToAccounts;
      } else {
        return getTradeAccounts(VIEW_WAREHOUSES, lineWarehouse);
      }
    }
  }

  private static Dimensions computeTradeDimensions(List<TradeDimensionsPrecedence> precedence,
      Map<TradeDimensionsPrecedence, Dimensions> input) {

    return Dimensions.merge(precedence.stream().map(input::get).collect(Collectors.toList()));
  }

  private static TradeAccounts computeTradeAccounts(List<TradeAccountsPrecedence> precedence,
      Map<TradeAccountsPrecedence, TradeAccounts> input, TradeAccounts defaultAccounts) {

    List<TradeAccounts> list = precedence.stream().map(input::get).collect(Collectors.toList());
    list.add(defaultAccounts);

    return TradeAccounts.merge(list);
  }

  private static void postPayments(BeeRowSet buffer, BeeRowSet payments, boolean asDebit,
      Long debtAccount, double factor, Long currency, Long employee, Dimensions dimensions) {

    int dateIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_DATE);
    int amountIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_AMOUNT);

    int accountIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_ACCOUNT);
    int typeAccountIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_TYPE_ACCOUNT);

    int seriesIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_SERIES);
    int numberIndex = payments.getColumnIndex(COL_TRADE_PAYMENT_NUMBER);

    int outputPaymentIndex = buffer.getColumnIndex(COL_FIN_TRADE_PAYMENT);

    int outputSeriesIndex = buffer.getColumnIndex(asDebit
        ? COL_FIN_DEBIT_SERIES : COL_FIN_CREDIT_SERIES);
    int outputDocumentIndex = buffer.getColumnIndex(asDebit
        ? COL_FIN_DEBIT_DOCUMENT : COL_FIN_CREDIT_DOCUMENT);

    for (BeeRow row : payments) {
      DateTime date = row.getDateTime(dateIndex);

      Double amount = row.getDouble(amountIndex);
      if (BeeUtils.nonZero(amount)) {
        amount *= factor;
      }

      Long account = row.getLong(accountIndex);
      if (account == null) {
        account = row.getLong(typeAccountIndex);
      }

      String series = row.getString(seriesIndex);
      String number = row.getString(numberIndex);

      Long debit = asDebit ? account : debtAccount;
      Long credit = asDebit ? debtAccount : account;

      BeeRow output = write(buffer, date, debit, credit, amount, currency, employee, dimensions);

      if (output != null) {
        output.setValue(outputPaymentIndex, row.getId());

        if (!BeeUtils.isEmpty(number)) {
          output.setValue(outputDocumentIndex, number.trim());

          if (!BeeUtils.isEmpty(series)) {
            output.setValue(outputSeriesIndex, series.trim());
          }
        }
      }
    }
  }

  private void updateContents(BeeRowSet rowSet, Long operation) {
    if (!DataUtils.isEmpty(rowSet)) {
      int contentIndex = rowSet.getColumnIndex(COL_FIN_CONTENT);

      int debitIndex = rowSet.getColumnIndex(COL_FIN_DEBIT);
      int creditIndex = rowSet.getColumnIndex(COL_FIN_CREDIT);

      Map<String, String> contentTranslationColumns =
          sys.getView(rowSet.getViewName()).getTranslationColumns(COL_FIN_CONTENT);

      int precision = rowSet.getColumn(contentIndex).getPrecision();

      int updated = 0;

      for (BeeRow row : rowSet) {
        Long debit = row.getLong(debitIndex);
        Long credit = row.getLong(creditIndex);

        if (BeeUtils.isEmpty(row.getString(contentIndex))
            && FinanceUtils.isValidEntry(debit, credit)) {

          BeeRowSet contents = getContents(debit, credit, Dimensions.create(rowSet, row));
          if (!DataUtils.isEmpty(contents)) {
            int rowIndex = 0;
            row.setValue(contentIndex,
                clampContent(contents.getString(rowIndex, COL_FIN_CONTENT), precision));

            if (!BeeUtils.isEmpty(contentTranslationColumns)) {
              for (String colName : contentTranslationColumns.values()) {
                int sourceIndex = contents.getColumnIndex(colName);
                int targetIndex = rowSet.getColumnIndex(colName);

                if (sourceIndex >= 0 && targetIndex >= 0) {
                  row.setValue(targetIndex,
                      clampContent(contents.getString(rowIndex, sourceIndex), precision));
                }
              }
            }

            updated++;
          }
        }
      }

      if (updated < rowSet.getNumberOfRows() && DataUtils.isId(operation)) {
        BeeRowSet operationData = qs.getViewDataById(VIEW_TRADE_OPERATIONS, operation);

        if (!DataUtils.isEmpty(operationData)) {
          int operationNameIndex = operationData.getColumnIndex(COL_OPERATION_NAME);
          Map<String, String> operationNameTranslationColumns =
              sys.getView(operationData.getViewName()).getTranslationColumns(COL_OPERATION_NAME);

          int rowIndex = 0;

          Map<Integer, String> values = new HashMap<>();
          values.put(contentIndex,
              clampContent(operationData.getString(rowIndex, operationNameIndex), precision));

          if (!BeeUtils.isEmpty(operationNameTranslationColumns)
              && !BeeUtils.isEmpty(contentTranslationColumns)) {

            operationNameTranslationColumns.forEach((locale, colName) -> {
              if (contentTranslationColumns.containsKey(locale)) {
                int sourceIndex = operationData.getColumnIndex(colName);
                int targetIndex = rowSet.getColumnIndex(contentTranslationColumns.get(locale));

                if (sourceIndex >= 0 && targetIndex >= 0) {
                  String value = operationData.getString(rowIndex, sourceIndex);
                  if (!BeeUtils.isEmpty(value)) {
                    values.put(targetIndex, clampContent(value, precision));
                  }
                }
              }
            });
          }

          for (BeeRow row : rowSet) {
            if (BeeUtils.isEmpty(row.getString(contentIndex))) {
              values.forEach(row::setValue);
            }
          }
        }
      }
    }
  }

  private BeeRowSet getContents(Long debit, Long credit, Dimensions dimensions) {
    String debitCode = qs.getValueById(TBL_CHART_OF_ACCOUNTS, debit, COL_ACCOUNT_CODE);
    String creditCode = qs.getValueById(TBL_CHART_OF_ACCOUNTS, credit, COL_ACCOUNT_CODE);

    CompoundFilter filter = Filter.and();
    filter.add(getContentsAccountFilter(debitCode, ALS_DEBIT_CODE));
    filter.add(getContentsAccountFilter(creditCode, ALS_CREDIT_CODE));

    if (dimensions != null) {
      filter.add(dimensions.getFilter());
    }

    return qs.getViewData(VIEW_FINANCE_CONTENTS, filter);
  }

  private static Filter getContentsAccountFilter(String code, String column) {
    if (BeeUtils.isEmpty(code)) {
      return Filter.isFalse();
    }

    CompoundFilter filter = Filter.or();
    int length = BeeUtils.trimRight(code).length();

    for (int i = 1; i <= length; i++) {
      String value = code.substring(0, i);

      if (!BeeUtils.isEmpty(value)) {
        filter.add(Filter.equals(column, value));
      }
    }

    return filter;
  }

  private static String clampContent(String value, int precision) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    } else if (precision > 0) {
      return BeeUtils.left(value.trim(), precision);
    } else {
      return value.trim();
    }
  }

  private static void updateDocumentNumbers(BeeRowSet rowSet, String series, String number) {
    if (!DataUtils.isEmpty(rowSet) && !BeeUtils.isEmpty(number)) {
      int debitSeriesIndex = rowSet.getColumnIndex(COL_FIN_DEBIT_SERIES);
      int debitDocumentIndex = rowSet.getColumnIndex(COL_FIN_DEBIT_DOCUMENT);

      int creditSeriesIndex = rowSet.getColumnIndex(COL_FIN_CREDIT_SERIES);
      int creditDocumentIndex = rowSet.getColumnIndex(COL_FIN_CREDIT_DOCUMENT);

      for (BeeRow row : rowSet) {
        if (BeeUtils.allEmpty(row.getString(debitSeriesIndex),
            row.getString(debitDocumentIndex))) {

          if (!BeeUtils.isEmpty(series)) {
            row.setValue(debitSeriesIndex, series);
          }
          row.setValue(debitDocumentIndex, number);
        }

        if (BeeUtils.allEmpty(row.getString(creditSeriesIndex),
            row.getString(creditDocumentIndex))) {

          if (!BeeUtils.isEmpty(series)) {
            row.setValue(creditSeriesIndex, series);
          }
          row.setValue(creditDocumentIndex, number);
        }
      }
    }
  }

  private static void updateJournal(BeeRowSet rowSet, Long journal) {
    if (!DataUtils.isEmpty(rowSet) && DataUtils.isId(journal)) {
      int index = rowSet.getColumnIndex(COL_FIN_JOURNAL);

      for (BeeRow row : rowSet) {
        if (!DataUtils.isId(row.getLong(index))) {
          row.setValue(index, journal);
        }
      }
    }
  }

  private static void updateCompany(BeeRowSet rowSet, Long company) {
    if (!DataUtils.isEmpty(rowSet) && DataUtils.isId(company)) {
      int index = rowSet.getColumnIndex(COL_FIN_COMPANY);

      for (BeeRow row : rowSet) {
        if (!DataUtils.isId(row.getLong(index))) {
          row.setValue(index, company);
        }
      }
    }
  }

  private static BeeRow write(BeeRowSet rowSet, DateTime date,
      Long debit, Long credit, Double amount, Long currency,
      Long employee, Dimensions dimensions) {

    if (FinanceUtils.isValidEntry(date, debit, credit, amount, currency)) {
      BeeRow row = rowSet.addEmptyRow();

      row.setValue(rowSet.getColumnIndex(COL_FIN_DATE), date);

      row.setValue(rowSet.getColumnIndex(COL_FIN_DEBIT), debit);
      row.setValue(rowSet.getColumnIndex(COL_FIN_CREDIT), credit);

      row.setValue(rowSet.getColumnIndex(COL_FIN_AMOUNT), amount);
      row.setValue(rowSet.getColumnIndex(COL_FIN_CURRENCY), currency);

      if (DataUtils.isId(employee)) {
        row.setValue(rowSet.getColumnIndex(COL_FIN_EMPLOYEE), employee);
      }

      if (dimensions != null && !dimensions.isEmpty()) {
        dimensions.applyTo(rowSet.getColumns(), row);
      }

      return row;

    } else {
      return null;
    }
  }

  private static BeeRowSet aggregate(BeeRowSet input) {
    if (DataUtils.getNumberOfRows(input) <= 1) {
      return input;
    }

    int amountIndex = input.getColumnIndex(COL_FIN_AMOUNT);

    Map<List<String>, Double> map = input.getRows().stream().collect(
        Collectors.groupingBy(row -> FinanceUtils.groupingFunction(row, amountIndex),
            Collectors.summingDouble(row -> BeeUtils.unbox(row.getDouble(amountIndex)))));

    if (map.size() == input.getNumberOfRows()) {
      return input;
    }

    BeeRowSet result = new BeeRowSet(input.getViewName(), input.getColumns());
    int amountScale = input.getColumn(amountIndex).getScale();

    map.forEach((values, amount) -> {
      List<String> list = new ArrayList<>(values);
      list.set(amountIndex, BeeUtils.toString(amount, amountScale));

      result.addRow(DataUtils.NEW_ROW_ID, DataUtils.NEW_ROW_VERSION, list);
    });

    return result;
  }

  private ResponseObject commitTradeDocument(long docId, BeeRowSet rowSet) {
    SqlDelete delete = new SqlDelete(TBL_FINANCIAL_RECORDS)
        .setWhere(SqlUtils.equals(TBL_FINANCIAL_RECORDS, COL_FIN_TRADE_DOCUMENT, docId));

    ResponseObject deleteResponse = qs.updateDataWithResponse(delete);
    if (deleteResponse.hasErrors() || DataUtils.isEmpty(rowSet)) {
      return deleteResponse;
    }

    int index = rowSet.getColumnIndex(COL_FIN_TRADE_DOCUMENT);
    for (BeeRow row : rowSet) {
      row.setValue(index, docId);
    }

    BeeRowSet insert = DataUtils.createRowSetForInsert(rowSet);

    for (int i = 0; i < insert.getNumberOfRows(); i++) {
      ResponseObject insertResponse = deb.commitRow(insert, i, RowInfo.class);
      if (insertResponse.hasErrors()) {
        return insertResponse;
      }
    }

    return ResponseObject.response(insert.getNumberOfRows());
  }
}
