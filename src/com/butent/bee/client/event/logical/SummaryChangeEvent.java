package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class SummaryChangeEvent extends GwtEvent<SummaryChangeEvent.Handler> {

  public interface Handler extends EventHandler {
    void onSummaryChange(SummaryChangeEvent event);
  }

  private static final Type<Handler> TYPE = new Type<>();

  public static HasSummaryChangeHandlers findSource(Widget parent) {
    if (parent == null) {
      return null;

    } else if (parent instanceof HasSummaryChangeHandlers) {
      return (HasSummaryChangeHandlers) parent;

    } else if (parent instanceof HasOneWidget) {
      return findSource(((HasOneWidget) parent).getWidget());

    } else if (parent instanceof HasWidgets) {
      for (Widget child : (HasWidgets) parent) {
        HasSummaryChangeHandlers found = findSource(child);
        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  public static void fire(HasSummaryChangeHandlers source, String summary) {
    source.fireEvent(new SummaryChangeEvent(summary));
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final String summary;

  public SummaryChangeEvent(String summary) {
    this.summary = summary;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getSummary() {
    return summary;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSummaryChange(this);
  }
}
