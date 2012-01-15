package com.butent.bee.client.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsTable;
import com.butent.bee.shared.data.filter.Filter;

import java.util.List;

public class LocalProvider extends CachedProvider {

  public LocalProvider(HasDataTable display, List<BeeColumn> columns, Filter dataFilter,
      IsTable<?, ?> table) {
    super(display, null, columns, null, null, dataFilter, table);
  }
  
  public void addRow(BeeRow row) {
    Assert.notNull(row);
    if (getTable() instanceof BeeRowSet) {
      ((BeeRowSet) getTable()).addRow(row);
      applyFilter(getUserFilter());
    }
  }
}
