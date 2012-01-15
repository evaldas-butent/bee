package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.DragEndHandler;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DragStartHandler;
import com.google.gwt.event.dom.client.DropHandler;

public interface HandlesAllDndEvents extends DragHandler, DragEndHandler, DragEnterHandler,
    DragLeaveHandler, DragOverHandler, DragStartHandler, DropHandler {
}
