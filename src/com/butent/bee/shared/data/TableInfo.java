package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class TableInfo implements BeeSerializable, Comparable<TableInfo> {
  public static TableInfo restore(String s) {
    Assert.notEmpty(s);
    TableInfo ti = new TableInfo();
    ti.deserialize(s);
    return ti;
  }

  private String name;

  private int rowCount;

  public TableInfo(String name, int rowCount) {
    this.name = name;
    this.rowCount = rowCount;
  }

  private TableInfo() {
  }

  public int compareTo(TableInfo o) {
    if (o == null) {
      return BeeConst.COMPARE_MORE;
    }
    return BeeUtils.compareNormalized(getName(), o.getName());
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.arrayLength(arr, 2);

    name = arr[0];
    setRowCount(BeeUtils.toInt(arr[1]));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TableInfo)) {
      return false;
    }
    return BeeUtils.same(getName(), ((TableInfo) obj).getName());
  }

  public String getName() {
    return name;
  }

  public int getRowCount() {
    return rowCount;
  }

  @Override
  public int hashCode() {
    return BeeUtils.normalize(getName()).hashCode();
  }

  public String serialize() {
    return Codec.beeSerializeAll(getName(), getRowCount());
  }

  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }
}
