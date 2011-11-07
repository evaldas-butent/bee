package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Gets handler type for add start event and registers the handler.
 */

public class AddStartEvent extends GwtEvent<AddStartEvent.Handler> {

  /**
   * Requires implementing classes to have a method for add start event.
   */

  public interface Handler extends EventHandler {
    void onAddStart(AddStartEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }
  
  private final String caption;

  public AddStartEvent(String caption) {
    super();
    this.caption = caption;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public String getCaption() {
    return caption;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onAddStart(this);
  }
}
