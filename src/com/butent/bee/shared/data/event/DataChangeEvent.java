package com.butent.bee.shared.data.event;

import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class DataChangeEvent extends Event<DataChangeEvent.Handler> implements DataEvent {
  
  public enum Effect {
    CANCEL, REFRESH, RESET 
  }

  public interface Handler {
    void onDataChange(DataChangeEvent event);
  }
  
  public static final Set<Effect> CANCEL_RESET_REFRESH = 
      EnumSet.of(Effect.CANCEL, Effect.REFRESH, Effect.RESET);

  private static final Type<Handler> TYPE = new Type<Handler>();
  
  public static void fire(String viewName, Collection<Effect> effects) {
    BeeKeeper.getBus().fireEvent(new DataChangeEvent(viewName, effects));
  }

  public static void fireRefresh(String viewName) {
    fire(viewName, EnumSet.of(Effect.REFRESH));
  }

  public static void fireReset(String viewName) {
    fire(viewName, EnumSet.of(Effect.REFRESH, Effect.RESET));
  }
  
  public static HandlerRegistration register(EventBus eventBus, Handler handler) {
    Assert.notNull(eventBus);
    Assert.notNull(handler);
    return eventBus.addHandler(TYPE, handler);
  }

  private final String viewName;
  private final Collection<Effect> effects;

  public DataChangeEvent(String viewName, Collection<Effect> effects) {
    this.viewName = viewName;
    this.effects = effects;
  }
  
  public boolean contains(Effect effect) {
    return effects != null && effect != null && effects.contains(effect);
  }
  
  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  public String getViewName() {
    return viewName;
  }
  
  public boolean hasCancel() {
    return contains(Effect.CANCEL);
  }

  public boolean hasRefresh() {
    return contains(Effect.REFRESH);
  }
  
  public boolean hasReset() {
    return contains(Effect.RESET);
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
