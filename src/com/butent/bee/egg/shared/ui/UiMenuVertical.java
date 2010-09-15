package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;

public class UiMenuVertical extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createMenuVertical(this);
  }

}
