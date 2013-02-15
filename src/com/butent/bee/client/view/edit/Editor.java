package com.butent.bee.client.view.edit;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.dialog.TabulationHandler;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.ui.EditorAction;

import java.util.List;

/**
 * Contains requirements for user interface components which are able to edit data values.
 */

public interface Editor extends IdentifiableWidget, TakesValue<String>, Focusable,
    HasAllFocusHandlers, HasKeyDownHandlers, HasEditState, HasEditStopHandlers,
    HasEnabled, HasOptions, TabulationHandler, HasValueChangeHandlers<String> {

  void clearValue();

  EditorAction getDefaultFocusAction();

  String getNormalizedValue();

  FormWidget getWidgetType();

  boolean handlesKey(int keyCode);

  boolean isNullable();

  boolean isOrHasPartner(Node node);
  
  void normalizeDisplay(String normalizedValue);

  void setNullable(boolean nullable);

  void startEdit(String oldValue, char charCode, EditorAction onEntry, Element sourceElement);

  List<String> validate(boolean checkForNull);

  List<String> validate(String normalizedValue, boolean checkForNull);
}
