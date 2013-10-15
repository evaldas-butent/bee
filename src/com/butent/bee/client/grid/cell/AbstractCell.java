package com.butent.bee.client.grid.cell;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.butent.bee.client.grid.CellContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractCell<C> {

  private Set<String> consumedEvents;

  public AbstractCell(String... consumedEvents) {
    Set<String> events = null;
    if (consumedEvents != null && consumedEvents.length > 0) {
      events = new HashSet<String>();
      for (String event : consumedEvents) {
        events.add(event);
      }
    }
    init(events);
  }

  public AbstractCell(Set<String> consumedEvents) {
    init(consumedEvents);
  }

  public Set<String> getConsumedEvents() {
    return consumedEvents;
  }

  public abstract void onBrowserEvent(CellContext context, Element parent, C value, 
      NativeEvent event);

  public abstract void render(CellContext context, C value, SafeHtmlBuilder sb);

  private void init(Set<String> events) {
    if (events != null) {
      this.consumedEvents = Collections.unmodifiableSet(events);
    }
  }
}
