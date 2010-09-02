package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;

public class UiLabel extends UiComponent {

  protected UiLabel(String id) {
    super(id);
  }

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createLabel(this);
  }
}
