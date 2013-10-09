package com.butent.bee.shared.html.builder;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

public class BooleanAttribute extends Attribute {

  public BooleanAttribute(String name) {
    super(name);
  }

  public BooleanAttribute(String name, boolean b) {
    super(name);
    if (b) {
      setValue(name);
    }
  }
  
  public boolean getBoolean() {
    return !BeeUtils.isEmpty(getValue());
  }
  
  public void setValue(boolean b) {
    super.setValue(b ? getName() : null);
  }
  @Override
  public String write() {
    return getBoolean() ? (BeeConst.STRING_SPACE + getName()) : BeeConst.STRING_EMPTY;
  }
}
