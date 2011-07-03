package com.butent.bee.shared;

import java.util.Collection;

/**
 * Determines implementators to have a method to set value for items collection.
 */

public interface HasItems {
  void setItems(Collection<String> items);
}
