package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.HasLayoutCallback;
import com.butent.bee.client.layout.LayoutData;
import com.butent.bee.shared.HasId;

public abstract class Splitter extends Widget implements HasId {

  private static final int SENSITIVITY_MILLIS = 1;
  
  private final Widget target;
  private final Element targetContainer;

  private final boolean reverse;
  private final int size;

  private int minSize;

  private boolean mouseDown = false;
  private int offset = 0;

  private Timer timer = null;
  
  public Splitter(Widget target, Element targetContainer, boolean reverse, int size) {
    this.target = target;
    this.targetContainer = targetContainer;
    this.reverse = reverse;
    this.size = size;
    
    this.minSize = size + 1;

    setElement(Document.get().createDivElement());
    DomUtils.createId(this, getIdPrefix());

    sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE);
  }

  public abstract int getAbsolutePosition();

  public abstract int getEventPosition(Event event);

  public String getId() {
    return DomUtils.getId(this);
  }
  
  public abstract String getIdPrefix();

  public int getMinSize() {
    return minSize;
  }

  public int getSize() {
    return size;
  }

  public abstract int getTargetPosition();

  public abstract int getTargetSize();

  public boolean isReverse() {
    return reverse;
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
        setMouseDown(true);
        setOffset(getEventPosition(event) - getAbsolutePosition());
        Event.setCapture(getElement());

        event.preventDefault();
        event.stopPropagation();
        break;

      case Event.ONMOUSEUP:
        setMouseDown(false);
        Event.releaseCapture(getElement());

        event.preventDefault();
        event.stopPropagation();
        break;

      case Event.ONMOUSEMOVE:
        if (isMouseDown()) {
          int z;
          if (isReverse()) {
            z = getTargetPosition() + getTargetSize() - getEventPosition(event) - getOffset();
          } else {
            z = getEventPosition(event) - getTargetPosition() - getOffset();
          }

          setAssociatedWidgetSize(z);
          event.preventDefault();
          event.stopPropagation();
        }
        break;
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setMinSize(int minSize) {
    this.minSize = minSize;
    LayoutData layout = (LayoutData) getTarget().getLayoutData();
    setAssociatedWidgetSize((int) layout.size);
  }

  protected Element getTargetContainer() {
    return targetContainer;
  }

  private void createTimer() {
    this.timer = new Timer() {
      @Override
      public void run() {
        Widget p = getParent();
        if (p instanceof HasLayoutCallback) {
          ((HasLayoutCallback) p).onLayout();
        }
      }
    };
  }

  private int getOffset() {
    return offset;
  }

  private Widget getTarget() {
    return target;
  }

  private Timer getTimer() {
    return timer;
  }

  private boolean isMouseDown() {
    return mouseDown;
  }

  private void setAssociatedWidgetSize(int size) {
    int z = Math.max(size, minSize);

    LayoutData layout = (LayoutData) getTarget().getLayoutData();
    if (z == layout.size) {
      return;
    }
    layout.size = z;

    if (getTimer() == null) {
      createTimer();
    }
    getTimer().schedule(SENSITIVITY_MILLIS);
  }

  private void setMouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }
  
  private void setOffset(int offset) {
    this.offset = offset;
  }
}
