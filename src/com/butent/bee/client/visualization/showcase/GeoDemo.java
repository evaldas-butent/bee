package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.visualization.DataTable;
import com.butent.bee.client.visualization.AbstractDataTable.ColumnType;
import com.butent.bee.client.visualization.visualizations.GeoMap;
import com.butent.bee.client.visualization.visualizations.GeoMap.Options;
import com.butent.bee.shared.utils.BeeUtils;

public class GeoDemo implements LeftTabPanel.WidgetProvider {
  public Widget getWidget() {
    final Options options = Options.create();
    options.setHeight(300);
    options.setWidth(450);

    options.setDataMode(GeoMap.DataMode.MARKERS);
    options.setRegion("LT");

    final DataTable dataTable = DataTable.create();      
    dataTable.addColumn(ColumnType.NUMBER, "Latitude");
    dataTable.addColumn(ColumnType.NUMBER, "Longitude");
    dataTable.addColumn(ColumnType.NUMBER, "Klientai");
    dataTable.addColumn(ColumnType.STRING, "Miestas");
    dataTable.addRows(10);
    
    int min = 20;
    int max = 1000;
    
    dataTable.setValue(0, 0, 54.4);
    dataTable.setValue(0, 1, 24.05);
    dataTable.setValue(0, 3, "Alytus");
    dataTable.setValue(1, 0, 54.9);
    dataTable.setValue(1, 1, 23.933333);
    dataTable.setValue(1, 3, "Kaunas");
    dataTable.setValue(2, 0, 55.7);
    dataTable.setValue(2, 1, 21.133333);
    dataTable.setValue(2, 3, "Klaipėda");
    dataTable.setValue(3, 0, 54.566667);
    dataTable.setValue(3, 1, 23.35);
    dataTable.setValue(3, 3, "Marijampolė");
    dataTable.setValue(4, 0, 55.733333);
    dataTable.setValue(4, 1, 24.35);
    dataTable.setValue(4, 3, "Panevėžys");
    dataTable.setValue(5, 0, 55.933333);
    dataTable.setValue(5, 1, 23.316667);
    dataTable.setValue(5, 3, "Šiauliai");
    dataTable.setValue(6, 0, 55.25);
    dataTable.setValue(6, 1, 22.283333);
    dataTable.setValue(6, 3, "Tauragė");
    dataTable.setValue(7, 0, 55.983333);
    dataTable.setValue(7, 1, 22.25);
    dataTable.setValue(7, 3, "Tel�?iai");
    dataTable.setValue(8, 0, 55.5);
    dataTable.setValue(8, 1, 25.6);
    dataTable.setValue(8, 3, "Utena");
    dataTable.setValue(9, 0, 54.683333);
    dataTable.setValue(9, 1, 25.283333);
    dataTable.setValue(9, 3, "Vilnius");
    
    for (int i = 0; i < 10; i++) {
      dataTable.setValue(i, 2, BeeUtils.randomInt(min, max)); 
    }
    
    final GeoMap geo = new GeoMap(dataTable, options);
    return geo;
  }
}
