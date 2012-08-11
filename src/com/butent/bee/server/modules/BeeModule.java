package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.modules.BeeParameter;

import java.util.Collection;
import java.util.List;

public interface BeeModule {

  Collection<String> dependsOn();

  List<SearchResult> doSearch(String query);

  ResponseObject doService(RequestInfo reqInfo);

  Collection<BeeParameter> getDefaultParameters();

  String getName();

  String getResourcePath();

  void init();
}
