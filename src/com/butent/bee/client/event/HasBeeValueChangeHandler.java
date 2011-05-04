package com.butent.bee.client.event;

/**
 * Requires implementing classes to have a method for handling value change events in user interface
 * components.
 */

public interface HasBeeValueChangeHandler<I> {
  boolean onValueChange(I value);

}
