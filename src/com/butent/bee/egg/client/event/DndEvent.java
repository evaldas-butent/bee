package com.butent.bee.egg.client.event;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Element;

import com.butent.bee.egg.shared.Assert;
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
  
  public static final String DEFAULT_DATA_FORMAT = "text/plain";
  public static final String EFFECT_MOVE = "move";

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

  public final String getDropEffect() {
    return dataTransferGetDropEffect(this);
  }

  public final String getEffectAllowed() {
    return dataTransferGetEffectAllowed(this);
  }
  
  public final Element getElement() {
    return getEventTarget().cast();
  }

  public final void setData(String value) {
    setData(DEFAULT_DATA_FORMAT, value);
  }

  public final void setData(String format, String value) {
    Assert.notEmpty(format);
    Assert.notEmpty(value);
    dataTransferSetData(this, format, value);
  }

  public final void setDropEffect(String value) {
    Assert.notEmpty(value);
    dataTransferSetDropEffect(this, value);
  }

  public final void setEffectAllowed(String value) {
    Assert.notEmpty(value);
    dataTransferSetEffectAllowed(this, value);
  }
  
  private native String dataTransferGetDropEffect(DndEvent evt) /*-{
    return evt.dataTransfer.dropEffect;
  }-*/;

  private native String dataTransferGetEffectAllowed(DndEvent evt) /*-{
    return evt.dataTransfer.effectAllowed;
  }-*/;

  private native void dataTransferSetData(DndEvent evt, String format, String value) /*-{
    evt.dataTransfer.setData(format, value);
  }-*/;
  
  private native void dataTransferSetDropEffect(DndEvent evt, String value) /*-{
    evt.dataTransfer.dropEffect = value;
  }-*/;

  private native void dataTransferSetEffectAllowed(DndEvent evt, String value) /*-{
    evt.dataTransfer.effectAllowed = value;
  }-*/;

}
