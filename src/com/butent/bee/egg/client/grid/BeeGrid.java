package com.butent.bee.egg.client.grid;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.DataUtils;

public class BeeGrid {

  public Widget simpleGrid(Object data, Object... columns) {
    Assert.notNull(data);

    BeeView view = DataUtils.createView(data, columns);
    Assert.notNull(view);
    
    int c = view.getColumnCount();
    Assert.isPositive(c);

    int r = view.getRowCount();
    if (r <= 0) {
      BeeKeeper.getLog().warning("data view empty");
      return null;
    }

    BeeCellTable table = new BeeCellTable(r);
    
    String[] arr = view.getColumnNames();
    for (int i = 0; i < c; i++) {
      table.addColumn(new BeeTextColumn(view, i, 256), arr[i]);
    }
    table.initData(r);

    return table;
  }
}