package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.visualizations.ImageChart;

/**
 * Implements demonstration of data table visualization in an image chart.
 */

public class ImageDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;

  public ImageDemo() {
    ImageChart.Options options = ImageChart.Options.create();

    options.set("cht", "rs");
    options.set("chco", "00ff00,ff00ff");
    options.set("chg", "25.0,25.0,4.0,4.0");
    options.set("chm", "B,FF000080,0,1.0,5.0|B,FF990080,1,1.0,5.0");
    options.set("chls", "2,1,0|2,5,5");
    options.setSize(300, 300);

    DataTable dataTable = DataTable.create();
    dataTable.addColumn(ColumnType.STRING);
    dataTable.addColumn(ColumnType.NUMBER);
    dataTable.addColumn(ColumnType.NUMBER);
    dataTable.addRows(8);

    char pi = '\u03C0';
    dataTable.setValue(0, 0, "0");
    dataTable.setValue(1, 0, pi + "/4");
    dataTable.setValue(2, 0, pi + "/2");
    dataTable.setValue(3, 0, "3" + pi + "/4");
    dataTable.setValue(4, 0, "" + pi);
    dataTable.setValue(5, 0, "5" + pi + "/4");
    dataTable.setValue(6, 0, "3" + pi + "/2");
    dataTable.setValue(7, 0, "7" + pi + "/4");

    dataTable.setValue(0, 1, 10);
    dataTable.setValue(1, 1, 20);
    dataTable.setValue(2, 1, 30);
    dataTable.setValue(3, 1, 40);
    dataTable.setValue(4, 1, 50);
    dataTable.setValue(5, 1, 60);
    dataTable.setValue(6, 1, 70);
    dataTable.setValue(7, 1, 80);

    dataTable.setValue(0, 2, 100);
    dataTable.setValue(1, 2, 80);
    dataTable.setValue(2, 2, 60);
    dataTable.setValue(3, 2, 30);
    dataTable.setValue(4, 2, 25);
    dataTable.setValue(5, 2, 20);
    dataTable.setValue(6, 2, 10);
    dataTable.setValue(7, 2, 5);

    widget = new ImageChart(dataTable, options);
  }

  public Widget getWidget() {
    return widget;
  }
}
