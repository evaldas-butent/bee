package com.butent.bee.client.event.logical;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

import com.butent.bee.shared.ui.Action;

import java.util.EnumSet;
import java.util.Set;

public class ActionEvent extends GwtEvent<ActionEvent.Handler> {

  public interface HasActionHandlers extends HasHandlers {
    HandlerRegistration addActionHandler(Handler handler);
  }

  public interface Handler extends EventHandler {
    void onAction(ActionEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final Set<Action> actions;

  public ActionEvent(Set<Action> actions) {
    super();
    this.actions = actions;
  }

  public ActionEvent(Action action) {
    this(EnumSet.of(action));
  }

  public boolean contains(Action action) {
    return getActions().contains(action);
  }

  public Set<Action> getActions() {
    return actions;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAction(this);
  }
}
