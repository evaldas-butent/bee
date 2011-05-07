package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.ImageAreaChart;
import com.butent.bee.client.visualization.visualizations.ImageAreaChart.Options;

/**
 * Implements demonstration of an image area chart visualization.
 */

public class ImageAreaDemo implements LeftTabPanel.WidgetProvider {

  public Widget getWidget() {
    Options options = Options.create();
    options.setTitle("BŪTENT");
    options.setLegend(LegendPosition.BOTTOM);

    return new ImageAreaChart(Showcase.getCompanyPerformance(), options);
  }
}
