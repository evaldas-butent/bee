package com.butent.bee.egg.client.visualization.visualizations.corechart;

import com.butent.bee.egg.client.visualization.AbstractDataTable;

public class ColumnChart extends CoreChart {
  public ColumnChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.COLUMNS);
  }
}
