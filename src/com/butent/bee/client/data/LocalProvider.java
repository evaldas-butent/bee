package com.butent.bee.client.data;

import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.event.ModificationPreviewer;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;
import java.util.Map;

public class LocalProvider extends CachedProvider {

  public LocalProvider(HasDataTable display, HasDataProvider presenter,
      ModificationPreviewer modificationPreviewer, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, BeeRowSet table) {

    this(display, presenter, modificationPreviewer, notificationListener, viewName, columns, null,
        table, null, null);
  }

  public LocalProvider(HasDataTable display, HasDataProvider presenter,
      ModificationPreviewer modificationPreviewer, NotificationListener notificationListener,
      String viewName, List<BeeColumn> columns, Filter immutableFilter, BeeRowSet table,
      Map<String, Filter> parentFilters, Filter userFilter) {

    super(display, presenter, modificationPreviewer, notificationListener, viewName, columns,
        immutableFilter, table, parentFilters, userFilter);
  }

  @Override
  public void refresh(boolean preserveActiveRow) {
    getDisplay().setRowCount(getTable().getNumberOfRows(), true);
    updateDisplay(preserveActiveRow);
  }
}
