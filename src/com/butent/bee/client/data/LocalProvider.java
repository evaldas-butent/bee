package com.butent.bee.client.data;

import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public class LocalProvider extends CachedProvider {

  public LocalProvider(HasDataTable display, NotificationListener notificationListener,
      List<BeeColumn> columns, BeeRowSet table) {
    this(display, notificationListener, null, columns, null, table);
  }

  public LocalProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, BeeRowSet table) {
    this(display, notificationListener, viewName, columns, null, table);
  }
  
  public LocalProvider(HasDataTable display, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, Filter immutableFilter, BeeRowSet table) {
    super(display, notificationListener, viewName, columns, immutableFilter, table);
  }
  
  @Override
  public void refresh(boolean updateActiveRow) {
    updateDisplay(updateActiveRow);
  }
}
