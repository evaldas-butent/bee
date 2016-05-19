package com.butent.bee.shared.modules.payroll;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public final class PayrollUtils {

  public static double calculateEarnings(Double amount, Double percent, Double... bonuses) {
    double result = BeeConst.DOUBLE_ZERO;

    if (BeeUtils.isPositive(amount)) {
      if (BeeUtils.nonZero(percent)) {
        result += BeeUtils.round(BeeUtils.plusPercent(amount, percent), 2);
      } else {
        result += amount;
      }
    }

    if (bonuses != null) {
      for (Double bonus : bonuses) {
        if (BeeUtils.nonZero(bonus)) {
          result += bonus;
        }
      }
    }

    return result;
  }

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
