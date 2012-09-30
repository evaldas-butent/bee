package com.butent.bee.shared.data.view;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

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
  private long version;

  public RowInfo(long id, long version) {
    super();
    setId(id);
    setVersion(version);
  }

  public RowInfo(IsRow row) {
    this(row.getId(), row.getVersion());
  }

  private RowInfo() {
  }

  @Override
  public int compareTo(RowInfo o) {
    Assert.notNull(o);
    int res = Long.valueOf(getId()).compareTo(o.getId());

    if (res == 0) {
      res = Long.valueOf(getVersion()).compareTo(o.getVersion());
    }
    return res;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);
    setId(BeeUtils.toLong(arr[0]));
    setVersion(BeeUtils.toLong(arr[1]));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RowInfo)) {
      return false;
    }
    return getId() == ((RowInfo) obj).getId() && getVersion() == ((RowInfo) obj).getVersion();
  }

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Long.valueOf(getId()).hashCode();
    result = prime * result + Long.valueOf(getVersion()).hashCode();
    return result;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getId(), getVersion()});
  }

  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return BeeUtils.join(BeeConst.STRING_EMPTY, "ID=", getId(), ", VERSION=", getVersion());
  }

  public String transform() {
    return toString();
  }

  private void setId(long id) {
    Assert.isTrue(DataUtils.isId(id));
    this.id = id;
  }
}
