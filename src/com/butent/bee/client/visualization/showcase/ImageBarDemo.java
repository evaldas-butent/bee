package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.visualizations.ImageBarChart;
import com.butent.bee.client.visualization.visualizations.ImageBarChart.Options;

public class ImageBarDemo implements LeftTabPanel.WidgetProvider {

  public Widget getWidget() {
    Options options = Options.create();
    options.setValueLabelsInterval(300);
    return new ImageBarChart(Showcase.getCompanyPerformance(), options);
  }
}
