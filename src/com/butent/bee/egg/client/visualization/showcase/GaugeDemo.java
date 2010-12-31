package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.DataTable;
import com.butent.bee.egg.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.egg.client.visualization.visualizations.Gauge;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class GaugeDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;

  public GaugeDemo() {
    Gauge.Options options = Gauge.Options.create();
    options.setWidth(400);
    options.setHeight(240);

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, "Label");
    data.addColumn(ColumnType.NUMBER, "Value");
    data.addRows(3);
    data.setValue(0, 0, "Memory");
    data.setValue(0, 1, BeeUtils.randomInt(10, 90));
    data.setValue(1, 0, "CPU");
    data.setValue(1, 1, BeeUtils.randomInt(10, 90));
    data.setValue(2, 0, "Network");
    data.setValue(2, 1, BeeUtils.randomInt(10, 90));

    options.setGaugeRange(0, 100);
    options.setGreenRange(0, 50);
    options.setYellowRange(50, 75);
    options.setRedRange(75, 100);
    
    widget = new Gauge(data, options);
  }

  public Widget getWidget() {
    return widget;
  }
}
