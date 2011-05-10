package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create menu user interface components with horizontal layout.
 */

public class UiMenuHorizontal extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createMenuHorizontal(this);
  }
}
