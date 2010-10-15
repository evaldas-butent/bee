package com.butent.bee.egg.shared.utils;

public class Grego {
  public static final int MILLIS_PER_SECOND = 1000;
  public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  public static final int MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
  public static final int MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
  
  public static final int IDX_YEAR = 0;
  public static final int IDX_MONTH = 1;
  public static final int IDX_DOM = 2;
  public static final int IDX_DOW = 3;
  public static final int IDX_DOY = 4;

  public static final int IDX_HOUR = 5;
  public static final int IDX_MINUTE = 6;
  public static final int IDX_SECOND = 7;
  public static final int IDX_MILLIS = 8;

  public static final int FIELD_COUNT = 9;

  private static final int JULIAN_1_CE = 1721426;
  private static final int JULIAN_1970_CE = 2440588;

  private static final int[] MONTH_LENGTH = new int[]{
      31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31,
      31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

  private static final int[] DAYS_BEFORE = new int[]{
      0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334,
      0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

  private static final int THURSDAY = 5;

  public static int dayOfWeek(long day) {
    long[] remainder = new long[1];
    floorDivide(day + THURSDAY, 7, remainder);
    
    int dayOfWeek = (int) remainder[0];
    dayOfWeek = (dayOfWeek == 0) ? 7 : dayOfWeek;

    return dayOfWeek;
  }

  public static int[] dayToFields(long day) {
    int[] fields = new int[FIELD_COUNT];

    day += JULIAN_1970_CE - JULIAN_1_CE;

    long[] rem = new long[1];
    long n400 = floorDivide(day, 146097, rem);
    long n100 = floorDivide(rem[0], 36524, rem);
    long n4 = floorDivide(rem[0], 1461, rem);
    long n1 = floorDivide(rem[0], 365, rem);

    int year = (int) (400 * n400 + 100 * n100 + 4 * n4 + n1);
    int dayOfYear = (int) rem[0];
    if (n100 == 4 || n1 == 4) {
      dayOfYear = 365;
    } else {
      year++;
    }

    boolean isLeap = isLeapYear(year);
    int correction = 0;
    int march1 = isLeap ? 60 : 59;
    if (dayOfYear >= march1) {
      correction = isLeap ? 1 : 2;
    }
    int month = (12 * (dayOfYear + correction) + 6) / 367;
    int dayOfMonth = dayOfYear - DAYS_BEFORE[isLeap ? month + 12 : month] + 1;
    int dayOfWeek = (int) ((day + 2) % 7);
    if (dayOfWeek < 1) {
      dayOfWeek += 7;
    }
    dayOfYear++;

    fields[IDX_YEAR] = year;
    fields[IDX_MONTH] = month + 1;
    fields[IDX_DOM] = dayOfMonth;
    fields[IDX_DOW] = dayOfWeek;
    fields[IDX_DOY] = dayOfYear;

    return fields;
  }

  public static int fieldsToDay(int year, int month, int dom) {
    int y = year - 1;
    int julian = 365 * y + floorDivide(y, 4) + (JULIAN_1_CE - 3) +
        floorDivide(y, 400) - floorDivide(y, 100) + 2 +
        DAYS_BEFORE[month + (isLeapYear(year) ? 11 : -1)] + dom;

    return julian - JULIAN_1970_CE;
  }

  public static final boolean isLeapYear(int year) {
    return ((year & 0x3) == 0) && ((year % 100 != 0) || (year % 400 == 0));
  }

  public static final int monthLength(int year, int month) {
    if (isLeapYear(year)) {
      return MONTH_LENGTH[month + 11];
    } else {
      return MONTH_LENGTH[month - 1];
    }
  }

  public static int[] timeToFields(long time) {
    long[] remainder = new long[1];

    long day = floorDivide(time, MILLIS_PER_DAY, remainder);
    int[] fields = dayToFields(day);
    
    fields[IDX_HOUR] = (int) floorDivide(remainder[0], MILLIS_PER_HOUR, remainder);
    fields[IDX_MINUTE] = (int) floorDivide(remainder[0], MILLIS_PER_MINUTE, remainder);
    fields[IDX_SECOND] = (int) floorDivide(remainder[0], MILLIS_PER_SECOND, remainder);
    fields[IDX_MILLIS] = (int) remainder[0];

    return fields;
  }

  private static int floorDivide(int numerator, int denominator) {
    return (numerator >= 0) ? numerator / denominator
        : ((numerator + 1) / denominator) - 1;
  }

  private static long floorDivide(long numerator, long denominator, long[] remainder) {
    if (numerator >= 0) {
      remainder[0] = numerator % denominator;
      return numerator / denominator;
    }
    long quotient = ((numerator + 1) / denominator) - 1;
    remainder[0] = numerator - (quotient * denominator);

    return quotient;
  }

}
