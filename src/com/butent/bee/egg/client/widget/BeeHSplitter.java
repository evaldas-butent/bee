package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class BeeHSplitter extends BeeSplitter {

  public BeeHSplitter(Widget target, boolean reverse, int size) {
    super(target, reverse, size);
    getElement().getStyle().setPropertyPx("width", size);
    setStyleName("bee-HSplitter");
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "h-splitter");
  }
  
  @Override
  protected int getAbsolutePosition() {
    return getAbsoluteLeft();
  }

  @Override
  protected int getEventPosition(Event event) {
    return event.getClientX();
  }

  @Override
  protected int getTargetPosition() {
    return getTarget().getAbsoluteLeft();
  }

  @Override
  protected int getTargetSize() {
    return getTarget().getOffsetWidth();
  }

}
