package com.butent.bee.shared.data.view;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements operations with data row - serialization, comparison, transformations.
 */

public class RowInfo implements BeeSerializable, Comparable<RowInfo>, Transformable {

  public static RowInfo restore(String s) {
    Assert.notEmpty(s);
    RowInfo rowInfo = new RowInfo();
    rowInfo.deserialize(s);
    return rowInfo;
  }

  private long id;

  public RowInfo(long id) {
    super();
    this.id = id;
  }

  private RowInfo() {
  }

  @Override
  public int compareTo(RowInfo o) {
    Assert.notNull(o);
    return Long.valueOf(getId()).compareTo(o.getId());
  }

  @Override
  public void deserialize(String s) {
    Assert.notEmpty(s);
    long x = BeeUtils.toLong(s);
    Assert.isTrue(x != 0);
    setId(x);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RowInfo)) {
      return false;
    }
    return getId() == ((RowInfo) obj).getId();
  }

  public long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Long.valueOf(getId()).hashCode();
  }

  @Override
  public String serialize() {
    return Long.toString(getId());
  }

  @Override
  public String toString() {
    return BeeUtils.toString(getId());
  }

  public String transform() {
    return toString();
  }

  private void setId(long id) {
    this.id = id;
  }
}
