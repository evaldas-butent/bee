package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumable;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasService;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

public class RowActionEvent extends Event<RowActionEvent.Handler> implements DataEvent, HasService,
    HasOptions, Consumable {

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
  private final long rowId;

  private String service;
  private String options;
  
  private boolean consumed;

  public RowActionEvent(String viewName, IsRow row, String service) {
    this(viewName, row, service, null);
  }

  public RowActionEvent(String viewName, IsRow row, String service, String options) {
    this(viewName, row, (row == null) ? BeeConst.UNDEF : row.getId(), service, options);
  }

  public RowActionEvent(String viewName, long rowId, String service) {
    this(viewName, rowId, service, null);
  }

  public RowActionEvent(String viewName, long rowId, String service, String options) {
    this(viewName, null, rowId, service, options);
  }
  
  private RowActionEvent(String viewName, IsRow row, long rowId, String service, String options) {
    this.viewName = viewName;
    this.row = row;
    this.rowId = rowId;
    this.service = service;
    this.options = options;
  }
  
  @Override
  public void consume() {
    setConsumed(true);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public String getOptions() {
    return options;
  }

  public IsRow getRow() {
    return row;
  }

  public long getRowId() {
    return rowId;
  }

  @Override
  public String getService() {
    return service;
  }
  
  @Override
  public String getViewName() {
    return viewName;
  }
  
  public boolean hasRow() {
    return row != null;
  }

  public boolean hasService(String svc) {
    return BeeUtils.same(svc, getService());
  }
  
  public boolean hasView(String view) {
    return BeeUtils.same(view, getViewName());
  }

  @Override
  public boolean isConsumed() {
    return consumed;
  }

  @Override
  public void setConsumed(boolean consumed) {
    this.consumed = consumed;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  @Override
  public void setService(String service) {
    this.service = service;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onRowAction(this);
  }
}
