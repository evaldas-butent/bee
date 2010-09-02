package com.butent.bee.egg.shared.ui;

import com.butent.bee.egg.shared.Assert;

public class UiHorizontalLayout extends UiComponent {

  protected UiHorizontalLayout(String id) {
    super(id);
  }

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createHorizontalLayout(this);
  }

}
