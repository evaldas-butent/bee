package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.LegendPosition;
import com.butent.bee.client.visualization.visualizations.corechart.AxisOptions;
import com.butent.bee.client.visualization.visualizations.corechart.Options;
import com.butent.bee.client.visualization.visualizations.corechart.ScatterChart;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements demonstration of a scatter chart visualization.
 */
public class ScatterDemo implements LeftTabPanel.WidgetProvider {
  public Widget getWidget() {

    Options options = Options.create();
    options.setHeight(240);
    options.setWidth(400);
    options.setLegend(LegendPosition.NONE);

    AxisOptions vAxisOptions = AxisOptions.create();
    vAxisOptions.setTitle("Pelnas");
    options.setVAxisOptions(vAxisOptions);

    AxisOptions hAxisOptions = AxisOptions.create();
    hAxisOptions.setTitle("Temperatūra");
    options.setHAxisOptions(hAxisOptions);

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.NUMBER, "Temperatūra");
    data.addColumn(ColumnType.NUMBER, "Pelnas");

    int rows = BeeUtils.randomInt(5, 20);
    int mint = -10 - BeeUtils.randomInt(1, 4) * 5;
    int step = (BeeUtils.randomInt(20, 40) - mint) / rows;
    int minp = -100;
    int maxp = 200;

    data.addRows(rows);
    for (int i = 0; i < rows; i++) {
      data.setValue(i, 0, mint + i * step);
      data.setValue(i, 1, BeeUtils.randomInt(minp, maxp));
    }

    ScatterChart viz = new ScatterChart(data, options);
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
