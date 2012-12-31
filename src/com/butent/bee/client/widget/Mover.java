package com.butent.bee.client.widget;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;

import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.Orientation;

public class Mover extends CustomDiv implements MoveEvent.HasMoveHandlers {

  private final Orientation orientation;

  private boolean mouseDown = false;

  private int startPosition = 0;
  private int currentPosition = 0;

  public Mover(Orientation orientation) {
    this(orientation, null);
  }

  public Mover(Orientation orientation, String styleName) {
    super(styleName);
    this.orientation = orientation;

    sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEMOVE);
  }

  @Override
  public HandlerRegistration addMoveHandler(MoveEvent.Handler handler) {
    return addHandler(handler, MoveEvent.getType());
  }

  public int getCurrentPosition() {
    return currentPosition;
  }

  @Override
  public String getIdPrefix() {
    return "mover";
  }

  public int getStartPosition() {
    return startPosition;
  }

  @Override
  public void onBrowserEvent(Event event) {
    int p = getEventPosition(event);

    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
        setMouseDown(true);

        setStartPosition(p);
        setCurrentPosition(p);

        Event.setCapture(getElement());

        event.preventDefault();
        event.stopPropagation();
        break;

      case Event.ONMOUSEUP:
        if (isMouseDown()) {
          setMouseDown(false);

          int delta = p - getCurrentPosition();
          setCurrentPosition(p);

          Event.releaseCapture(getElement());

          event.preventDefault();
          event.stopPropagation();

          MoveEvent.fireFinish(this, delta);
        }
        break;

      case Event.ONMOUSEMOVE:
        if (isMouseDown()) {
          int delta = p - getCurrentPosition();
          setCurrentPosition(p);

          event.preventDefault();
          event.stopPropagation();

          if (delta != 0) {
            MoveEvent.fireMove(this, delta);
          }
        }
        break;
    }
  }

  private int getEventPosition(Event event) {
    if (orientation == null) {
      return BeeConst.UNDEF;
    } else if (Orientation.HORIZONTAL.equals(orientation)) {
      return event.getClientX();
    } else {
      return event.getClientY();
    }
  }

  private boolean isMouseDown() {
    return mouseDown;
  }

  private void setCurrentPosition(int currentPosition) {
    this.currentPosition = currentPosition;
  }

  private void setMouseDown(boolean mouseDown) {
    this.mouseDown = mouseDown;
  }

  private void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }
}
