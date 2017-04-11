package com.butent.bee.server.modules.trade;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

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
    return ResponseObject.emptyResponse();
  }

  private ResponseObject getMovementOfGoodsReport(RequestInfo reqInfo) {
    return ResponseObject.emptyResponse();
  }
}
