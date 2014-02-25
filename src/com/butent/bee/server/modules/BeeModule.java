package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.rights.Module;

import java.util.Collection;
import java.util.List;

public interface BeeModule {

  List<SearchResult> doSearch(String query);

  ResponseObject doService(String svc, RequestInfo reqInfo);

  Collection<BeeParameter> getDefaultParameters();

  Module getModule();

  String getResourcePath();

  void init();
}
