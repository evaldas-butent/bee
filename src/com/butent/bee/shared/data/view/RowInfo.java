package com.butent.bee.shared.data.view;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class RowInfo implements BeeSerializable, Comparable<RowInfo> {

  public static RowInfo restore(String s) {
    Assert.notEmpty(s);
    RowInfo rowInfo = new RowInfo();
    rowInfo.deserialize(s);
    return rowInfo;
  }

  private long id;
  private long version;

  private boolean editable;
  
  public RowInfo(IsRow row, boolean editable) {
    this(row.getId(), row.getVersion(), editable);
  }

  public RowInfo(long id, long version, boolean editable) {
    super();

    this.id = id;
    this.version = version;
    this.editable = editable;
  }

  private RowInfo() {
  }

  @Override
  public int compareTo(RowInfo o) {
    Assert.notNull(o);
    int res = Long.valueOf(getId()).compareTo(o.getId());

    if (res == BeeConst.COMPARE_EQUAL) {
      res = Long.valueOf(getVersion()).compareTo(o.getVersion());
      if (res == BeeConst.COMPARE_EQUAL) {
        res = Boolean.valueOf(isEditable()).compareTo(o.isEditable());
      }
    }
    return res;
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 3);

    setId(BeeUtils.toLong(arr[0]));
    setVersion(BeeUtils.toLong(arr[1]));
    setEditable(Codec.unpack(arr[2]));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RowInfo)) {
      return false;
    }
    return getId() == ((RowInfo) obj).getId() 
        && getVersion() == ((RowInfo) obj).getVersion()
        && isEditable() == ((RowInfo) obj).isEditable();
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
    result = prime * result + Boolean.valueOf(isEditable()).hashCode();
    return result;
  }

  public boolean isEditable() {
    return editable;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(new Object[] {getId(), getVersion(), Codec.pack(isEditable())});
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  @Override
  public String toString() {
    return BeeUtils.joinWords("id:", getId(), "version:", getVersion(), "editable:", isEditable());
  }

  private void setId(long id) {
    this.id = id;
  }
}
