package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class AnalysisBean {

  private static BeeLogger logger = LogUtils.getLogger(AnalysisBean.class);

  @EJB
  QueryServiceBean qs;

  public ResponseObject calculateForm(long formId) {
    String msg = BeeUtils.joinWords(SVC_CALCULATE_ANALYSIS_FORM, formId);
    logger.info(msg);
    return ResponseObject.response(msg);
  }

  public ResponseObject verifyForm(long formId) {
    String msg = BeeUtils.joinWords(SVC_VERIFY_ANALYSIS_FORM, formId);
    logger.info(msg);
    return ResponseObject.response(msg);
  }
}
