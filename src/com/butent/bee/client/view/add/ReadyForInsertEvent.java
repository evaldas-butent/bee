package com.butent.bee.client.view.add;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import com.butent.bee.shared.data.BeeColumn;

import java.util.List;

/**
 * Gets handler type for ready for insertion event and registers the handler.
 */

public class ReadyForInsertEvent extends GwtEvent<ReadyForInsertEvent.Handler> {

  /**
   * Requires implementing classes to have a method for ready for insertion event.
   */

  public interface Handler extends EventHandler {
    void onReadyForInsert(ReadyForInsertEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public static Type<Handler> getType() {
    return TYPE;
  }

  private final List<BeeColumn> columns;
  private final List<String> values;

  public ReadyForInsertEvent(List<BeeColumn> columns, List<String> values) {
    super();
    this.columns = columns;
    this.values = values;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  public List<BeeColumn> getColumns() {
    return columns;
  }

  public List<String> getValues() {
    return values;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReadyForInsert(this);
  }
}
