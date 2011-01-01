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
    start = JsUtils.currentTimeMillis();
  }

  public BeeDuration(int timeout) {
    this();
    this.timeout = timeout;
  }

  public BeeDuration(String message) {
    this();
    this.message = message;
  }

  public int finish() {
    setEndMillis();
    return completed;
  }

  public int getCompleted() {
    return completed;
  }

  public String getCompletedTime() {
    if (completed > 0) {
      return JsUtils.toSeconds(completed);
    } else {
      return BeeConst.STRING_ZERO;
    }
  }

  public double getEnd() {
    return end;
  }

  public String getEndTime() {
    if (isFinished()) {
      return JsUtils.toTime(getEnd());
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getExpireTime() {
    if (isPending() && timeout > 0) {
      return JsUtils.toTime(getStart() + timeout);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getMessage() {
    return message;
  }

  public double getStart() {
    return start;
  }

  public String getStartTime() {
    return JsUtils.toTime(getStart());
  }

  public int getTimeout() {
    return timeout;
  }

  public String getTimeoutAsTime() {
    if (isPending() && timeout > 0) {
      return JsUtils.toSeconds(timeout);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public boolean isFinished() {
    return completed >= 0;
  }

  public boolean isPending() {
    return (completed == BeeConst.TIME_UNKNOWN);
  }

  public void restart(String msg) {
    clearEnd();
    clearCompleted();
    clearTimeout();

    setMessage(msg);

    start = JsUtils.currentTimeMillis();
  }

  public void setCompleted(int completed) {
    this.completed = completed;
  }

  public void setEndMillis() {
    end = JsUtils.currentTimeMillis();
    completed = JsUtils.toInt(end - getStart());
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String toLog() {
    return BeeUtils.concat(1, getMessage(), getStartTime(),
        BeeUtils.bracket(getCompletedTime()));
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, getMessage(), getStartTime(), getEndTime(),
        BeeUtils.bracket(getCompletedTime()));
  }

  private void clearCompleted() {
    completed = BeeConst.TIME_UNKNOWN;
  }

  private void clearEnd() {
    end = BeeConst.TIME_UNKNOWN;
  }

  private void clearTimeout() {
    timeout = BeeConst.TIME_UNKNOWN;
  }

}
