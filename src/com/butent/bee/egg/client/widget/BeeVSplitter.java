package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class BeeVSplitter extends BeeSplitter {
  public BeeVSplitter(Widget target, boolean reverse, int size) {
    super(target, reverse, size);
    getElement().getStyle().setPropertyPx("height", size);
    setStyleName("bee-VSplitter");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "v-splitter");
  }
  
  @Override
  protected int getAbsolutePosition() {
    return getAbsoluteTop();
  }

  @Override
  protected int getEventPosition(Event event) {
    return event.getClientY();
  }

  @Override
  protected int getTargetPosition() {
    return getTarget().getAbsoluteTop();
  }

  @Override
  protected int getTargetSize() {
    return getTarget().getOffsetHeight();
  }

}
