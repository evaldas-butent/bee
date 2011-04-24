package com.butent.bee.client.view.grid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Widget;

import java.util.HashSet;
import java.util.Set;

abstract class CellBasedWidgetImpl {

  private static CellBasedWidgetImpl impl;

  public static CellBasedWidgetImpl get() {
    if (impl == null) {
      impl = GWT.create(CellBasedWidgetImpl.class);
    }
    return impl;
  }

  final Set<String> focusableTypes;

  CellBasedWidgetImpl() {
    focusableTypes = new HashSet<String>();
    focusableTypes.add("select");
    focusableTypes.add("input");
    focusableTypes.add("textarea");
    focusableTypes.add("option");
    focusableTypes.add("button");
    focusableTypes.add("label");
  }

  public boolean isFocusable(Element elem) {
    return focusableTypes.contains(elem.getTagName().toLowerCase()) || elem.getTabIndex() >= 0;
  }

  public abstract void onBrowserEvent(Widget widget, Event event);

  public SafeHtml processHtml(SafeHtml html) {
    return html;
  }

  public void resetFocus(ScheduledCommand command) {
    command.execute();
  }

  public final void sinkEvents(Widget widget, Set<String> typeNames) {
    if (typeNames == null) {
      return;
    }

    int eventsToSink = 0;
    for (String typeName : typeNames) {
      int typeInt = sinkEvent(widget, typeName);
      if (typeInt > 0) {
        eventsToSink |= typeInt;
      }
    }
    if (eventsToSink > 0) {
      widget.sinkEvents(eventsToSink);
    }
  }

  protected int sinkEvent(@SuppressWarnings("unused") Widget widget, String typeName) {
    return Event.getTypeInt(typeName);
  }
}
