package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.List;

public class OrdEcOrder implements BeeSerializable {

  private enum Serial {
    ORDER_ID, DATE, STATUS, MANAGER, COMMENT, ITEMS
  }

  public static OrdEcOrder restore(String s) {
    OrdEcOrder order = new OrdEcOrder();
    order.deserialize(s);
    return order;
  }

  private long orderId;
  private DateTime date;

  private int status;
  private String manager;

  private String comment;

  private final List<OrdEcOrderItem> items = new ArrayList<>();

  public OrdEcOrder() {
    super();
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (members[i]) {
        case ORDER_ID:
          setOrderId(BeeUtils.toLong(value));
          break;

        case DATE:
          setDate(DateTime.restore(value));
          break;

        case STATUS:
          setStatus(BeeUtils.toInt(value));
          break;

        case MANAGER:
          setManager(value);
          break;

        case COMMENT:
          setComment(value);
          break;

        case ITEMS:
          items.clear();
          String[] itemArr = Codec.beeDeserializeCollection(value);
          if (itemArr != null) {
            for (String it : itemArr) {
              items.add(OrdEcOrderItem.restore(it));
            }
          }
          break;
      }
    }
  }

  public double getAmount() {
    double total = 0;
    for (OrdEcOrderItem item : items) {
      total += item.getAmount();
    }
    return total;
  }

  public String getComment() {
    return comment;
  }

  public DateTime getDate() {
    return date;
  }

  public List<OrdEcOrderItem> getItems() {
    return items;
  }

  public String getManager() {
    return manager;
  }

  public long getOrderId() {
    return orderId;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ORDER_ID:
          arr[i++] = getOrderId();
          break;

        case DATE:
          arr[i++] = getDate();
          break;

        case STATUS:
          arr[i++] = getStatus();
          break;

        case MANAGER:
          arr[i++] = getManager();
          break;

        case COMMENT:
          arr[i++] = getComment();
          break;

        case ITEMS:
          arr[i++] = getItems();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setManager(String manager) {
    this.manager = manager;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}