package com.butent.bee.client.layout;

import com.google.gwt.event.dom.client.DragEndEvent;
import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartEvent;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasAllDragAndDropHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.HasIndexedWidgets;
import com.butent.bee.shared.HasId;

/**
 * Implements a panel that formats its child widgets using the default HTML layout behavior.
 */

public class Flow extends FlowPanel implements HasId, HasAllDragAndDropHandlers, HasIndexedWidgets {

  public Flow() {
    DomUtils.createId(this, getIdPrefix());
  }

  public HandlerRegistration addDragEndHandler(DragEndHandler handler) {
    return addBitlessDomHandler(handler, DragEndEvent.getType());
  }

  public HandlerRegistration addDragEnterHandler(DragEnterHandler handler) {
    return addBitlessDomHandler(handler, DragEnterEvent.getType());
  }

  public HandlerRegistration addDragHandler(DragHandler handler) {
    return addBitlessDomHandler(handler, DragEvent.getType());
  }

  public HandlerRegistration addDragLeaveHandler(DragLeaveHandler handler) {
    return addBitlessDomHandler(handler, DragLeaveEvent.getType());
  }

  public HandlerRegistration addDragOverHandler(DragOverHandler handler) {
    return addBitlessDomHandler(handler, DragOverEvent.getType());
  }

  public HandlerRegistration addDragStartHandler(DragStartHandler handler) {
    return addBitlessDomHandler(handler, DragStartEvent.getType());
  }

  public HandlerRegistration addDropHandler(DropHandler handler) {
    return addBitlessDomHandler(handler, DropEvent.getType());
  }
  
  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "flow";
  }
  
  @Override
  public void onAttach() {
    super.onAttach();
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
