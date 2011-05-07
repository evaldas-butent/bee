package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JsArray;

import com.butent.bee.client.visualization.events.SelectHandler;

/**
 * Requires implementing classes to have select event handlers and be able to set selections.
 */

public interface Selectable {
  void addSelectHandler(SelectHandler handler);

  JsArray<Selection> getSelections();

  void setSelections(JsArray<Selection> sel);
}
