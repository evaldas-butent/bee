package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;

public interface BeeModule {

  String getName();

  ResponseObject doService(RequestInfo reqInfo);
}
