package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class DataChangeEvent extends Event<DataChangeEvent.Handler> implements DataEvent {

  public interface Handler {
    void onDataChange(DataChangeEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(String viewName) {
    BeeKeeper.getBus().fireEvent(new DataChangeEvent(viewName));
  }

  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;

  public DataChangeEvent(String viewName) {
    this.viewName = viewName;
  }
  
  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public String getViewName() {
    return viewName;
  }
  
  @Override
  public boolean hasView(String view) {
    return BeeUtils.same(view, getViewName());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDataChange(this);
  }
}
