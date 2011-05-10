package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create field user interface components to present database field
 * information to a user.
 */

public class UiField extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createField(this);
  }
}
