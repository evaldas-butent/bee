package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.DataTable;
import com.butent.bee.egg.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.egg.client.visualization.visualizations.IntensityMap;
import com.butent.bee.egg.client.visualization.visualizations.IntensityMap.Options;

public class IntensityDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;
  
  public IntensityDemo() {
    Options options = Options.create();

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, "", "Country");
    data.addColumn(ColumnType.NUMBER, "Population (mil)", "a");
    data.addColumn(ColumnType.NUMBER, "Area (km2)", "b");
    data.addRows(5);
    data.setValue(0, 0, "CN");
    data.setValue(0, 1, 1324);
    data.setValue(0, 2, 9640821);
    data.setValue(1, 0, "IN");
    data.setValue(1, 1, 1133);
    data.setValue(1, 2, 3287263);
    data.setValue(2, 0, "US");
    data.setValue(2, 1, 304);
    data.setValue(2, 2, 9629091);
    data.setValue(3, 0, "ID");
    data.setValue(3, 1, 232);
    data.setValue(3, 2, 1904569);
    data.setValue(4, 0, "BR");
    data.setValue(4, 1, 187);
    data.setValue(4, 2, 8514877);
    
    widget = new IntensityMap(data, options);
  }

  public Widget getWidget() {
    return widget;
  }
}
