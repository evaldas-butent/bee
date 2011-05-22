package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

public class InputLong extends InputNumber {

  public InputLong() {
    super();
  }

  public InputLong(Element element) {
    super(element);
  }

  public InputLong(HasStringValue source) {
    super(source);
  }

  @Override
  public Number getNumber() {
    return BeeUtils.toLongOrNull(BeeUtils.trim(getValue()));
  }

  @Override
  protected boolean checkType(String v) {
    return BeeUtils.isLong(v);
  }

  @Override
  protected String getDefaultIdPrefix() {
    return "long";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputLong";
  }

  @Override
  protected String normalize(String v) {
    return BeeUtils.toString(BeeUtils.toLong(v));
  }
}
