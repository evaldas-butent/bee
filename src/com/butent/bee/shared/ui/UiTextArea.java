package com.butent.bee.shared.ui;

import com.butent.bee.shared.Assert;

/**
 * Enables to dynamically create text area user interface components for representation of more than
 * one text line.
 */

public class UiTextArea extends UiComponent {

  @Override
  public Object createInstance(UiCreator creator) {
    Assert.notEmpty(creator);
    return creator.createTextArea(this);
  }
}
