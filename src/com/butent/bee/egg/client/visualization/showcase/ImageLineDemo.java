package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.LegendPosition;
import com.butent.bee.egg.client.visualization.visualizations.ImageLineChart;
import com.butent.bee.egg.client.visualization.visualizations.ImageLineChart.Options;

public class ImageLineDemo implements LeftTabPanel.WidgetProvider {

  public Widget getWidget() {
    Options options = Options.create();
    options.setLegend(LegendPosition.LEFT);
    options.setShowValueLabels(false);
    return new ImageLineChart(Showcase.getCompanyPerformance(), options);
  }
}
