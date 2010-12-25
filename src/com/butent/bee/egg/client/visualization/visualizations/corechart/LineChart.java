package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.butent.bee.egg.client.visualization.AbstractDataTable;

public class LineChart extends CoreChart {
  public LineChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.LINE);
  }
}
