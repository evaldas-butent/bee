package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Focusable;

import com.butent.bee.shared.HasId;

public interface BooleanWidget extends Focusable, TakesValue<Boolean>, HasId, HasClickHandlers {
}
