package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import com.butent.bee.client.visualization.events.Handler;
import com.butent.bee.client.visualization.events.SelectHandler;
import com.butent.bee.client.visualization.visualizations.Visualization;

/**
 * Implements selection of cell, row or column in a visualization.
 */

public class Selection extends JavaScriptObject {
  public static <E extends Visualization<?>> void addSelectHandler(E viz, SelectHandler handler) {
    Handler.addHandler(viz, "select", handler);
  }

//CHECKSTYLE:OFF
  public static native Selection createCellSelection(int row, int column) /*-{
    return {
      'row' : row,
      'column' : column
    };
  }-*/;

  public static native Selection createColumnSelection(int i) /*-{
    return {
      'column' : i
    };
  }-*/;

  public static native Selection createRowSelection(int i) /*-{
    return {
      'row' : i
    };
  }-*/;

  public static final native <E extends Visualization<?>> JsArray<Selection> getSelections(E viz) /*-{
    var jso = viz.@com.butent.bee.client.visualization.visualizations.Visualization::getJso()();
    return jso.getSelection();
  }-*/;

  public static final native <E extends Visualization<?> & Selectable> void setSelections(E viz,
      JsArray<Selection> selections) /*-{
    var jso = viz.@com.butent.bee.client.visualization.visualizations.Visualization::getJso()();
    if (selections == null) {
      jso.setSelection([ {
        'row' : null,
        'column' : null
      } ]);
    } else {
      jso.setSelection(selections);
    }
  }-*/;

  public static native <E extends Visualization<?> & Selectable> void triggerSelection(E viz,
      JsArray<Selection> selections) /*-{
    var jso = viz.@com.butent.bee.client.visualization.visualizations.Visualization::getJso()();
    $wnd.google.visualization.events.trigger(jso, 'select', selections);
  }-*/;

  protected Selection() {
  }

  public final native int getColumn() /*-{
    return this.column;
  }-*/;

  public final native int getRow() /*-{
    return this.row;
  }-*/;

  public final native boolean isCell() /*-{
    return typeof this.row == 'number' && typeof this.column == 'number';
  }-*/;

  public final native boolean isColumn() /*-{
    return typeof this.row != 'number' && typeof this.column == 'number';
  }-*/;

  public final native boolean isRow() /*-{
    return typeof this.row == 'number' && typeof this.column != 'number';
  }-*/;
}