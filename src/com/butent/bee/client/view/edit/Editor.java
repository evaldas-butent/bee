package com.butent.bee.client.view.edit;

import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.shared.HasId;

public interface Editor extends HasId, IsWidget, HasValue<String>, Focusable, HasBlurHandlers,
    HasKeyDownHandlers {
  
  String getNormalizedValue();

  boolean isNullable();
  
  void setNullable(boolean nullable);

  void startEdit(String oldValue, char charCode);
  
  boolean validate();
}
