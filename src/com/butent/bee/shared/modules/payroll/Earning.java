package com.butent.bee.shared.modules.payroll;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class Earning {

  private final Collection<Integer> days;
  private final long millis;

  private final double amount;

  public Earning(Collection<Integer> days, long millis, double amount) {
    this.days = days;
    this.millis = millis;
    this.amount = amount;
  }

  public void appplyTo(IsRow row) {
    Assert.notNull(row);

    if (getNumberOfDays() > 0) {
      row.setProperty(PRP_EARNINGS_NUMBER_OF_DAYS, getNumberOfDays());
    }

    if (getMillis() > 0) {
      row.setProperty(PRP_EARNINGS_MILLIS, getMillis());
      row.setProperty(PRP_EARNINGS_DURATION, getDuration());

      if (getAmount() > 0) {
        row.setProperty(PRP_EARNINGS_AMOUNT, BeeUtils.round(getAmount(), 2));
        row.setProperty(PRP_EARNINGS_HOURLY_WAGE, BeeUtils.round(getHourlyWage(), 5));
      }
    }
  }

  public double getAmount() {
    return amount;
  }

  public Collection<Integer> getDays() {
    return days;
  }

  public String getDuration() {
    if (getMillis() > 0) {
      return TimeUtils.renderTime(getMillis(), false);
    } else {
      return null;
    }
  }

  public double getHourlyWage() {
    if (getMillis() > 0) {
      return getAmount() * TimeUtils.MILLIS_PER_HOUR / getMillis();
    } else {
      return BeeConst.DOUBLE_ZERO;
    }
  }

  public long getMillis() {
    return millis;
  }

  public int getNumberOfDays() {
    return BeeUtils.size(getDays());
  }
}
