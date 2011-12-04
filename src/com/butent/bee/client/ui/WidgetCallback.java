package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

public interface WidgetCallback {

  void afterCreateWidget(String name, Widget widget);

  boolean beforeCreateWidget(String name, Element description);
  
  Widget createCustomWidget(String name, Element description);
}
