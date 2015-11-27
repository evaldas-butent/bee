package com.butent.bee.shared.modules.payroll;

import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

public class WorkScheduleSummary {

  private long millis;
  private final Set<String> timeCardCodes = new HashSet<>();

  public WorkScheduleSummary() {
  }

  public void addMillis(long time) {
    if (time > 0) {
      millis += time;
    }
  }

  public void addTimeCardCode(String code) {
    if (!BeeUtils.isEmpty(code)) {
      timeCardCodes.add(code.trim());
    }
  }

  public String getDuration() {
    if (getMillis() > 0) {
      return TimeUtils.renderTime(getMillis(), false);
    } else {
      return null;
    }
  }

  public long getMillis() {
    return millis;
  }

  public Set<String> getTimeCardCodes() {
    return timeCardCodes;
  }

  public boolean hasTimeCardCodes() {
    return !timeCardCodes.isEmpty();
  }
}
