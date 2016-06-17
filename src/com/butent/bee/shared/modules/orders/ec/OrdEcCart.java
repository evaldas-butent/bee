package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OrdEcCart implements BeeSerializable {
  private enum Serial {
    COMMENT, ITEMS
  }

  public static OrdEcCart restore(String s) {
    OrdEcCart cart = new OrdEcCart();
    cart.deserialize(s);
    return cart;
  }

  private String comment;

  private final List<OrdEcCartItem> items = new ArrayList<>();

  public OrdEcCart() {
    super();
  }

  public OrdEcCartItem add(OrdEcItem ecItem, int quantity) {
    if (ecItem != null && quantity > 0) {
      OrdEcCartItem item = getItem(ecItem.getId());

      if (item == null) {
        item = new OrdEcCartItem(ecItem, quantity);
        items.add(item);
      } else {
        item.add(quantity);
      }
      return item;

    } else {
      return null;
    }
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

        case COMMENT:
          setComment(value);
          break;

        case ITEMS:
          items.clear();
          String[] itemArr = Codec.beeDeserializeCollection(value);
          if (itemArr != null) {
            for (String it : itemArr) {
              items.add(OrdEcCartItem.restore(it));
            }
          }
          break;
      }
    }
  }

  public String getComment() {
    return comment;
  }

  public List<OrdEcCartItem> getItems() {
    return items;
  }

  public String getCaption() {
    return Localized.dictionary().ecShoppingCart();
  }

  public int getQuantity(long itemId) {
    OrdEcCartItem item = getItem(itemId);
    return (item == null) ? 0 : item.getQuantity();
  }

  public boolean isEmpty() {
    return items.isEmpty();
  }

  public boolean remove(OrdEcItem ecItem) {
    for (Iterator<OrdEcCartItem> it = items.iterator(); it.hasNext();) {
      OrdEcCartItem item = it.next();

      if (item.getEcItem().equals(ecItem)) {
        it.remove();
        return true;
      }
    }
    return false;
  }

  public void reset() {
    setComment(null);
    items.clear();
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {

        case COMMENT:
          arr[i++] = getComment();
          break;

        case ITEMS:
          arr[i++] = items;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int totalCents() {
    int total = 0;
    for (OrdEcCartItem item : items) {
      total += item.getQuantity() * item.getEcItem().getPrice();
    }
    return total;
  }

  public int totalQuantity() {
    int total = 0;
    for (OrdEcCartItem item : items) {
      total += item.getQuantity();
    }
    return total;
  }

  private OrdEcCartItem getItem(long itemId) {
    for (OrdEcCartItem item : items) {
      if (item.getEcItem().getId() == itemId) {
        return item;
      }
    }
    return null;
  }
}