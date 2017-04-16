package com.butent.bee.server.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.shared.modules.trade.TradeReportGroup;
import com.butent.bee.shared.report.ReportParameters;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class TradeReportsBean {

  private static BeeLogger logger = LogUtils.getLogger(TradeReportsBean.class);

  @EJB
  QueryServiceBean qs;

  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);
    switch (svc) {
      case SVC_TRADE_STOCK_REPORT:
        response = getStockReport(reqInfo);
        break;

      case SVC_TRADE_MOVEMENT_OF_GOODS_REPORT:
        response = getMovementOfGoodsReport(reqInfo);
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  private ResponseObject getStockReport(RequestInfo reqInfo) {
    if (!reqInfo.hasParameter(Service.VAR_REPORT_PARAMETERS)) {
      return ResponseObject.parameterNotFound(reqInfo.getLabel(), Service.VAR_REPORT_PARAMETERS);
    }

    ReportParameters parameters =
        ReportParameters.restore(reqInfo.getParameter(Service.VAR_REPORT_PARAMETERS));

    DateTime date = parameters.getDateTime(RP_DATE);

    boolean showQuantity = parameters.getBoolean(RP_SHOW_QUANTITY);
    boolean showAmount = parameters.getBoolean(RP_SHOW_AMOUNT);

    if (!showQuantity && !showAmount) {
      showQuantity = true;
      showAmount = true;
    }

    ItemPrice itemPrice = parameters.getEnum(RP_ITEM_PRICE, ItemPrice.class);
    if (itemPrice == ItemPrice.COST) {
      itemPrice = null;
    }

    Long currency = parameters.getLong(RP_CURRENCY);

    Set<Long> warehouses = parameters.getIds(RP_WAREHOUSES);
    Set<Long> suppliers = parameters.getIds(RP_SUPPLIERS);
    Set<Long> manufacturers = parameters.getIds(RP_MANUFACTURERS);
    Set<Long> documents = parameters.getIds(RP_DOCUMENTS);

    Set<Long> itemTypes = parameters.getIds(RP_ITEM_TYPES);
    Set<Long> itemGroups = parameters.getIds(RP_ITEM_GROUPS);
    Set<Long> itemCategories = parameters.getIds(RP_ITEM_CATEGORIES);
    Set<Long> items = parameters.getIds(RP_ITEMS);

    DateTime receivedFrom = parameters.getDateTime(RP_RECEIVED_FROM);
    DateTime receivedTo = parameters.getDateTime(RP_RECEIVED_TO);

    String itemFilter = parameters.getText(RP_ITEM_FILTER);

    if (!items.isEmpty()) {
      manufacturers.clear();

      itemTypes.clear();
      itemGroups.clear();
      itemCategories.clear();

      itemFilter = null;
    }

    List<TradeReportGroup> rowGroups = TradeReportGroup.parseList(parameters, 5);
    boolean summary = parameters.getBoolean(RP_SUMMARY);

    TradeReportGroup columnGroup = TradeReportGroup.parse(parameters.getText(RP_COLUMNS));
    if (columnGroup == null) {
      columnGroup = TradeReportGroup.WAREHOUSE;
    }

    Map<String, String> result = new LinkedHashMap<>(parameters);
    return ResponseObject.response(result);
  }

  private ResponseObject getMovementOfGoodsReport(RequestInfo reqInfo) {
    return ResponseObject.emptyResponse();
  }
}
