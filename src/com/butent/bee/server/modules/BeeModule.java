package com.butent.bee.server.modules;

import com.google.common.collect.Multimap;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.modules.BeeParameter;

import java.util.Collection;

public interface BeeModule {

  String dependsOn();

  ResponseObject doService(RequestInfo reqInfo);

  Collection<BeeParameter> getDefaultParameters();
  
  String getName();

  String getResourcePath();
  
  Multimap<String, String> getSearchableColumns();

  void init();
}
