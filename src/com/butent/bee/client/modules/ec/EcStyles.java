package com.butent.bee.client.modules.ec;

import com.google.gwt.user.client.ui.Widget;

class EcStyles {
  
  private static final String PREFIX = "bee-ec-";
  private static final String SEPARATOR = "-";
  
  static void add(Widget widget, String style) {
    widget.addStyleName(name(style));
  }

  static void add(Widget widget, String primary, String secondary) {
    add(widget, primary + SEPARATOR + secondary);
  }
  
  static String name(String style) {
    return PREFIX + style;
  }
}
