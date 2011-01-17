package com.butent.bee.client.visualization.visualizations.corechart;

import com.butent.bee.client.visualization.AbstractDataTable;

public class AreaChart extends CoreChart {
  public AreaChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.AREA);
  }
}
