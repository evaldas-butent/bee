package com.butent.bee.shared.data;

import com.butent.bee.shared.utils.BeeUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Handles state information storage in XML structure.
 */
@XmlRootElement(name = "BeeState", namespace = DataUtils.DEFAULT_NAMESPACE)
public class XmlState {

  @XmlAttribute
  public String name;
  @XmlAttribute
  public String mode;
  @XmlAttribute
  public boolean checked;

  private boolean safe = false;

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
}
