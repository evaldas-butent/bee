package com.butent.bee.client.widget;

import com.butent.bee.shared.utils.BeeUtils;

public class TextLabel extends ValueLabel<String> {

  public TextLabel() {
    super(null);
  }

  @Override
  public void setValue(String value) {
    this.value = value;
    setText(BeeUtils.trimRight(value));
  }
}
