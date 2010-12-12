package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class VerticalSplitter extends Splitter {
  public VerticalSplitter(Widget target, Element targetContainer, boolean reverse, int size) {
    super(target, targetContainer, reverse, size);
    getElement().getStyle().setPropertyPx("height", size);
    setStyleName("bee-VSplitter");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "v-splitter");
  }
  
  @Override
  public int getAbsolutePosition() {
    return getAbsoluteTop();
  }

  @Override
  public int getEventPosition(Event event) {
    return event.getClientY();
  }

  @Override
  public int getTargetPosition() {
    return getTargetContainer().getAbsoluteTop();
  }

  @Override
  public int getTargetSize() {
    return getTargetContainer().getOffsetHeight();
  }

}
