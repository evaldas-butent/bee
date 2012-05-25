package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;

import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;

public interface WidgetCallback {

  void afterCreateWidget(String name, Widget widget, WidgetDescriptionCallback callback);

  boolean beforeCreateWidget(String name, Element description);
  
  Widget createCustomWidget(String name, Element description);
}
