package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.visualizations.ImagePieChart;
import com.butent.bee.egg.client.visualization.visualizations.ImagePieChart.Options;

public class ImagePieDemo implements LeftTabPanel.WidgetProvider {

  public Widget getWidget() {
    Options options = Options.create();
    options.setLabels("value");
    return new ImagePieChart(Showcase.getSales(), options);
  }
}