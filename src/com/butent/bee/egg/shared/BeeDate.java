package com.butent.bee.egg.shared;

import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Date;

@SuppressWarnings("serial")
public class BeeDate extends Date {

  public BeeDate() {
    super();
  }

  public BeeDate(long date) {
    super(date);
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getDay() {
    return super.getDate();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getHours() {
    return super.getHours();
  }

  public int getMillis() {
    return (int) (getTime() % 1000);
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getMinutes() {
    return super.getMinutes();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getMonth() {
    return super.getMonth() + 1;
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getSeconds() {
    return super.getSeconds();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getYear() {
    return super.getYear() + 1900;
  }

  public String toLog() {
    return BeeUtils.toLeadingZeroes(getHours(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getMinutes(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getSeconds(), 2) + "."
        + BeeUtils.toLeadingZeroes(getMillis(), 3);
  }

  @Override
  public String toString() {
    return BeeUtils.toLeadingZeroes(getYear(), 4)
        + BeeUtils.toLeadingZeroes(getMonth(), 2)
        + BeeUtils.toLeadingZeroes(getDay(), 2)
        + BeeUtils.toLeadingZeroes(getHours(), 2)
        + BeeUtils.toLeadingZeroes(getMinutes(), 2)
        + BeeUtils.toLeadingZeroes(getSeconds(), 2);
  }
}
