package com.butent.bee.shared.modules.payroll;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Objects;

public class Earnings implements BeeSerializable {

  private enum Serial {
    EMPLOYEE_ID, SUBSTITUTE_FOR, OBJECT_ID, DATE_FROM, DATE_UNTIL, FUND, WAGE,
    PLANNED_DAYS, PLANNED_MILLIS, ACTUAL_DAYS, ACTUAL_MILLIS, HOLY_DAYS, HOLY_MILLIS
  }

  public static Earnings restore(String s) {
    Earnings earnings = new Earnings();
    earnings.deserialize(s);
    return earnings;
  }

  private static double amount(double hourly, long millis) {
    return BeeUtils.round(hourly * millis / TimeUtils.MILLIS_PER_HOUR, AMOUNT_PRECISION);
  }

  public static final int FUND_PRECISION = 2;
  private static final double FUND_FACTOR = Math.pow(10, FUND_PRECISION);

  public static final int WAGE_PRECISION = 2;
  private static final double WAGE_FACTOR = Math.pow(10, WAGE_PRECISION);

  public static final int AMOUNT_PRECISION = 2;

  private Long employeeId;
  private Long substituteFor;
  private Long objectId;

  private JustDate dateFrom;
  private JustDate dateUntil;

  private Integer fund;
  private Integer wage;

  private Integer plannedDays;
  private Long plannedMillis;

  private Integer actualDays;
  private Long actualMillis;

  private Integer holyDays;
  private Long holyMillis;

  private Earnings() {
  }

  public Earnings(Long employeeId, Long substituteFor, Long objectId) {
    this.employeeId = employeeId;
    this.substituteFor = substituteFor;
    this.objectId = objectId;
  }

  public Double amountForHolidays() {
    if (BeeUtils.isPositive(getHolyMillis())) {
      Double w = computeWage();

      if (BeeUtils.isPositive(w)) {
        return amount(w, getHolyMillis());
      }
    }
    return null;
  }

  public Double amountWithoutHolidays() {
    if (BeeUtils.isPositive(getActualMillis())) {
      if (BeeUtils.isPositive(getWage())) {
        return amount(getHourlyWage(), getActualMillis());

      } else if (BeeUtils.isPositive(getFund())) {
        if (Objects.equals(getActualMillis(), getPlannedMillis())) {
          return BeeUtils.round(getSalaryFund(), AMOUNT_PRECISION);

        } else {
          Double w = computeWage();
          if (BeeUtils.isPositive(w)) {
            return amount(w, getActualMillis());
          }
        }
      }
    }
    return null;
  }

  public Double computeWage() {
    if (BeeUtils.isPositive(getWage())) {
      return getHourlyWage();

    } else if (BeeUtils.isPositive(getFund()) && BeeUtils.isPositive(getPlannedMillis())) {
      return BeeUtils.round(getSalaryFund() * TimeUtils.MILLIS_PER_HOUR / getPlannedMillis(),
          WAGE_PRECISION);

    } else {
      return null;
    }
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, Serial.values().length);

