package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class OrdEcCartItem implements BeeSerializable {
  private enum Serial {
    EC_ITEM, QUANTITY
  }

  public static OrdEcCartItem restore(String s) {
    OrdEcCartItem cartItem = new OrdEcCartItem();
    cartItem.deserialize(s);
    return cartItem;
  }

  private OrdEcItem ecItem;
  private int quantity;

  public OrdEcCartItem(OrdEcItem ecItem, int quantity) {
    this.ecItem = ecItem;
    this.quantity = quantity;
  }

  private OrdEcCartItem() {
  }

  public void add(int qty) {
    setQuantity(getQuantity() + qty);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case EC_ITEM:
          setEcItem(OrdEcItem.restore(value));
          break;

        case QUANTITY:
          setQuantity(BeeUtils.toInt(value));
          break;
      }
    }
  }

  public OrdEcItem getEcItem() {
    return ecItem;
  }

  public int getQuantity() {
    return quantity;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case EC_ITEM:
          arr[i++] = getEcItem();
          break;

        case QUANTITY:
          arr[i++] = getQuantity();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  private void setEcItem(OrdEcItem ecItem) {
    this.ecItem = ecItem;
  }
}
