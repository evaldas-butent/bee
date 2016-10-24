package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.finance.FinanceConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
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
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  public ResponseObject postTradeDocument(long docId) {
    BeeRowSet docData = qs.getViewData(VIEW_TRADE_DOCUMENTS, Filter.compareId(docId));
    if (DataUtils.isEmpty(docData)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.trdDocument(), docId, dictionary.nothingFound());
    }

    BeeRowSet docItems = qs.getViewData(VIEW_TRADE_DOCUMENT_ITEMS,
        Filter.equals(COL_TRADE_DOCUMENT, docId));
    if (DataUtils.isEmpty(docItems)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.trdDocumentItems(), docId,
          dictionary.nothingFound());
    }

    BeeRowSet config = qs.getViewData(VIEW_FINANCE_CONFIGURATION);
    if (DataUtils.isEmpty(config)) {
      Dictionary dictionary = usr.getDictionary();
      return ResponseObject.warning(dictionary.dataNotAvailable(dictionary.finDefaultAccounts()));
    }

//    TradeAccounts defaultAccounts = TradeAccounts.create(config, config.getRow(0));

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

    Long warehouseFrom = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_FROM);
    Long warehouseTo = docData.getLong(rowIndex, COL_TRADE_WAREHOUSE_TO);

    Long currency = docData.getLong(rowIndex, COL_TRADE_CURRENCY);
    Long payer = docData.getLong(rowIndex, COL_TRADE_PAYER);

    Long manager = docData.getLong(rowIndex, COL_TRADE_MANAGER);

    TradeDocumentSums tdSums = TradeDocumentSums.of(docData, docItems);

    Dimensions docDimensions = Dimensions.create(docData, docData.getRow(rowIndex));
    TradeAccounts docAccounts = TradeAccounts.create(docData, docData.getRow(rowIndex));

    int itemIndex = docItems.getColumnIndex(COL_ITEM);
    int isServiceIndex = docItems.getColumnIndex(COL_ITEM_IS_SERVICE);

    int warehouseIndex = docItems.getColumnIndex(COL_TRADE_ITEM_WAREHOUSE);
    int employeeIndex = docItems.getColumnIndex(COL_TRADE_ITEM_EMPLOYEE);

    int parentIndex = docItems.getColumnIndex(COL_TRADE_ITEM_PARENT);

    int costIndex = docItems.getColumnIndex(COL_TRADE_ITEM_COST);
    int costCurrencyIndex = docItems.getColumnIndex(ALS_COST_CURRENCY);

    int parentCostIndex = docItems.getColumnIndex(ALS_PARENT_COST);
    int parentCostCurrencyIndex = docItems.getColumnIndex(ALS_PARENT_COST_CURRENCY);

    for (BeeRow row : docItems) {
      Long item = row.getLong(itemIndex);
    }

    return ResponseObject.emptyResponse();
  }
}
