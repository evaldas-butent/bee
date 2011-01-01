package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.layout.Vertical;
import com.butent.bee.egg.client.visualization.DataTable;
import com.butent.bee.egg.client.visualization.visualizations.corechart.AreaChart;
import com.butent.bee.egg.client.visualization.visualizations.corechart.AxisOptions;
import com.butent.bee.egg.client.visualization.visualizations.corechart.ChartAreaOptions;
import com.butent.bee.egg.client.visualization.visualizations.corechart.Options;
import com.butent.bee.egg.client.widget.BeeLabel;

public class AreaDemo implements LeftTabPanel.WidgetProvider {
  public Widget getWidget() {
    Options options = Options.create();
    options.setHeight(240);
    options.setTitle("Įmonės veikla");
    options.setWidth(400);
    
    ChartAreaOptions areaOptions = ChartAreaOptions.create();
    areaOptions.setLeft(50);
    areaOptions.setWidthPct(60);
    options.setChartAreaOptions(areaOptions);
    
    AxisOptions vAxisOptions = AxisOptions.create();
    vAxisOptions.setMinValue(0);
    vAxisOptions.setMaxValue(2000);
    options.setVAxisOptions(vAxisOptions);

    DataTable data = Showcase.getCompanyPerformance();
    AreaChart viz = new AreaChart(data, options);

    BeeLabel status = new BeeLabel();
    BeeLabel onMouseOverAndOutStatus = new BeeLabel();

    viz.addSelectHandler(new SelectionDemo(viz, status));
    viz.addReadyHandler(new ReadyDemo(status));
    viz.addOnMouseOverHandler(new OnMouseOverDemo(onMouseOverAndOutStatus));
    viz.addOnMouseOutHandler(new OnMouseOutDemo(onMouseOverAndOutStatus));

    Vertical result = new Vertical();
    result.setSpacing(3);

    result.add(status);
    result.add(viz);
    result.add(onMouseOverAndOutStatus);
    
    return result;
  }
}
