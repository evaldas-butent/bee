package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class EcItemList implements BeeSerializable {

  public static EcItemList restore(String s) {
    EcItemList itemList = new EcItemList();
    itemList.deserialize(s);
    return itemList;
  }

  private final List<EcItem> items = Lists.newArrayList();

  public EcItemList(List<EcItem> items) {
    this.items.clear();
    this.items.addAll(items);
  }

  private EcItemList() {
  }

  @Override
  public void deserialize(String s) {
    items.clear();

    for (String item : Codec.beeDeserializeCollection(s)) {
      items.add(EcItem.restore(item));
    }
  }

  public EcItem get(int index) {
    return (index < size()) ? items.get(index) : null;
  }

  public boolean isEmpty() {
    return size() <= 0;
  }

  @Override
  public String serialize() {
    return Codec.beeSerialize(items);
  }

  public int size() {
    return items.size();
  }
}
