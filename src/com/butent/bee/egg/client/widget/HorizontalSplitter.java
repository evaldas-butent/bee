package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class HorizontalSplitter extends Splitter {

  public HorizontalSplitter(Widget target, Element targetContainer, boolean reverse, int size) {
    super(target, targetContainer, reverse, size);
    getElement().getStyle().setPropertyPx("width", size);
    setStyleName("bee-HSplitter");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "h-splitter");
  }
  
  @Override
  public int getAbsolutePosition() {
    return getAbsoluteLeft();
  }

  @Override
  public int getEventPosition(Event event) {
    return event.getClientX();
  }

  @Override
  public int getTargetPosition() {
    return getTargetContainer().getAbsoluteLeft();
  }

  @Override
  public int getTargetSize() {
    return getTargetContainer().getOffsetWidth();
  }

}
