package com.butent.bee.shared.exceptions;

/**
 * Extends {@code BeeRuntimeException} class and is thrown when an incorrect number of parameters is
 * given to a method.
 */

@SuppressWarnings("serial")
public class ArgumentCountException extends BeeRuntimeException {

  private final int cnt;
  private final int min;
  private final int max;

  public ArgumentCountException(int cnt, int min) {
    this(cnt, min, -1);
  }

  public ArgumentCountException(int cnt, int min, int max) {
    super("Count: " + cnt + " Min: " + min + ", Max: " + max);

    this.cnt = cnt;
    this.min = min;
    this.max = max;
  }

  public int getCnt() {
    return cnt;
  }

  public int getMax() {
    return max;
  }

  public int getMin() {
    return min;
  }
}
