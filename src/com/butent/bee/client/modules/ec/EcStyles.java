package com.butent.bee.client.modules.ec;

import com.google.gwt.user.client.ui.Widget;

public final class EcStyles {
  
  private static final String SEPARATOR = "-";
  private static final String PREFIX = "bee-ec-";
  
  public static void add(Widget widget, String style) {
    widget.addStyleName(name(style));
  }

  public static void add(Widget widget, String primary, String secondary) {
    add(widget, primary + SEPARATOR + secondary);
  }
  
  public static String name(String style) {
    return PREFIX + style;
  }

  public static String name(String primary, String secondary) {
    return name(primary + SEPARATOR + secondary);
  }
  
  public static void remove(Widget widget, String style) {
    widget.removeStyleName(name(style));
  }

  public static void remove(Widget widget, String primary, String secondary) {
    remove(widget, primary + SEPARATOR + secondary);
  }

  private EcStyles() {
  }
}
