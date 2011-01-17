package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

public class UiRadioButton extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createRadioButton(this);
  }
}
