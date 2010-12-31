package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.AbstractDrawOptions;
import com.butent.bee.egg.client.visualization.CommonChartOptions;
import com.butent.bee.egg.client.visualization.DataTable;
import com.butent.bee.egg.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.egg.client.visualization.visualizations.Visualization;

public class MoneyDemo implements LeftTabPanel.WidgetProvider {
  private Widget widget;

  public MoneyDemo() {
    CommonChartOptions options = CommonChartOptions.create();
    options.setWidth(300);
    options.setHeight(200);
    options.setTitle("Pelno paskirstymas");

    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING, "Label");
    data.addColumn(ColumnType.NUMBER, "Value");
    data.addRows(4);
    data.setValue(0, 0, "Alus");
    data.setValue(1, 0, "Merginos");
    data.setValue(2, 0, "Poilsis");
    data.setValue(3, 0, "Investicijos");
    data.setCell(0, 1, 10, "10,000", null);
    data.setCell(1, 1, 30, "30,000", null);
    data.setCell(2, 1, 20, "20,000", null);
    data.setCell(3, 1, 7.5, "7,500", null);

    widget = new Visualization<AbstractDrawOptions>(data, options) {
      @Override
      protected native JavaScriptObject createJso(Element div) /*-{
        return new $wnd.PilesOfMoney(div);
      }-*/;
    };
  }

  public Widget getWidget() {
    return widget;
  }
}
