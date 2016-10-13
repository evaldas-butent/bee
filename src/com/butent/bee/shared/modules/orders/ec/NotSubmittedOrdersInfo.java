package com.butent.bee.shared.modules.orders.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.Codec;

public class NotSubmittedOrdersInfo implements BeeSerializable {

  private enum Serial {
    NAME, DATE, COMMENT
  }

  public static NotSubmittedOrdersInfo restore(String s) {
    NotSubmittedOrdersInfo nsoi = new NotSubmittedOrdersInfo();
    nsoi.deserialize(s);
    return nsoi;
  }

  private String name;
  private String comment;
  private DateTime date;

  public NotSubmittedOrdersInfo() {
    super();
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
        case NAME:
          setName(value);
          break;

        case DATE:
          setDate(DateTime.restore(value));
          break;

        case COMMENT:
          setComment(value);
          break;
      }
    }
  }

  public String getName() {
    return name;
  }

  public DateTime getDate() {
    return date;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case NAME:
          arr[i++] = getName();
          break;

        case DATE:
          arr[i++] = getDate();
          break;

        case COMMENT:
          arr[i++] = getComment();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}