package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.DataTable;
import com.butent.bee.egg.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.egg.client.visualization.visualizations.MotionChart;
import com.butent.bee.egg.client.visualization.visualizations.MotionChart.Options;
import com.butent.bee.egg.shared.BeeDate;

public class MotionDemo implements LeftTabPanel.WidgetProvider {
  private static final String STATE_STRING = "{"
      + "\"duration\":{\"timeUnit\":\"D\",\"multiplier\":1},\"nonSelectedAlpha\":0.4,"
      + "\"yZoomedDataMin\":300,\"yZoomedDataMax\":1200,\"iconKeySettings\":[],\"yZoomedIn\":false,"
      + "\"xZoomedDataMin\":300,\"xLambda\":1,\"time\":\"2010-01-06\",\"orderedByX\":false,\"xZoomedIn\":false,"
      + "\"uniColorForNonSelected\":false,\"sizeOption\":\"_UNISIZE\",\"iconType\":\"BUBBLE\","
      + "\"playDuration\":15000,\"dimensions\":{\"iconDimensions\":[\"dim0\"]},\"xZoomedDataMax\":1200,"
      + "\"yLambda\":1,\"yAxisOption\":\"2\",\"colorOption\":\"4\",\"showTrails\":true,\"xAxisOption\":\"2\","
      + "\"orderedByY\":false}";
  private Widget widget;

  public MotionDemo() {
    Options options = Options.create();
    options.setHeight(300);
    options.setWidth(600);
    options.setState(STATE_STRING);

    DataTable data = DataTable.create();
    data.addRows(6);
    data.addColumn(ColumnType.STRING, "Prekė");
    data.addColumn(ColumnType.DATE, "Data");
    data.addColumn(ColumnType.NUMBER, "Pardavimai");
    data.addColumn(ColumnType.NUMBER, "Sąnaudos");
    data.addColumn(ColumnType.STRING, "Miestas");
    data.setValue(0, 0, "Obuoliai");
    data.setValue(0, 2, 1000);
    data.setValue(0, 3, 300);
    data.setValue(0, 4, "Kaunas");
    data.setValue(1, 0, "Apelsinai");
    data.setValue(1, 2, 950);
    data.setValue(1, 3, 200);
    data.setValue(1, 4, "Vilnius");
    data.setValue(2, 0, "Bananai");
    data.setValue(2, 2, 300);
    data.setValue(2, 3, 250);
    data.setValue(2, 4, "Vilnius");
    data.setValue(3, 0, "Obuoliai");
    data.setValue(3, 2, 1200);
    data.setValue(3, 3, 400);
    data.setValue(3, 4, "Kaunas");
    data.setValue(4, 0, "Apelsinai");
    data.setValue(4, 2, 900);
    data.setValue(4, 3, 150);
    data.setValue(4, 4, "Vilnius");
    data.setValue(5, 0, "Bananai");
    data.setValue(5, 2, 788);
    data.setValue(5, 3, 617);
    data.setValue(5, 4, "Vilnius");

    data.setDate(0, 1, new BeeDate(2010, 1, 1));
    data.setDate(1, 1, new BeeDate(2010, 1, 1));
    data.setDate(2, 1, new BeeDate(2010, 1, 1));
    data.setDate(3, 1, new BeeDate(2010, 2, 1));
    data.setDate(4, 1, new BeeDate(2010, 2, 1));
    data.setDate(5, 1, new BeeDate(2010, 2, 1));

    widget = new MotionChart(data, options);
  }

  public Widget getWidget() {
    return widget;
  }
}
