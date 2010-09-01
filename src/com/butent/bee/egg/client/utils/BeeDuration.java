package com.butent.bee.egg.client.utils;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeDuration {
  private double start = BeeConst.TIME_UNKNOWN;
  private double end = BeeConst.TIME_UNKNOWN;

  private int timeout = BeeConst.TIME_UNKNOWN;
  private int completed = BeeConst.TIME_UNKNOWN;

  private String message = null;

  public BeeDuration() {
    start = BeeJs.currentTimeMillis();
  }

  public BeeDuration(int timeout) {
    this();
    this.timeout = timeout;
  }

  public BeeDuration(String message) {
    this();
    this.message = message;
  }

  public double getStart() {
    return start;
  }

  public double getEnd() {
    return end;
  }

  public void setEndMillis() {
    end = BeeJs.currentTimeMillis();
    completed = BeeJs.toInt(end - getStart());
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public int getCompleted() {
    return completed;
  }

  public void setCompleted(int completed) {
    this.completed = completed;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int finish() {
    setEndMillis();
    return completed;
  }

  public boolean isFinished() {
    return completed >= 0;
  }

  public boolean isPending() {
    return (completed == BeeConst.TIME_UNKNOWN);
  }

  public String getStartTime() {
    return BeeJs.toTime(getStart());
  }

  public String getEndTime() {
    if (isFinished())
      return BeeJs.toTime(getEnd());
    else
      return BeeConst.STRING_EMPTY;
  }

  public String getCompletedTime() {
    if (completed > 0)
      return BeeJs.toSeconds(completed);
    else
      return BeeConst.STRING_EMPTY;
  }

  public String getTimeoutAsTime() {
    if (isPending() && timeout > 0)
      return BeeJs.toSeconds(timeout);
    else
      return BeeConst.STRING_EMPTY;
  }

  public String getExpireTime() {
    if (isPending() && timeout > 0)
      return BeeJs.toTime(getStart() + timeout);
    else
      return BeeConst.STRING_EMPTY;
  }

  private void clearEnd() {
    end = BeeConst.TIME_UNKNOWN;
  }

  private void clearCompleted() {
    completed = BeeConst.TIME_UNKNOWN;
  }

  private void clearTimeout() {
    timeout = BeeConst.TIME_UNKNOWN;
  }

  public void restart(String msg) {
    clearEnd();
    clearCompleted();
    clearTimeout();

    setMessage(msg);

    start = BeeJs.currentTimeMillis();
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, getMessage(), getStartTime(), getEndTime(),
        getCompletedTime());
  }

}
