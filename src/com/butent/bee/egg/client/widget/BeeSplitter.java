package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.BeeLayoutCommand;
import com.butent.bee.egg.client.layout.BeeLayoutData;
import com.butent.bee.egg.client.layout.HasLayoutCallback;
import com.butent.bee.egg.shared.HasId;

public abstract class BeeSplitter extends Widget implements HasId, HasLayoutCallback {
  private Widget target;

  private int offset;
  private boolean mouseDown;
  private BeeLayoutCommand layoutCommand;

  private final boolean reverse;
  private int size;
  private int minSize;

  public BeeSplitter(Widget target, boolean reverse, int size) {
    this.target = target;
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

  public String getId() {
    return DomUtils.getId(this);
  }

  public int getSize() {
    return size;
  }

  public Widget getTarget() {
    return target;
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
        mouseDown = true;
        offset = getEventPosition(event) - getAbsolutePosition();
        Event.setCapture(getElement());
        event.preventDefault();
        break;

      case Event.ONMOUSEUP:
        mouseDown = false;
        Event.releaseCapture(getElement());
        event.preventDefault();
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
  
  public void setMinSize(int minSize) {
    this.minSize = minSize;
    BeeLayoutData layout = (BeeLayoutData) target.getLayoutData();
    setAssociatedWidgetSize((int) layout.size);
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setTarget(Widget target) {
    this.target = target;
  }

  protected abstract int getAbsolutePosition();

  protected abstract int getEventPosition(Event event);

  protected abstract int getTargetPosition();

  protected abstract int getTargetSize();

  private void setAssociatedWidgetSize(int size) {
    if (size < minSize) {
      size = minSize;
    }

    BeeLayoutData layout = (BeeLayoutData) target.getLayoutData();
    if (size == layout.size) {
      return;
    }

    layout.size = size;

    if (layoutCommand == null) {
      layoutCommand = new BeeLayoutCommand(this);
      DeferredCommand.addCommand(layoutCommand);
    }
  }

}
