package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.IsRow;

import java.util.List;

public class DataReceivedEvent extends GwtEvent<DataReceivedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onDataReceived(DataReceivedEvent event);
  }

  private static final GwtEvent.Type<DataReceivedEvent.Handler> TYPE = new GwtEvent.Type<>();

  public static GwtEvent.Type<DataReceivedEvent.Handler> getType() {
    return TYPE;
  }

  private final List<? extends IsRow> rows;

  public DataReceivedEvent(List<? extends IsRow> rows) {
    this.rows = rows;
  }

  @Override
  public GwtEvent.Type<DataReceivedEvent.Handler> getAssociatedType() {
    return TYPE;
  }

  public List<? extends IsRow> getRows() {
    return rows;
  }

  @Override
  protected void dispatch(DataReceivedEvent.Handler handler) {
    handler.onDataReceived(this);
  }
}
