package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.corechart.PieChart;
import com.butent.bee.client.visualization.visualizations.corechart.TextStyle;
import com.butent.bee.client.widget.BeeLabel;

/**
 * Implements demonstration of pie chart visualization.
 */

public class PieDemo implements LeftTabPanel.WidgetProvider {
  public Widget getWidget() {
    PieChart.PieOptions options = PieChart.createPieOptions();
    options.setWidth(400);
    options.setHeight(240);
    options.set3D(true);
    options.setTitle("Pardavimai");
    options.setLegend(LegendPosition.LEFT);

    TextStyle style = TextStyle.create();
    style.setColor("red");
    style.setFontName("Verdana");
    style.setFontSize(20);
    options.setTitleTextStyle(style);

    DataTable data = Showcase.getSales();

    PieChart viz = new PieChart(data, options);
    BeeLabel status = new BeeLabel();
    BeeLabel onMouseOverAndOutStatus = new BeeLabel();
    viz.addSelectHandler(new SelectionDemo(viz, status));
    viz.addReadyHandler(new ReadyDemo(status));
    viz.addOnMouseOverHandler(new OnMouseOverDemo(onMouseOverAndOutStatus));
    viz.addOnMouseOutHandler(new OnMouseOutDemo(onMouseOverAndOutStatus));

    Vertical result = new Vertical();
    result.add(status);
    result.add(viz);
    result.add(onMouseOverAndOutStatus);
    return result;
  }
}
