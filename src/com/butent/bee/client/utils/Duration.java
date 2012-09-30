package com.butent.bee.client.utils;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables system to measure time consumption of specific processes.
 */

public class Duration {

  private double start = BeeConst.UNDEF;
  private double end = BeeConst.UNDEF;

  private int timeout = BeeConst.UNDEF;
  private int completed = BeeConst.UNDEF;

  private String message = null;

  public Duration() {
    start = JsUtils.currentTimeMillis();
  }

  public Duration(int timeout) {
    this();
    this.timeout = timeout;
  }

  public Duration(String message) {
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
    return (BeeConst.isUndef(completed));
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
    return BeeUtils.joinWords(getMessage(), getStartTime(),
        BeeUtils.bracket(getCompletedTime()));
  }

  @Override
  public String toString() {
    return BeeUtils.joinWords(getMessage(), getStartTime(), getEndTime(),
        BeeUtils.bracket(getCompletedTime()));
  }

  private void clearCompleted() {
    completed = BeeConst.UNDEF;
  }

  private void clearEnd() {
    end = BeeConst.UNDEF;
  }

  private void clearTimeout() {
    timeout = BeeConst.UNDEF;
  }
}
