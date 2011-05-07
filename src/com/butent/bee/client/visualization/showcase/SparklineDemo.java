package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.visualizations.ImageSparklineChart;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements demonstration of a image spark line chart visualization.
 */

public class SparklineDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;

  public SparklineDemo() {
    ImageSparklineChart.Options options = ImageSparklineChart.Options.create();
    options.setWidth(300);
    options.setHeight(200);
    options.setShowAxisLines(true);
    options.setShowValueLabels(true);
    options.setLabelPosition("left");

    DataTable data = DataTable.create();

    data.addColumn(ColumnType.NUMBER, "Pajamos");
    data.addColumn(ColumnType.NUMBER, "Sąnaudos");
    data.addColumn(ColumnType.NUMBER, "Pelnas");
    data.addColumn(ColumnType.NUMBER, "Darbuotojai");

    int rows = BeeUtils.randomInt(10, 30);
    data.addRows(rows);

    for (int i = 0; i < rows; i++) {
      int x = BeeUtils.randomInt(2000, 3000);
      int y = BeeUtils.randomInt(1500, 2000);
      data.setValue(i, 0, x);
      data.setValue(i, 1, y);
      data.setValue(i, 2, x - y);
      data.setValue(i, 3, 20 + i / 2 + BeeUtils.randomInt(-2, 2));
    }

    widget = new ImageSparklineChart(data, options);
  }

  public Widget getWidget() {
    return widget;
  }
}
