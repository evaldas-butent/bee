package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.shared.HasId;

/**
 * Determines which classes have to be extended for boolean widget implementing classes.
 */

public interface BooleanWidget extends Focusable, TakesValue<Boolean>, HasId, HasClickHandlers,
    HasEnabled {
}
