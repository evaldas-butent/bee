package com.butent.bee.egg.shared;

import java.util.Date;

import com.butent.bee.egg.shared.utils.BeeUtils;

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
  public int getYear() {
    return super.getYear() + 1900;
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getMonth() {
    // TODO Auto-generated method stub
    return super.getMonth() + 1;
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getDay() {
    // TODO Auto-generated method stub
    return super.getDate();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getHours() {
    // TODO Auto-generated method stub
    return super.getHours();
  }

  public int getMillis() {
    return (int) (getTime() % 1000);
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getMinutes() {
    // TODO Auto-generated method stub
    return super.getMinutes();
  }

  @SuppressWarnings("deprecation")
  @Override
  public int getSeconds() {
    // TODO Auto-generated method stub
    return super.getSeconds();
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

  public String toLog() {
    return BeeUtils.toLeadingZeroes(getHours(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getMinutes(), 2) + ":"
        + BeeUtils.toLeadingZeroes(getSeconds(), 2) + "."
        + BeeUtils.toLeadingZeroes(getMillis(), 3);
  }
}
