package com.butent.bee.client.visualization.visualizations.corechart;

import com.butent.bee.client.visualization.AbstractDataTable;

public class LineChart extends CoreChart {
  public LineChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.LINE);
  }
}
