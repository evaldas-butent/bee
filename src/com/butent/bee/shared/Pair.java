package com.butent.bee.shared;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

import com.butent.bee.shared.utils.BeeUtils;

/**
 * Defines pairs of objects.
 * 
 * @param <A> type of first object to pair
 * @param <B> type of second object to pair
 */
public class Pair<A, B> {

  public static final Splitter SPLITTER =
      Splitter.on(CharMatcher.anyOf(" ,;=")).trimResults().omitEmptyStrings().limit(2);
  
  /**
   * Creates the new {@code Pair} object passing the pair of objects.
   * 
   * @param a object to pair the object {@code b}
   * @param b object to pair the object {@code a}
   */
  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<A, B>(a, b);
  }
  
  public static Pair<String, String> split(String input) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }
    
    String a = null;
    String b = null;
    
    for (String s : SPLITTER.split(input)) {
      if (a == null) {
        a = s;
      } else {
        b = s;
      }
    }
    return Pair.of(a, b);
  }

  private A a;
  private B b;

  private Pair(A a, B b) {
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

  public void setA(A a) {
    this.a = a;
  }

  public void setB(B b) {
    this.b = b;
  }

  /**
   * Converts pair of objects to {@code String}.
   * 
   * @return {@code String} of objects pair
   */
  @Override
  public String toString() {
    return BeeUtils.join(BeeConst.STRING_COMMA, a, b);
  }
}
