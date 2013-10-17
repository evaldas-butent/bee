package com.butent.bee.client.modules.ec;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.Selectors;

public final class EcStyles {
  
  private static final String SEPARATOR = "-";
  private static final String PREFIX = "bee-ec-";

  private static final String LIST_PRICE = "bee-ec-ListPrice";
  private static final String PRICE = "bee-ec-Price";
  private static final String STOCK = "bee-ec-Stock";

  private static final String HIDDEN = "bee-ec-Hidden";
  
  public static void add(Widget widget, String style) {
    widget.addStyleName(name(style));
  }

  public static void add(Widget widget, String primary, String secondary) {
    add(widget, primary + SEPARATOR + secondary);
  }
  
  public static String getListPriceSelector() {
    return Selectors.classSelector(LIST_PRICE);
  }

  public static String getPriceSelector() {
    return Selectors.classSelector(PRICE);
  }
  
  public static String getStockSelector() {
    return Selectors.classSelector(STOCK);
  }
  
  public static String name(String style) {
    return PREFIX + style;
  }
  
  public static void markListPrice(Widget widget) {
    widget.addStyleName(LIST_PRICE);
    if (!EcKeeper.isListPriceVisible()) {
      widget.addStyleName(HIDDEN);
    }
  }

  public static void markPrice(Widget widget) {
    widget.addStyleName(PRICE);
    if (!EcKeeper.isPriceVisible()) {
      widget.addStyleName(HIDDEN);
    }
  }

  public static void markStock(Widget widget) {
    widget.addStyleName(STOCK);
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

  public static void setVisible(NodeList<Element> elements, boolean visible) {
    if (elements != null) {
      for (int i = 0; i < elements.getLength(); i++) {
        Element element = elements.getItem(i);
        if (visible) {
          element.removeClassName(HIDDEN);
        } else {
          element.addClassName(HIDDEN);
        }
      }
    }
  }

  private EcStyles() {
  }
}
