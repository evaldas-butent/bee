package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.utils.BeeUtils;

public class Pair<A, B> implements Transformable {
  private final A a;
  private final B b;

  public Pair(A a, B b) {
    this.a = a;
    this.b = b;
  }

  public A getA() {
    return a;
  }

  public B getB() {
    return b;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_LIST_SEPARATOR, a, b);
  }

  public String transform() {
    return toString();
  }

}
