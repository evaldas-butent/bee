package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.Value;

import java.util.ArrayList;
import java.util.Collection;

public final class SummaryChangeEvent extends GwtEvent<SummaryChangeEvent.Handler> {

  public interface Handler extends EventHandler {
    void onSummaryChange(SummaryChangeEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static Collection<HasSummaryChangeHandlers> findSources(Widget parent) {
    Collection<HasSummaryChangeHandlers> sources = new ArrayList<>();

    if (parent instanceof HasSummaryChangeHandlers) {
      if (((HasSummaryChangeHandlers) parent).summarize()) {
        sources.add((HasSummaryChangeHandlers) parent);
      }

    } else if (parent instanceof HasOneWidget) {
      sources.addAll(findSources(((HasOneWidget) parent).getWidget()));

    } else if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        sources.addAll(findSources(child));
      }
    }

    return sources;
  }

  public static void fire(HasSummaryChangeHandlers source) {
    Assert.notNull(source);
    source.fireEvent(new SummaryChangeEvent(source.getId(), source.getSummary()));
  }

  public static void maybeFire(HasSummaryChangeHandlers source) {
    if (source != null && source.summarize()) {
      fire(source);
    }
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final String sourceId;
  private final Value value;

  private SummaryChangeEvent(String sourceId, Value value) {
    this.sourceId = sourceId;
    this.value = value;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getSourceId() {
    return sourceId;
  }

  public Value getValue() {
    return value;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSummaryChange(this);
  }
}
