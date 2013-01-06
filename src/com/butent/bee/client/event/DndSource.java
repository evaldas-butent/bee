package com.butent.bee.client.event;

import com.google.gwt.event.dom.client.HasDragEndHandlers;
import com.google.gwt.event.dom.client.HasDragHandlers;
import com.google.gwt.event.dom.client.HasDragStartHandlers;

import com.butent.bee.client.ui.IdentifiableWidget;

public interface DndSource extends HasDragStartHandlers, HasDragHandlers, HasDragEndHandlers,
    IdentifiableWidget {
}
