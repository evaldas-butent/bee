package com.butent.bee.shared.modules.ec;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public class DeliveryMethod implements BeeSerializable {

  private enum Serial {
    ID, NAME, NOTES
  }

  public static DeliveryMethod restore(String s) {
    DeliveryMethod deliveryMethod = new DeliveryMethod();
    deliveryMethod.deserialize(s);
    return deliveryMethod;
  }

  private long id;
  private String name;
  private String notes;

  public DeliveryMethod(long id, String name, String notes) {
    this.id = id;
    this.name = name;
    this.notes = notes;
  }

  private DeliveryMethod() {
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
        case ID:
          setId(BeeUtils.toLong(value));
          break;

        case NAME:
          setName(value);
          break;

        case NOTES:
          setNotes(value);
          break;
      }
    }
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNotes() {
    return notes;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ID:
          arr[i++] = getId();
          break;

        case NAME:
          arr[i++] = getName();
          break;

        case NOTES:
          arr[i++] = getNotes();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  private void setId(long id) {
    this.id = id;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setNotes(String notes) {
    this.notes = notes;
  }
}
