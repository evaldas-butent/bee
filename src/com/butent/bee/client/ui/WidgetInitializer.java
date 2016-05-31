package com.butent.bee.client.ui;

import com.google.gwt.user.client.ui.Widget;

@FunctionalInterface
public interface WidgetInitializer {
  Widget initialize(Widget widget, String name);
}
