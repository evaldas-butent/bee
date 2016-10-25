package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.FinanceUtils;
import com.butent.bee.shared.modules.finance.TradeAccounts;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

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

  public ResponseObject postTradeDocument(long docId) {
    BeeRowSet docData = qs.getViewData(VIEW_TRADE_DOCUMENTS, Filter.compareId(docId));
    if (DataUtils.isEmpty(docData)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.trdDocument(), docId, dictionary.nothingFound());
    }

    BeeRowSet docLines = qs.getViewData(VIEW_TRADE_DOCUMENT_ITEMS,
        Filter.equals(COL_TRADE_DOCUMENT, docId));
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

      Long warehouse = row.getLong(warehouseIndex);
      Long employee = row.getLong(employeeIndex);

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
    }

    return ResponseObject.emptyResponse();
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

  private void write(BeeRowSet rowSet, DateTime date, Long company,
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
}
