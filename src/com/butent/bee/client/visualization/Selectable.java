package com.butent.bee.client.visualization;

import com.google.gwt.core.client.JsArray;

import com.butent.bee.client.visualization.events.SelectHandler;

public interface Selectable {
  void addSelectHandler(SelectHandler handler);
  
  JsArray<Selection> getSelections();
  
  void setSelections(JsArray<Selection> sel);
}
