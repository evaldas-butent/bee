package com.butent.bee.shared.data;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Handles state information storage in XML structure.
 */
@XmlRootElement(name = "BeeState", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlState implements BeeSerializable {

  private enum SerializationMembers {
    NAME, MODE, CHECKED, SAFE
  }

  public static XmlState restore(String s) {
    XmlState state = new XmlState();
    state.deserialize(s);
    return state;
  }

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String mode;
  @XmlAttribute
  public boolean checked;

  private boolean safe = false;

  @Override
  public void deserialize(String s) {
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserializeCollection(s);

    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case NAME:
          name = value;
          break;
        case MODE:
          mode = value;
          break;
        case CHECKED:
          checked = BeeUtils.toBoolean(value);
          break;
        case SAFE:
          safe = BeeUtils.toBoolean(value);
          break;
      }
    }
  }

  public XmlState getChanges(XmlState otherState) {
    XmlState diff = null;

    if (otherState != null) {
      boolean upd = false;
      diff = new XmlState();
      diff.name = name;

      if (isProtected() || BeeUtils.equals(mode, otherState.mode)) {
        diff.mode = mode;
      } else {
        diff.mode = otherState.mode;
        upd = true;
      }
      if (isProtected() || BeeUtils.equals(checked, otherState.checked)) {
        diff.checked = checked;
      } else {
        diff.checked = otherState.checked;
        upd = true;
      }
      if (!upd) {
        diff = null;
      }
    }
    return diff;
  }

  public boolean isProtected() {
    return safe;
  }

  public void merge(XmlState otherState) {
    XmlState diff = getChanges(otherState);

    if (diff != null) {
      this.mode = diff.mode;
      this.checked = diff.checked;
    }
  }

  public XmlState protect() {
    this.safe = true;
    return this;
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : SerializationMembers.values()) {
      switch (member) {
        case NAME:
          arr[i++] = name;
          break;
        case MODE:
          arr[i++] = mode;
          break;
        case CHECKED:
          arr[i++] = checked;
          break;
        case SAFE:
          arr[i++] = safe;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }
}
