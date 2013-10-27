package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class EcOrderEvent implements BeeSerializable {

  private enum Serial {
    DATE, STATUS, USER
  }

  public static EcOrderEvent restore(String s) {
    EcOrderEvent event = new EcOrderEvent();
    event.deserialize(s);
    return event;
  }

  private DateTime date;
  private int status;
  private String user;

  public EcOrderEvent(DateTime date, int status, String user) {
    this.date = date;
    this.status = status;
    this.user = user;
  }

  private EcOrderEvent() {
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
        case DATE:
          setDate(DateTime.restore(value));
          break;

        case STATUS:
          setStatus(BeeUtils.toInt(value));
          break;

        case USER:
          setUser(value);
          break;
      }
    }
  }

  public DateTime getDate() {
    return date;
  }

  public int getStatus() {
    return status;
  }

  public String getUser() {
    return user;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case DATE:
          arr[i++] = getDate();
          break;

        case STATUS:
          arr[i++] = getStatus();
          break;

        case USER:
          arr[i++] = getUser();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setDate(DateTime date) {
    this.date = date;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public void setUser(String user) {
    this.user = user;
  }
}
