package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.ImageLineChart;
import com.butent.bee.client.visualization.visualizations.ImageLineChart.Options;

/**
 * Implements demonstration of image line chart visualization.
 */

public class ImageLineDemo implements LeftTabPanel.WidgetProvider {

  @Override
  public Widget getWidget() {
    Options options = Options.create();
    options.setLegend(LegendPosition.LEFT);
    options.setShowValueLabels(false);
    return new ImageLineChart(Showcase.getCompanyPerformance(), options);
  }
}
