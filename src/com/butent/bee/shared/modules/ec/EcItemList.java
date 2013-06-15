package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.Codec;

public class EcItemList implements BeeSerializable {
  
  public static EcItemList restore(String s) {
    EcItemList itemList = new EcItemList();
    itemList.deserialize(s);
    return itemList;
  }
  
  private SimpleRowSet rowSet;

  public EcItemList(SimpleRowSet rowSet) {
    this.rowSet = rowSet;
  }
  
  private EcItemList() {
  }
  
  @Override
  public void deserialize(String s) {
    rowSet = SimpleRowSet.restore(s);
  }

  public EcItem get(int index) {
    return (index < size()) ? new EcItem() : null;
  }
  
  public boolean isEmpty() {
    return size() <= 0;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(rowSet);
  }

  public int size() {
    return (rowSet == null) ? 0 : rowSet.getNumberOfRows();
  }
}
