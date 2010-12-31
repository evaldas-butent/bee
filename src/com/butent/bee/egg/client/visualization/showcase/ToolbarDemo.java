package com.butent.bee.egg.client.visualization.showcase;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.visualization.visualizations.Toolbar;
import com.butent.bee.egg.client.visualization.visualizations.Toolbar.Component;
import com.butent.bee.egg.client.visualization.visualizations.Toolbar.Type;

public class ToolbarDemo implements LeftTabPanel.WidgetProvider {
  private static final String GADGET = 
      "http://www.google.com/ig/modules/pie-chart.xml";
  private static final String DATA_SOURCE = 
      "http://spreadsheets.google.com/tq?key=tRC7Qb0eZXwKXmnnPOmNj3g&pub=1";

  public Widget getWidget() {
    Panel result = new VerticalPanel();
    PieDemo pieDemo = new PieDemo();
    Widget pieWidget = pieDemo.getWidget();
    result.add(pieWidget);
    
    Toolbar toolbar = new Toolbar();
    
    Component component = Component.create();
    component.setType(Type.HTMLCODE);
    component.setDataSource(DATA_SOURCE);
    component.setGadget(GADGET);
    toolbar.addComponent(component);

    component = Component.create();
    component.setType(Type.CSV);
    component.setDataSource(DATA_SOURCE);
    component.setGadget(GADGET);
    toolbar.addComponent(component);
    
    component = Component.create();
    component.setType(Type.HTML);
    component.setDataSource(DATA_SOURCE);
    component.setGadget(GADGET);
    toolbar.addComponent(component);    

    component = Component.create();
    component.setType(Type.IGOOGLE);
    component.setDataSource(DATA_SOURCE);
    component.setGadget(GADGET);
    toolbar.addComponent(component);
    
    result.add(toolbar);

    return result;
  }
}
