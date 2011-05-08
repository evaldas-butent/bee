package com.butent.bee.client.event;

import com.google.gwt.dom.client.NativeEvent;

public class Modifiers {
  
  public static boolean isEmpty(Modifiers m) {
    if (m == null) {
      return true;
    }
    return !m.isShiftKey() && !m.isCtrlKey() && !m.isAltKey() && !m.isMetaKey();
  }

  public static boolean isNotEmpty(Modifiers m) {
    if (m == null) {
      return false;
    }
    return m.isShiftKey() || m.isCtrlKey() || m.isAltKey() || m.isMetaKey();
  }
  
  private boolean shiftKey = false;
  private boolean ctrlKey = false;
  private boolean altKey = false;
  private boolean metaKey = false;
  
  public Modifiers(NativeEvent event) {
    if (event != null) {
      this.shiftKey = event.getShiftKey();
      this.ctrlKey = event.getCtrlKey();
      this.altKey = event.getAltKey();
      this.metaKey = event.getMetaKey();
    }
  }
  
  protected Modifiers() {
  }

  public boolean isAltKey() {
    return altKey;
  }

  public boolean isCtrlKey() {
    return ctrlKey;
  }

  public boolean isMetaKey() {
    return metaKey;
  }

  public boolean isShiftKey() {
    return shiftKey;
  }

  public void setAltKey(boolean altKey) {
    this.altKey = altKey;
  }

  public void setCtrlKey(boolean ctrlKey) {
    this.ctrlKey = ctrlKey;
  }

  public void setMetaKey(boolean metaKey) {
    this.metaKey = metaKey;
  }

  public void setShiftKey(boolean shiftKey) {
    this.shiftKey = shiftKey;
  }
}
