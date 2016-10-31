package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class FinanceModuleBean implements BeeModule {

  private static BeeLogger logger = LogUtils.getLogger(FinanceModuleBean.class);

  @EJB
  FinancePostingBean posting;

  @Override
  public ResponseObject doService(String service, RequestInfo reqInfo) {
    ResponseObject response;

    String svc = BeeUtils.trim(service);
    switch (svc) {
      case SVC_POST_TRADE_DOCUMENT:
        Long docId = reqInfo.getParameterLong(Service.VAR_ID);
        if (DataUtils.isId(docId)) {
          response = posting.postTradeDocument(docId);
        } else {
          response = ResponseObject.parameterNotFound(svc, Service.VAR_ID);
        }
        break;

      default:
        String msg = BeeUtils.joinWords("service not recognized:", svc);
        logger.warning(msg);
        response = ResponseObject.error(msg);
    }

    return response;
  }

  @Override
  public Module getModule() {
    return Module.FINANCE;
  }
}
