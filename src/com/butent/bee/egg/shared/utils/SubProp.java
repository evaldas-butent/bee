package com.butent.bee.egg.shared.utils;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;

public class SubProp extends StringProp {
  private String sub;
  private BeeDate date = new BeeDate();

  public SubProp() {
    super();
  }

  public SubProp(String name) {
    super(name);
    this.sub = BeeConst.STRING_EMPTY;
  }

  public SubProp(String name, String value) {
    super(name, value);
    this.sub = BeeConst.STRING_EMPTY;
  }

  public SubProp(String name, String sub, String value) {
    super(name, value);
    this.sub = sub;
  }

  public String getSub() {
    return sub;
  }

  public void setSub(String sub) {
    this.sub = sub;
  }

  public BeeDate getDate() {
    return date;
  }

  public void setDate(BeeDate date) {
    this.date = date;
  }

  @Override
  public boolean equals(Object obj) {
    boolean ok = super.equals(obj);

    if (!ok || !(obj instanceof SubProp))
      return false;

    String z = ((SubProp) obj).getSub();
    if (sub == null)
      ok = (z == null);
    else if (z == null)
      ok = false;
    else
      ok = sub.equals(z);

    return ok;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_VALUE_SEPARATOR, BeeUtils.concat(
        BeeConst.DEFAULT_PROPERTY_SEPARATOR, getName(), getSub()), BeeUtils
        .transform(getValue()));
  }

  public String transform() {
    return toString();
  }

}
