package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.visualizations.ImagePieChart;
import com.butent.bee.client.visualization.visualizations.ImagePieChart.Options;

/**
 * Implements demonstration of image pie chart visualization.
 */

public class ImagePieDemo implements LeftTabPanel.WidgetProvider {

  @Override
  public Widget getWidget() {
    Options options = Options.create();
    options.setLabels("value");
    return new ImagePieChart(Showcase.getSales(), options);
  }
}