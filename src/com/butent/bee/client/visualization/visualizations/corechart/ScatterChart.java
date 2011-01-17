package com.butent.bee.client.visualization.visualizations.corechart;

import com.butent.bee.client.visualization.AbstractDataTable;

public class ScatterChart extends CoreChart {
  public ScatterChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.SCATTER);
  }
}
