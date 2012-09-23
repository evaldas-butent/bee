/*
 * Copyright 2009 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.butent.bee.client.dnd.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dnd.util.impl.DOMUtilImpl;

/**
 * Provides DOM utility methods.
 */
public class DOMUtil {

  /**
   * Whether or not debugging is enabled.
   */
  public static final boolean DEBUG = false;

  private static DOMUtilImpl impl;

  static {
    impl = (DOMUtilImpl) GWT.create(DOMUtilImpl.class);
  }

  /**
   * Cancel all currently selected region(s) on the current page.
   */
  public static void cancelAllDocumentSelections() {
    impl.cancelAllDocumentSelections();
  }

  /**
   * Set a widget's border style for debugging purposes.
   * @param widget the widget to color
   * @param color the desired border color
   */
  public static void debugWidgetWithColor(Widget widget, String color) {
    if (DEBUG) {
      widget.getElement().getStyle().setProperty("border", "2px solid " + color);
    }
  }

  /**
   * Set an element's location as fast as possible, avoiding some of the overhead in
   * {@link com.google.gwt.user.client.ui.AbsolutePanel#setWidgetPosition(Widget, int, int)} .
   * 
   * @param elem the element's whose position is to be modified
   * @param left the left pixel offset
   * @param top the top pixel offset
   */
  public static void fastSetElementPosition(Element elem, int left, int top) {
    elem.getStyle().setPropertyPx("left", left);
    elem.getStyle().setPropertyPx("top", top);
  }

  /**
   * Gets an element's CSS based 'border-left-width' in pixels or <code>0</code> (zero) when the
   * element is hidden.
   * 
   * @param elem the element to be measured
   * @return the width of the left CSS border in pixels
   */
  public static int getBorderLeft(Element elem) {
    return impl.getBorderLeft(elem);
  }

  /**
   * Gets an element's CSS based 'border-top-widget' in pixels or <code>0</code> (zero) when the
   * element is hidden.
   * 
   * @param elem the element to be measured
   * @return the width of the top CSS border in pixels
   */
  public static int getBorderTop(Element elem) {
    return impl.getBorderTop(elem);
  }

  /**
   * Gets an element's client height in pixels or <code>0</code> (zero) when the element is hidden.
   * This is equal to offset height minus the top and bottom CSS borders.
   * 
   * @param elem the element to be measured
   * @return the element's client height in pixels
   */
  public static int getClientHeight(Element elem) {
    return impl.getClientHeight(elem);
  }

  /**
   * Gets an element's client widget in pixels or <code>0</code> (zero) when the element is hidden.
   * This is equal to offset width minus the left and right CSS borders.
   * 
   * @param elem the element to be measured
   * @return the element's client width in pixels
   */
  public static int getClientWidth(Element elem) {
    return impl.getClientWidth(elem);
  }

  public static String getEffectiveStyle(Element elem, String styleName) {
    return impl.getEffectiveStyle(elem, styleName);
  }

  /**
   * Gets the sum of an element's left and right CSS borders in pixels.
   * 
   * @param widget the widget to be measured
   * @return the total border width in pixels
   */
  public static int getHorizontalBorders(Widget widget) {
    return impl.getHorizontalBorders(widget);
  }

  /**
   * Gets the sum of an element's top and bottom CSS borders in pixels.
   * 
   * @param widget the widget to be measured
   * @return the total border height in pixels
   */
  public static int getVerticalBorders(Widget widget) {
    return impl.getVerticalBorders(widget);
  }

  /**
   * Report a fatal exception via <code>Window.alert()</code> than throw a <code>RuntimeException</code>.
   * @param msg the message to report
   * @throws RuntimeException a new exception based on the provided message
   */
  public static void reportFatalAndThrowRuntimeException(String msg) throws RuntimeException {
    msg = "gwt-dnd warning: " + msg;
    Window.alert(msg);
    throw new RuntimeException(msg);
  }
}
