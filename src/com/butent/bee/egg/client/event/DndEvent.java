package com.butent.bee.egg.client.event;

import com.google.gwt.dom.client.NativeEvent;

import com.butent.bee.egg.shared.utils.BeeUtils;

public class DndEvent extends NativeEvent {
  public static enum TYPE { 
    DRAG("drag"), DRAGEND("dragend"), DRAGENTER("dragenter"), DRAGLEAVE("dragleave"),
    DRAGOVER("dragover"), DRAGSTART("dragstart"), DROP("drop");
    
    private String nativeType;

    private TYPE(String nativeType) {
      this.nativeType = nativeType;
    }
    
    public boolean isSourceEvent() {
      return this == DRAGSTART || this == DRAG || this == DRAGEND;
    }
  }

  protected DndEvent() {
  }
  
  public final TYPE getDndType() {
    String nt = getType();
    TYPE tp = null;
    
    for (TYPE z : TYPE.values()) {
      if (BeeUtils.same(nt, z.nativeType)) {
        tp = z;
        break;
      }
    }
    
    return tp;
  }
}
