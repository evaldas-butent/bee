package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.CommonsConstants;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class CommonsModuleBean implements BeeModule {

  @Override
  public String dependsOn() {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    Assert.unsupported();
    return null;
  }

  @Override
  public String getName() {
    return CommonsConstants.COMMONS_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }
}
