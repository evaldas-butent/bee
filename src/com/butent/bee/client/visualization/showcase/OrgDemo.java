package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.visualizations.OrgChart;
import com.butent.bee.client.visualization.visualizations.OrgChart.Options;
import com.butent.bee.client.visualization.visualizations.OrgChart.Size;
import com.butent.bee.client.widget.BeeLabel;

/**
 * Implements demonstration of a organizational chart visualization.
 */

public class OrgDemo implements LeftTabPanel.WidgetProvider {
  private Vertical panel = new Vertical();

  public OrgDemo() {
    Options options = Options.create();
    options.setSize(Size.MEDIUM);
    options.setAllowCollapse(true);

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, "Name");
    data.addColumn(ColumnType.STRING, "Manager");
    data.addRows(8);
    data.setValue(0, 0, "Daiva");
    data.setValue(1, 0, "Jovita");
    data.setValue(1, 1, "Daiva");
    data.setValue(2, 0, "Ramūnas");
    data.setValue(2, 1, "Daiva");
    data.setValue(3, 0, "Tomas");
    data.setValue(3, 1, "Ramūnas");
    data.setValue(4, 0, "Saulius");
    data.setValue(4, 1, "Ramūnas");
    data.setValue(5, 0, "Lina");
    data.setValue(5, 1, "Jovita");
    data.setValue(6, 0, "Marius");
    data.setValue(6, 1, "Daiva");
    data.setValue(7, 0, "Danutė");
    data.setValue(7, 1, "Jovita");

    OrgChart viz = new OrgChart(data, options);
    BeeLabel status = new BeeLabel();
    viz.addSelectHandler(new SelectionDemo(viz, status));
    panel.add(viz);
    panel.add(status);
  }

  public Widget getWidget() {
    return panel;
  }
}
