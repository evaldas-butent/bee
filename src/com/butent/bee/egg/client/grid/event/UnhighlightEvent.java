package com.butent.bee.egg.client.grid.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class UnhighlightEvent<V> extends GwtEvent<UnhighlightHandler<V>> {
  private static Type<UnhighlightHandler<?>> TYPE;

  public static <V, S extends HasUnhighlightHandlers<V> & HasHandlers> void fire(
      S source, V value) {
    if (TYPE != null) {
      UnhighlightEvent<V> event = new UnhighlightEvent<V>(value);
      source.fireEvent(event);
    }
  }

  public static Type<UnhighlightHandler<?>> getType() {
    if (TYPE == null) {
      TYPE = new Type<UnhighlightHandler<?>>();
    }
    return TYPE;
  }
  
  private V value;

  public UnhighlightEvent(V value) {
    this.value = value;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Type<UnhighlightHandler<V>> getAssociatedType() {
    return (Type) TYPE;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  @Override
  protected void dispatch(UnhighlightHandler<V> handler) {
    handler.onUnhighlight(this);
  }
 
}
