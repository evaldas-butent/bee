package com.butent.bee.egg.client.widget;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.LayoutCommand;
import com.butent.bee.egg.client.layout.LayoutData;
import com.butent.bee.egg.client.layout.HasLayoutCallback;
import com.butent.bee.egg.shared.HasId;

public abstract class Splitter extends Widget implements HasId, HasLayoutCallback {
  private Widget target;
  private Element targetContainer;

  private int offset;
  private boolean mouseDown;
  private LayoutCommand layoutCommand;

  private boolean reverse;
  private int size;
  private int minSize = 0;

  public Splitter(Widget target, Element targetContainer, boolean reverse, int size) {
    this.target = target;
    this.targetContainer = targetContainer;
    this.reverse = reverse;
    this.size = size;

    setElement(Document.get().createDivElement());
    sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE
        | Event.ONDBLCLICK);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "splitter");
  }

  public abstract int getAbsolutePosition();

  public abstract int getEventPosition(Event event);

  public String getId() {
    return DomUtils.getId(this);
  }

  public LayoutCommand getLayoutCommand() {
    return layoutCommand;
  }

  public int getMinSize() {
    return minSize;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return size;
  }

  public Widget getTarget() {
    return target;
  }

  public Element getTargetContainer() {
    return targetContainer;
  }

  public abstract int getTargetPosition();

  public abstract int getTargetSize();

  public boolean isMouseDown() {
    return mouseDown;
  }

  public boolean isReverse() {
    return reverse;
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
        mouseDown = true;
        offset = getEventPosition(event) - getAbsolutePosition();
        Event.setCapture(getElement());
        
        event.preventDefault();
        event.stopPropagation();
        break;

      case Event.ONMOUSEUP:
        mouseDown = false;
        Event.releaseCapture(getElement());
        
        event.preventDefault();
        event.stopPropagation();
        break;

      case Event.ONMOUSEMOVE:
        if (mouseDown) {
          int z;
          if (reverse) {
            z = getTargetPosition() + getTargetSize()
                - getEventPosition(event) - offset;
          } else {
            z = getEventPosition(event) - getTargetPosition() - offset;
          }

          setAssociatedWidgetSize(z);
          event.preventDefault();
          event.stopPropagation();
        }
        break;
    }
  }

  public void onLayout() {
    layoutCommand = null;
    Widget p = getParent();
    if (p instanceof HasLayoutCallback) {
      ((HasLayoutCallback) p).onLayout();
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  public void setLayoutCommand(LayoutCommand layoutCommand) {
    this.layoutCommand = layoutCommand;
  }

  public void setMinSize(int minSize) {
    this.minSize = minSize;
    LayoutData layout = (LayoutData) target.getLayoutData();
    setAssociatedWidgetSize((int) layout.size);
  }

  public void setMouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public void setReverse(boolean reverse) {
    this.reverse = reverse;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setTarget(Widget target) {
    this.target = target;
  }

  private void setAssociatedWidgetSize(int size) {
    if (size < minSize) {
      size = minSize;
    }

    LayoutData layout = (LayoutData) target.getLayoutData();
    if (size == layout.size) {
      return;
    }

    layout.size = size;

    if (layoutCommand == null) {
      layoutCommand = new LayoutCommand(this);
      Scheduler.get().scheduleDeferred(layoutCommand);
    }
  }

}
