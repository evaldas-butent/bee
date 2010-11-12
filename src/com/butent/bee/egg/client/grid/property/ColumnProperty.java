package com.butent.bee.egg.client.grid.property;

public abstract class ColumnProperty {
  public abstract static class Type<P extends ColumnProperty> {
    private static int nextHashCode;
    private final int index;

    public Type() {
      index = ++nextHashCode;
    }

    public abstract P getDefault();

    @Override
    public final int hashCode() {
      return index;
    }
  }
}
