package com.butent.bee.client.visualization.visualizations.corechart;

import com.butent.bee.client.visualization.AbstractDataTable;

public class ColumnChart extends CoreChart {
  public ColumnChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.COLUMNS);
  }
}
