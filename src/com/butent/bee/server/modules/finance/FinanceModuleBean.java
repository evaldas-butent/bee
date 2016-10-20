package com.butent.bee.server.modules.finance;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.rights.Module;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class FinanceModuleBean implements BeeModule {

  @Override
  public ResponseObject doService(String svc, RequestInfo reqInfo) {
    return null;
  }

  @Override
  public Module getModule() {
    return Module.FINANCE;
  }
}
