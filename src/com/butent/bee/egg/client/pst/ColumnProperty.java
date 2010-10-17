package com.butent.bee.egg.client.pst;

public abstract class ColumnProperty {
  /**
   * Type class used to register properties.
   */
  public abstract static class Type<P extends ColumnProperty> {
    private static int nextHashCode;
    private final int index;

    /**
     * Construct a new type.
     */
    public Type() {
      index = ++nextHashCode;
    }

    /**
     * Get the default property value of this type. This method should never
     * return null.
     * 
     * @return the default (non null) property value
     */
    public abstract P getDefault();

    @Override
    public final int hashCode() {
      return index;
    }
  }
}
