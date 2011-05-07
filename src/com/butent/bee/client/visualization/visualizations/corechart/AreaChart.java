package com.butent.bee.client.visualization.visualizations.corechart;

import com.butent.bee.client.visualization.AbstractDataTable;

/**
 * Implements area chart type visualization.
 */

public class AreaChart extends CoreChart {
  public AreaChart(AbstractDataTable data, Options options) {
    super(data, options);
    options.setType(CoreChart.Type.AREA);
  }
}
