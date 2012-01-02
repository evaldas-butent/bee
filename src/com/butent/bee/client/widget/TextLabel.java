package com.butent.bee.client.widget;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables using text label user interface component.
 */

public class TextLabel extends ValueLabel<String> {

  public TextLabel(boolean inline) {
    super(null, inline);
  }

  @Override
  public void setValue(String value) {
    this.value = value;
    setText(BeeUtils.trimRight(value));
  }
}