    for (int i = 0; i < arr.length; i++) {
      String value = arr[i];

      if (!BeeUtils.isEmpty(value)) {
        switch (Serial.values()[i]) {
          case EMPLOYEE_ID:
            setEmployeeId(BeeUtils.toLongOrNull(value));
            break;
          case SUBSTITUTE_FOR:
            setSubstituteFor(BeeUtils.toLongOrNull(value));
            break;
          case OBJECT_ID:
            setObjectId(BeeUtils.toLongOrNull(value));
            break;

          case DATE_FROM:
            setDateFrom(TimeUtils.toDateOrNull(value));
            break;
          case DATE_UNTIL:
            setDateUntil(TimeUtils.toDateOrNull(value));
            break;

          case FUND:
            setFund(BeeUtils.toIntOrNull(value));
            break;
          case WAGE:
            setWage(BeeUtils.toIntOrNull(value));
            break;

          case PLANNED_DAYS:
            setPlannedDays(BeeUtils.toIntOrNull(value));
            break;
          case PLANNED_MILLIS:
            setPlannedMillis(BeeUtils.toLongOrNull(value));
            break;

          case ACTUAL_DAYS:
            setActualDays(BeeUtils.toIntOrNull(value));
            break;
          case ACTUAL_MILLIS:
            setActualMillis(BeeUtils.toLongOrNull(value));
            break;

          case HOLY_DAYS:
            setHolyDays(BeeUtils.toIntOrNull(value));
            break;
          case HOLY_MILLIS:
            setHolyMillis(BeeUtils.toLongOrNull(value));
            break;
        }
      }
    }
  }

  @Override
  public String serialize() {
    Object[] arr = new Object[Serial.values().length];
    int i = 0;

    for (Serial member : Serial.values()) {
      switch (member) {
        case EMPLOYEE_ID:
          arr[i++] = getEmployeeId();
          break;
        case SUBSTITUTE_FOR:
          arr[i++] = getSubstituteFor();
          break;
        case OBJECT_ID:
          arr[i++] = getObjectId();
          break;

        case DATE_FROM:
          arr[i++] = getDateFrom();
          break;
        case DATE_UNTIL:
          arr[i++] = getDateUntil();
          break;

        case FUND:
          arr[i++] = getFund();
          break;
        case WAGE:
          arr[i++] = getWage();
          break;

        case PLANNED_DAYS:
          arr[i++] = getPlannedDays();
          break;
        case PLANNED_MILLIS:
          arr[i++] = getPlannedMillis();
          break;

        case ACTUAL_DAYS:
          arr[i++] = getActualDays();
          break;
        case ACTUAL_MILLIS:
          arr[i++] = getActualMillis();
          break;

        case HOLY_DAYS:
          arr[i++] = getHolyDays();
          break;
        case HOLY_MILLIS:
          arr[i++] = getHolyMillis();
          break;
      }
    }

    return Codec.beeSerialize(arr);
  }

  public Long getEmployeeId() {
    return employeeId;
  }

  public Long getSubstituteFor() {
    return substituteFor;
  }

  public Long getObjectId() {
    return objectId;
  }

  public JustDate getDateFrom() {
    return dateFrom;
  }

  public JustDate getDateUntil() {
    return dateUntil;
  }

  public Double getSalaryFund() {
    if (BeeUtils.isPositive(getFund())) {
      return getFund() / FUND_FACTOR;
    } else {
      return null;
    }
  }

  private Integer getFund() {
    return fund;
  }

  public Double getHourlyWage() {
    if (BeeUtils.isPositive(getWage())) {
      return getWage() / WAGE_FACTOR;
    } else {
      return null;
    }
  }

  private Integer getWage() {
    return wage;
  }

  public Integer getPlannedDays() {
    return plannedDays;
  }

  public Long getPlannedMillis() {
    return plannedMillis;
  }

  public Integer getActualDays() {
    return actualDays;
  }

  public Long getActualMillis() {
    return actualMillis;
  }

  public Integer getHolyDays() {
    return holyDays;
  }

  public Long getHolyMillis() {
    return holyMillis;
  }

  public boolean isSubstitution() {
    return DataUtils.isId(getSubstituteFor()) && DataUtils.isId(getEmployeeId())
        && !Objects.equals(getEmployeeId(), getSubstituteFor());
  }

  private void setEmployeeId(Long employeeId) {
    this.employeeId = employeeId;
  }

  private void setSubstituteFor(Long substituteFor) {
    this.substituteFor = substituteFor;
  }

  private void setObjectId(Long objectId) {
    this.objectId = objectId;
  }

  public void setDateFrom(JustDate dateFrom) {
    this.dateFrom = dateFrom;
  }

  public void setDateUntil(JustDate dateUntil) {
    this.dateUntil = dateUntil;
  }

  public void setSalaryFund(Double value) {
    if (BeeUtils.isPositive(value)) {
      setFund(BeeUtils.round(value * FUND_FACTOR));
    } else {
      setFund(null);
    }
  }

  private void setFund(Integer fund) {
    this.fund = fund;
  }

  public void setHourlyWage(Double value) {
    if (BeeUtils.isPositive(value)) {
      setWage(BeeUtils.round(value * WAGE_FACTOR));
    } else {
      setWage(null);
    }
  }

  private void setWage(Integer wage) {
    this.wage = wage;
  }

  public void setPlannedDays(Integer plannedDays) {
    this.plannedDays = plannedDays;
  }

  public void setPlannedMillis(Long plannedMillis) {
    this.plannedMillis = plannedMillis;
  }

  public void setActualDays(Integer actualDays) {
    this.actualDays = actualDays;
  }

  public void setActualMillis(Long actualMillis) {
    this.actualMillis = actualMillis;
  }

  public void setHolyDays(Integer holyDays) {
    this.holyDays = holyDays;
  }

  public void setHolyMillis(Long holyMillis) {
    this.holyMillis = holyMillis;
  }

  public Double total() {
    Double x = amountWithoutHolidays();
    Double y = amountForHolidays();

    if (BeeUtils.isPositive(x) || BeeUtils.isPositive(y)) {
      return BeeUtils.positive(x, BeeConst.DOUBLE_ZERO)
          + BeeUtils.positive(y, BeeConst.DOUBLE_ZERO);

    } else {
      return null;
    }
  }
}
