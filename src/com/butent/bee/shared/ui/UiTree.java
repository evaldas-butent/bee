package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create tree user interface components for representation of data in tree
 * format.
 */

public class UiTree extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createTree(this);
  }
}
