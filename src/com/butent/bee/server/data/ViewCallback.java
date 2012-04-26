package com.butent.bee.server.data;

import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.utils.BeeUtils;

public abstract class ViewCallback {

  private final String moduleName;

  public ViewCallback(String moduleName) {
    Assert.notEmpty(moduleName);
    this.moduleName = moduleName;
  }

  public abstract void afterViewData(SqlSelect query, BeeRowSet rowset);

  ViewCallback getInstance() {
    return null;
  }

  boolean sameModule(String viewModule) {
    return BeeUtils.same(viewModule, moduleName);
  }
}
