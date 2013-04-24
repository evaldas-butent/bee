package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.visualizations.MapVisualization;
import com.butent.bee.client.visualization.visualizations.MapVisualization.Options;

/**
 * Implements demonstration of map visualization.
 */

public class MapDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;

  public MapDemo() {
    Options options = Options.create();
    options.setEnableScrollWheel(true);
    options.setLineColor("pink");
    options.setLineWidth(5);
    options.setMapType(MapVisualization.Type.HYBRID);
    options.setShowLine(true);
    options.setShowTip(true);

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.NUMBER, "Lat");
    data.addColumn(ColumnType.NUMBER, "Lon");
    data.addColumn(ColumnType.STRING, "Place");

    data.addRows(3);
    data.setValue(0, 0, 54.683333);
    data.setValue(0, 1, 25.283333);
    data.setValue(0, 2, "Vilnius");
    data.setValue(1, 0, 54.9);
    data.setValue(1, 1, 23.933333);
    data.setValue(1, 2, "Kaunas");
    data.setValue(2, 0, 55.7);
    data.setValue(2, 1, 21.133333);
    data.setValue(2, 2, "Klaipėda");

    widget = new MapVisualization(data, options, "400px", "300px");
  }

  @Override
  public Widget getWidget() {
    return widget;
  }
}
