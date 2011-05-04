package com.butent.bee.client.event;

import com.google.gwt.user.client.ui.HasWidgets;

/**
 * Requires implementing classes to have a method to handle an event after adding an item to it.
 */

public interface HasAfterAddHandler {
  void onAfterAdd(HasWidgets parent);
}
