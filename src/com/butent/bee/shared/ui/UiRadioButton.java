package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create radio button user interface components for selection of mutually
 * exclusive options.
 */

public class UiRadioButton extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createRadioButton(this);
  }
}
