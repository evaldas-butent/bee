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
@XmlRootElement(name = "State", namespace = DataUtils.STATE_NAMESPACE)
public class XmlState implements BeeSerializable {

  private enum Serial {
    NAME, USER_MODE, ROLE_MODE, CHECKED, SAFE
  }

  public static XmlState restore(String s) {
    XmlState state = new XmlState();
    state.deserialize(s);
    return state;
  }

  @XmlAttribute
  public String name;
  @XmlAttribute
  public boolean userMode;
  @XmlAttribute
  public boolean roleMode;
  @XmlAttribute
  public boolean checked;

  private boolean safe = false;

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
          name = value;
          break;
        case USER_MODE:
          userMode = BeeUtils.toBoolean(value);
          break;
        case ROLE_MODE:
          roleMode = BeeUtils.toBoolean(value);
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

      upd = upd || !BeeUtils.equals(userMode, otherState.userMode);
      diff.userMode = otherState.userMode;

      upd = upd || !BeeUtils.equals(roleMode, otherState.roleMode);
      diff.roleMode = otherState.roleMode;

      upd = upd || !BeeUtils.equals(checked, otherState.checked);
      diff.checked = otherState.checked;

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
      this.userMode = diff.userMode;
      this.roleMode = diff.roleMode;
      this.checked = diff.checked;
    }
  }

  public XmlState protect() {
    this.safe = true;
    return this;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case NAME:
          arr[i++] = name;
          break;
        case USER_MODE:
          arr[i++] = userMode;
          break;
        case ROLE_MODE:
          arr[i++] = roleMode;
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
