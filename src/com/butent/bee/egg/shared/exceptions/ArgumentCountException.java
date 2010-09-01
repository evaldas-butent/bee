package com.butent.bee.egg.shared.exceptions;

@SuppressWarnings("serial")
public class ArgumentCountException extends BeeRuntimeException {

  private int cnt = -1;
  private int min = -1;
  private int max = -1;

  public ArgumentCountException() {
    super();
  }

  public ArgumentCountException(String message, Throwable cause) {
    super(message, cause);
  }

  public ArgumentCountException(String message) {
    super(message);
  }

  public ArgumentCountException(Throwable cause) {
    super(cause);
  }

  public ArgumentCountException(int cnt, int min, int max) {
    super("Count: " + cnt + "Min: " + min + ", Max: " + max);

    this.cnt = cnt;
    this.min = min;
    this.max = max;
  }

  public ArgumentCountException(int cnt, int min) {
    this(cnt, min, -1);
  }

  public int getCnt() {
    return cnt;
  }

  public void setCnt(int cnt) {
    this.cnt = cnt;
  }

  public int getMin() {
    return min;
  }

  public void setMin(int min) {
    this.min = min;
  }

  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

}
