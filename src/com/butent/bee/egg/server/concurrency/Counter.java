package com.butent.bee.egg.server.concurrency;

import com.butent.bee.egg.shared.Transformable;

public class Counter implements Transformable {
  private int counter;

  public Counter() {
    counter = 0;
  }

  public synchronized int getCounter() {
    return counter;
  }

  public synchronized int setCounter(int c) {
    counter = c;

    return counter;
  }

  public synchronized int incCounter() {
    return (++counter);
  }

  @Override
  public String transform() {
    return Integer.toString(getCounter());
  }

}
