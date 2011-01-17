package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

public class UiField extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createField(this);
  }
}
