package com.butent.bee.client.event;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * Requires implementing classes to have a method to handle an event before adding an item to it.
 */

public interface HasBeforeAddHandler {
  Widget onBeforeAdd(HasWidgets parent);
}
