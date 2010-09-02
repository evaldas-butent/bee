package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;

public class UiWindow extends UiComponent {

  protected UiWindow(String id) {
    super(id);
  }

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createWindow(this);
  }
}
