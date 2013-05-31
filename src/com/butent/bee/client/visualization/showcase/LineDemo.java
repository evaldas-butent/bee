package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.corechart.AxisOptions;
import com.butent.bee.client.visualization.visualizations.corechart.HorizontalAxisOptions;
import com.butent.bee.client.visualization.visualizations.corechart.LineChart;
import com.butent.bee.client.visualization.visualizations.corechart.Options;
import com.butent.bee.client.widget.Label;

/**
 * Implements demonstration of line chart visualization.
 */

public class LineDemo implements LeftTabPanel.WidgetProvider {
  @Override
  public Widget getWidget() {

    Options options = Options.create();
    options.setHeight(240);
    options.setWidth(400);
    options.setTitle("Line Chart");
    options.setLegend(LegendPosition.TOP);
    options.setBackgroundColor("#E3F6CE");
    options.setCurveType("function");

    AxisOptions vAxisOptions = AxisOptions.create();
    vAxisOptions.setMinValue(0);
    vAxisOptions.setMaxValue(3000);
    options.setVAxisOptions(vAxisOptions);

    HorizontalAxisOptions hAxisOptions = HorizontalAxisOptions.create();
    hAxisOptions.setDirection(-1);
    options.setHAxisOptions(hAxisOptions);

    DataTable data = Showcase.getCompanyPerformance();
    LineChart viz = new LineChart(data, options);

    Label status = new Label();
    Label onMouseOverAndOutStatus = new Label();
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
