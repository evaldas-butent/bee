package com.butent.bee.server.concurrency;

import com.butent.bee.shared.Transformable;

/**
 * Enables server requests counter management in order to identify them uniquely.
 */

public class Counter implements Transformable {
  private int counter;

  public Counter() {
    counter = 0;
  }

  public synchronized int getCounter() {
    return counter;
  }

  public synchronized int incCounter() {
    return (++counter);
  }

  public synchronized int setCounter(int c) {
    counter = c;
    return counter;
  }

  public String transform() {
    return Integer.toString(getCounter());
  }
}
