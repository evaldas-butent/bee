package com.butent.bee.shared.html.builder.elements;

import com.butent.bee.shared.html.builder.Node;
import com.butent.bee.shared.utils.BeeUtils;

public class Text extends Node {

  public Text(String text) {
    super(text);
  }

  public Text(int text) {
    super(Integer.toString(text));
  }

  public Text(long text) {
    super(Long.toString(text));
  }

  public Text(double text) {
    super(BeeUtils.toString(text));
  }

  public Text(boolean text) {
    super(Boolean.toString(text));
  }

  @Override
  public String write() {
    return getTag();
  }
}
