package com.butent.bee.server.concurrency;

/**
 * Enables server requests counter management in order to identify them uniquely.
 */

public class Counter {
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

  @Override
  public String toString() {
    return Integer.toString(getCounter());
  }
}
