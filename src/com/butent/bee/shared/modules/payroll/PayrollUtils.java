package com.butent.bee.shared.modules.payroll;

import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public final class PayrollUtils {

  public static long getMillis(String from, String until, String duration) {
    Long time = TimeUtils.parseTime(duration);
    if (BeeUtils.isNonNegative(time)) {
      return time;
    }

    Long lower = TimeUtils.parseTime(from);
    if (BeeUtils.isNonNegative(lower)) {
      Long upper = TimeUtils.parseTime(until);
      if (BeeUtils.isLess(lower, upper)) {
        return upper - lower;
      }
    }

    return 0L;
  }

  private PayrollUtils() {
  }
}
