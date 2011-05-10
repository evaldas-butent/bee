package com.butent.bee.shared.exceptions;

/**
 * Extends {@code BeeRuntimeException} class and is thrown when an incorrect number of parameters is
 * given to a method.
 */

@SuppressWarnings("serial")
public class ArgumentCountException extends BeeRuntimeException {

  private int cnt = -1;
  private int min = -1;
  private int max = -1;

  public ArgumentCountException() {
    super();
  }

  public ArgumentCountException(int cnt, int min) {
    this(cnt, min, -1);
  }

  public ArgumentCountException(int cnt, int min, int max) {
    super("Count: " + cnt + " Min: " + min + ", Max: " + max);

    this.cnt = cnt;
    this.min = min;
    this.max = max;
  }

  public ArgumentCountException(String message) {
    super(message);
  }

  public ArgumentCountException(String message, Throwable cause) {
    super(message, cause);
  }

  public ArgumentCountException(Throwable cause) {
    super(cause);
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

  public void setCnt(int cnt) {
    this.cnt = cnt;
  }

  public void setMax(int max) {
    this.max = max;
  }

  public void setMin(int min) {
    this.min = min;
  }

}
