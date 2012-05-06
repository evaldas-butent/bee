package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.data.IsRow;

public class RowActionEvent extends Event<RowActionEvent.Handler> implements DataEvent, HasService,
    HasOptions {

  public interface Handler {
    void onRowAction(RowActionEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final IsRow row;

  private String service;
  private String options;

  public RowActionEvent(String viewName, IsRow row) {
    this(viewName, row, null, null);
  }

  public RowActionEvent(String viewName, IsRow row, String service) {
    this(viewName, row, service, null);
  }

  public RowActionEvent(String viewName, IsRow row, String service, String options) {
    super();
    this.viewName = viewName;
    this.row = row;
    this.service = service;
    this.options = options;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getOptions() {
    return options;
  }

  public IsRow getRow() {
    return row;
  }

  public long getRowId() {
    return getRow().getId();
  }

  public String getService() {
    return service;
  }

  public String getViewName() {
    return viewName;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public void setService(String service) {
    this.service = service;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRowAction(this);
  }
}
