package com.butent.bee.client.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Implements value change event handling for user interface components.
 */

public class BeeValueChangeHandler<I> implements ValueChangeHandler<I> {

  public void onValueChange(ValueChangeEvent<I> event) {
    Object source = event.getSource();
    I value = event.getValue();

    if (source instanceof HasBeeValueChangeHandler) {
      extracted(source).onValueChange(value);
      return;
    }
  }

  @SuppressWarnings("unchecked")
  private HasBeeValueChangeHandler<I> extracted(Object source) {
    return ((HasBeeValueChangeHandler<I>) source);
  }
}
