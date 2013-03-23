package com.butent.bee.client.data;

import com.butent.bee.client.Callback;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.IsRow;

public interface ParentRowCreator {
  boolean createParentRow(NotificationListener notificationListener, Callback<IsRow> callback);
}
