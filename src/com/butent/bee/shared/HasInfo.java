package com.butent.bee.shared;

import com.butent.bee.shared.utils.Property;

import java.util.List;

/**
 * Requires any implementing classes to have getInfo method.
 */

public interface HasInfo {
  List<Property> getInfo();
}
