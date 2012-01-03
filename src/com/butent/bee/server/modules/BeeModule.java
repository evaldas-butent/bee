package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;

public interface BeeModule {

  String dependsOn();

  ResponseObject doService(RequestInfo reqInfo);

  String getName();

  String getResourcePath();
}
