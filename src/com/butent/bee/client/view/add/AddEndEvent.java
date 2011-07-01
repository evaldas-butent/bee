package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Gets handler type for add end event and registers the handler.
 */

public class AddEndEvent extends GwtEvent<AddEndEvent.Handler> {

  /**
   * Requires implementing classes to have a method for add end event.
   */

  public interface Handler extends EventHandler {
    void onAddEnd(AddEndEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  public AddEndEvent() {
    super();
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAddEnd(this);
  }
}
