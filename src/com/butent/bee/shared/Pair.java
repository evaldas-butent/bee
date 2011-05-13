package com.butent.bee.shared;

import com.google.common.base.Objects;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Defines pairs of objects.
 * 
 * @param <A> type of first object to pair
 * @param <B> type of second object to pair
 */
public class Pair<A, B> implements Transformable {
  private final A a;
  private final B b;

  /**
   * Creates the new {@code Pair} object passing the pair of objects.
   * 
   * @param a object to pair the object {@code b}
   * @param b object to pair the object {@code a}
   */
  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pair)) {
      return false;
    }
    return Objects.equal(getA(), ((Pair<?, ?>) obj).getA())
        && Objects.equal(getB(), ((Pair<?, ?>) obj).getB());
  }

  /**
   * Returns the first object or value of object.
   * 
   * @return the first object or value of object.
   */
  public A getA() {
    return a;
  }

  /**
   * Returns the second object or value of object.
   * 
   * @return the second object or value of object.
   */
  public B getB() {
    return b;
  }

  @Override
  public int hashCode() {
    return 1 + Objects.hashCode(getA(), getB());
  }

  /**
   * Converts pair of objects to {@code String}.
   * 
   * @return {@code String} of objects pair
   */
  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR, a, b);
  }

  /**
   * Equivalents method {@link Pair#toString()}.
   * 
   * @see {@link Pair#toString()}
   */
  public String transform() {
    return toString();
  }
}
