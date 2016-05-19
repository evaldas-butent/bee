package com.butent.bee.client.data;

import com.butent.bee.client.screen.HandlesStateChange;
import com.butent.bee.shared.ui.HandlesActions;

public interface HasDataProvider extends HandlesActions, HandlesStateChange {
  Provider getDataProvider();
}
