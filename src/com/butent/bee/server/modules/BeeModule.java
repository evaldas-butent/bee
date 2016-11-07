package com.butent.bee.server.modules;

import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.rights.Module;

import java.util.Collection;
import java.util.List;

public interface BeeModule {

  default List<SearchResult> doSearch(String query) {
    return null;
  }

  ResponseObject doService(String svc, RequestInfo reqInfo);

  default Collection<BeeParameter> getDefaultParameters() {
    return null;
  }

  Module getModule();

  default String getResourcePath() {
    return getModule().getName();
  }

  default void init() {
  }
}
