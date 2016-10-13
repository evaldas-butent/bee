package com.butent.bee.client.data;

import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.ui.HandlesActions;

public interface HasDataProvider extends HandlesActions, HandlesStateChange {

  Provider getDataProvider();

  default Filter getUserFilter() {
    return (getDataProvider() == null) ? null : getDataProvider().getUserFilter();
  }
}
