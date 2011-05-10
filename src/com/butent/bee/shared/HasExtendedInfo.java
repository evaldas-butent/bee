package com.butent.bee.shared;

import com.butent.bee.shared.utils.ExtendedProperty;

import java.util.List;

/**
 * Requires any implementing classes to be able to get extended properties list.
 */

public interface HasExtendedInfo {
  List<ExtendedProperty> getInfo();

}
