package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.butent.bee.egg.client.visualization.AbstractDataTable;

public class BarChart extends CoreChart {
  public BarChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.BARS);
  }
}
