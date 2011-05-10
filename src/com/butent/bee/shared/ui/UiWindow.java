package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create windows interface components which are used for storing other
 * components in.
 */

public class UiWindow extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createWindow(this);
  }
}
