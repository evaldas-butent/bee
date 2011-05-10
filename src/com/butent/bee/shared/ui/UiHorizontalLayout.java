package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create horizontal layout interface components.
 */

public class UiHorizontalLayout extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createHorizontalLayout(this);
  }
}
