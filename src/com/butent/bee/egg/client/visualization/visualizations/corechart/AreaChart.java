package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.butent.bee.egg.client.visualization.AbstractDataTable;

public class AreaChart extends CoreChart {
  public AreaChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.AREA);
  }
}
