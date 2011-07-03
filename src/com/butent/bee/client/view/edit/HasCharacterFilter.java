package com.butent.bee.client.view.edit;

/**
 * Requires implementing classes to have a {@code acceptChar} method.
 */

public interface HasCharacterFilter {
  boolean acceptChar(char charCode);
}
