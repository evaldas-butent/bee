package com.butent.bee.client.view.edit;

import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

import com.butent.bee.shared.HasId;
import com.butent.bee.shared.ui.EditorAction;

/**
 * Contains requirements for user interface components which are able to edit data values.
 */

public interface Editor extends HasId, IsWidget, HasValue<String>, Focusable, HasBlurHandlers,
    HasKeyDownHandlers, HasEditState, HasEditStopHandlers, HasEnabled {

  String getNormalizedValue();
  
  boolean handlesKey(int keyCode);

  boolean isNullable();

  void setNullable(boolean nullable);

  void startEdit(String oldValue, char charCode, EditorAction onEntry);

  String validate();
}
