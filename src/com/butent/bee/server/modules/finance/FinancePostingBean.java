package com.butent.bee.server.modules.finance;

import com.google.common.collect.ImmutableList;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.FinanceUtils;
import com.butent.bee.shared.modules.finance.TradeAccounts;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    BeeRowSet docData = qs.getViewData(VIEW_TRADE_DOCUMENTS, Filter.compareId(docId));

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

    TradeAccounts defaultAccounts = TradeAccounts.createAvailable(config, config.getRow(0));

    Long defaultJournal = config.getLong(0, COL_DEFAULT_JOURNAL);
    Long costOfMerchandise = config.getLong(0, COL_COST_OF_MERCHANDISE);

    int rowIndex = 0;
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
    String operationName = docData.getString(rowIndex, COL_OPERATION_NAME);

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

    Long warehouseFrom = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_FROM);
    Long warehouseTo = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_TO);

    Long manager = docData.getLong(rowIndex, COL_TRADE_MANAGER);

    TradeDocumentSums tdSums = TradeDocumentSums.of(docData, docLines);

    Dimensions documentDimensions = Dimensions.create(docData, docData.getRow(rowIndex));
    TradeAccounts documentAccounts = TradeAccounts.create(docData, docData.getRow(rowIndex));

    Dimensions operationDimensions = getDimensions(VIEW_TRADE_OPERATIONS, operation);
    TradeAccounts operationAccounts = getTradeAccounts(VIEW_TRADE_OPERATIONS, operation);

    Dimensions supplierDimensions = getDimensions(VIEW_COMPANIES, supplier);
    TradeAccounts supplierAccounts = getTradeAccounts(VIEW_COMPANIES, supplier);

    Dimensions customerDimensions = getDimensions(VIEW_COMPANIES, customer);
    TradeAccounts customerAccounts = getTradeAccounts(VIEW_COMPANIES, customer);

    Dimensions payerDimensions = getDimensions(VIEW_COMPANIES, payer);
    TradeAccounts payerAccounts = getTradeAccounts(VIEW_COMPANIES, payer);

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

    BeeRowSet buffer = new BeeRowSet(VIEW_FINANCIAL_RECORDS,
        sys.getView(VIEW_FINANCIAL_RECORDS).getRowSetColumns());

    for (BeeRow row : docLines) {
      Long item = row.getLong(itemIndex);
      boolean isService = BeeUtils.isTrue(row.getBoolean(isServiceIndex));

      Double quantity = row.getDouble(quantityIndex);

      Long warehouse = row.getLong(warehouseIndex);

      Long employee = row.getLong(employeeIndex);
      if (!DataUtils.isId(employee)) {
        employee = manager;
      }

      Long parent = row.getLong(parentIndex);

      Double cost = row.getDouble(costIndex);
      Long costCurrency = row.getLong(costCurrencyIndex);

      Double parentCost = row.getDouble(parentCostIndex);
      Long parentCostCurrency = row.getLong(parentCostCurrencyIndex);

      Long parentWarehouse = getParentWarehouse(parent);

      Dimensions lineDimensions = Dimensions.create(docLines, row);
      TradeAccounts lineAccounts = TradeAccounts.create(docLines, row);

      Dimensions itemDimensions = getDimensions(VIEW_ITEMS, item);
      TradeAccounts itemAccounts = getTradeAccounts(VIEW_ITEMS, item);

      if (DataUtils.isId(parent) && BeeUtils.nonZero(parentCost)
          && DataUtils.isId(parentCostCurrency)) {

        write(buffer, date, customer,
            defaultAccounts.getCostOfGoodsSold(), costOfMerchandise,
            parentCost * quantity, parentCostCurrency, employee,
            Dimensions.merge(ImmutableList.of(lineDimensions, documentDimensions,
                itemDimensions, operationDimensions)));
      }
    }

    BeeRowSet output = aggregate(buffer);

    updateJournal(output, defaultJournal);
    updateDocumentNumbers(output, series, number);

    return commitTradeDocument(docId, output);
  }

  private Dimensions getDimensions(String viewName, Long id) {
    if (DataUtils.isId(id)) {
      BeeRowSet rowSet = qs.getViewData(viewName, Filter.compareId(id));

      if (!DataUtils.isEmpty(rowSet)) {
        return Dimensions.create(rowSet, rowSet.getRow(0));
      }
    }
    return null;
  }

  private TradeAccounts getTradeAccounts(String viewName, Long id) {
    if (DataUtils.isId(id)) {
      BeeRowSet rowSet = qs.getViewData(viewName, Filter.compareId(id));

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

  private static void write(BeeRowSet rowSet, DateTime date, Long company,
      Long debit, Long credit, Double amount, Long currency,
      Long employee, Dimensions dimensions) {

    if (FinanceUtils.isValidEntry(date, debit, credit, amount, currency)) {
      BeeRow row = rowSet.addEmptyRow();

      row.setValue(rowSet.getColumnIndex(COL_FIN_DATE), date);

      if (DataUtils.isId(company)) {
        row.setValue(rowSet.getColumnIndex(COL_FIN_COMPANY), company);
      }

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
