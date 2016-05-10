package com.butent.bee.shared.modules.payroll;

import com.google.common.collect.Range;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.StringList;

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

  public static Filter getIntersectionFilter(YearMonth ym, String col1, String col2) {
    if (ym == null || BeeUtils.anyEmpty(col1, col2) || BeeUtils.same(col1, col2)) {
      return Filter.isFalse();

    } else {
      StringList columns = StringList.of(col1, col2);
      Range<Value> range = Range.closed(new DateValue(ym.getDate()), new DateValue(ym.getLast()));

      return Filter.anyIntersects(columns, range);
    }
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
