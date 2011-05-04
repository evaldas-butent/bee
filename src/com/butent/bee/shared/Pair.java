package com.butent.bee.shared;

import com.butent.bee.shared.utils.BeeUtils;
/**
 * Defines pairs of objects
 * 
 * @param <A> type of first object to pair
 * @param <B> type of second object to pair
 */
public class Pair<A, B> implements Transformable {
  private final A a;
  private final B b;

  /**
   * Creates the new {@code Pair} object passing the pair of objects.  
   * @param a object to pair the object {@code b}
   * @param b object to pair the object {@code a}
   */
  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  /**
   * Returns the first object or value of object.
   * @return the first object or value of object.
   */
  public A getA() {
    return a;
  }

  /**
   * Returns the second object or value of object.
   * @return the second object or value of object.
   */
  public B getB() {
    return b;
  }

  /**
   * Converts pair of objects to {@code String}
   * @return {@code String} of objects pair
   */
  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR, a, b);
  }

  /**
   * Equivalents method {@link Pair#toString()}
   * @see {@link Pair#toString()}
   */
  public String transform() {
    return toString();
  }
}
