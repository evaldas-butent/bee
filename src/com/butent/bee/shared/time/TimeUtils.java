package com.butent.bee.shared.time;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.RangeOptions;

import java.util.Date;

/**
 * Contains methods for date/time calculations.
 */
public class TimeUtils {

  public static final int FIELD_ERA = 0;

  public static final int FIELD_YEAR = 1;
  public static final int FIELD_MONTH = 2;
  public static final int FIELD_WEEK_OF_YEAR = 3;
  public static final int FIELD_WEEK_OF_MONTH = 4;
  public static final int FIELD_DATE = 5;
  public static final int FIELD_DAY_OF_MONTH = 5;
  public static final int FIELD_DAY_OF_YEAR = 6;
  public static final int FIELD_DAY_OF_WEEK = 7;
  public static final int FIELD_DAY_OF_WEEK_IN_MONTH = 8;

  public static final int FIELD_AM_PM = 9;
  public static final int FIELD_HOUR = 10;
  public static final int FIELD_HOUR_OF_DAY = 11;
  public static final int FIELD_MINUTE = 12;
  public static final int FIELD_SECOND = 13;
  public static final int FIELD_MILLISECOND = 14;

  public static final int FIELD_ZONE_OFFSET = 15;
  public static final int FIELD_DST_OFFSET = 16;

  public static final int FIELD_YEAR_WOY = 17;

  public static final int FIELD_DOW_LOCAL = 18;

  public static final int FIELD_EXTENDED_YEAR = 19;
  public static final int FIELD_JULIAN_DAY = 20;

  public static final int FIELD_MILLISECONDS_IN_DAY = 21;

  public static final int HOURS_PER_DAY = 24;

  public static final int MINUTES_PER_HOUR = 60;
  public static final int MINUTES_PER_DAY = HOURS_PER_DAY * MINUTES_PER_HOUR;
  
  public static final int MILLIS_PER_SECOND = 1000;
  public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  public static final int MILLIS_PER_HOUR = MINUTES_PER_HOUR * MILLIS_PER_MINUTE;
  public static final long MILLIS_PER_DAY = HOURS_PER_DAY * MILLIS_PER_HOUR;
  public static final long MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;
 
  public static final RangeOptions OPEN_REQUIRED = new RangeOptions(false, true, true);
  public static final RangeOptions OPEN_NOT_REQUIRED = new RangeOptions(false, true, false);
  public static final RangeOptions CLOSED_REQUIRED = new RangeOptions(false, false, true);
  public static final RangeOptions CLOSED_NOT_REQUIRED = new RangeOptions(false, false, false);

  private static final String[] FIELD_NAME = {
      "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
      "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
      "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
      "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
      "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
      "JULIAN_DAY", "MILLISECONDS_IN_DAY",
  };

  private static final Splitter FIELD_SPLITTER =
      Splitter.on(CharMatcher.inRange(BeeConst.CHAR_ZERO, BeeConst.CHAR_NINE).negate())
          .omitEmptyStrings().trimResults();

  /**
   * Adds an amount of field type data to the date.
   * 
   * @param date the initial date to add to
   * @param field the field type to add
   * @param amount the amount to add
   */
  public static void add(DateTime date, int field, int amount) {
    Assert.notNull(date);
    if (amount == 0) {
      return;
    }
    date.setTime(date.getTime() + getDelta(date, field, amount));
  }

  public static void addDay(JustDate date, int amount) {
    Assert.notNull(date);
    if (amount != 0) {
      date.setDays(date.getDays() + amount);
    }
  }

  public static void addHour(DateTime date, int amount) {
    add(date, FIELD_HOUR, amount);
  }

  public static void addMinute(DateTime date, int amount) {
    add(date, FIELD_MINUTE, amount);
  }

  public static DateTime combine(HasDateValue datePart, DateTime timePart) {
    if (datePart == null) {
      return timePart;
    }
    if (timePart == null) {
      return datePart.getDateTime();
    }

    return new DateTime(datePart.getYear(), datePart.getMonth(), datePart.getDom(),
        timePart.getHour(), timePart.getMinute(), timePart.getSecond(), timePart.getMillis());
  }

  public static int compare(HasDateValue d1, HasDateValue d2) {
    if (d1 == null) {
      if (d2 == null) {
        return BeeConst.COMPARE_EQUAL;
      } else {
        return BeeConst.COMPARE_LESS;
      }
    } else if (d2 == null) {
      return BeeConst.COMPARE_MORE;

    } else if (d1 instanceof JustDate) {
      if (d2 instanceof JustDate) {
        return ((JustDate) d1).compareTo((JustDate) d2);
      } else {
        return d1.getDateTime().compareTo(d2.getDateTime());
      }

    } else if (d1 instanceof DateTime) {
      if (d2 instanceof DateTime) {
        return ((DateTime) d1).compareTo((DateTime) d2);
      } else {
        return ((DateTime) d1).compareTo(d2.getDateTime());
      }
    }

    Assert.untouchable();
    return BeeConst.COMPARE_EQUAL;
  }

  public static int countFields(String s) {
    if (BeeUtils.isEmpty(s)) {
      return 0;
    }
    return Iterables.size(FIELD_SPLITTER.split(s));
  }

  /**
   * Gets the difference between {@code start} and {@code end}.
   * 
   * @param start the start time
   * @param end the end time
   * @return the difference between {@code start} and {@code end} in days.
   */
  public static int dateDiff(DateTime start, DateTime end) {
    return fieldDifference(start, end, FIELD_DATE);
  }

  public static String dateToString(HasDateValue date, char sep) {
    if (date == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return dateToString(date.getYear(), date.getMonth(), date.getDom(), sep);
    }
  }

  public static String dateToString(int year, int month, int dom, char sep) {
    return yearToString(year) + sep + monthToString(month) + sep + dayOfMonthToString(dom);
  }

  public static int dayDiff(HasDateValue start, HasDateValue end) {
    Assert.notNull(start);
    Assert.notNull(end);

    return end.getDate().getDays() - start.getDate().getDays();
  }

  public static String dayOfMonthToString(int dom) {
    return padTwo(dom);
  }

  /**
   * Returns the elapsed time in seconds.
   * 
   * @param start the start time
   * @return the elapsed time in seconds from the specified start in brackets.
   */
  public static String elapsedSeconds(long start) {
    return BeeUtils.bracket(toSeconds(System.currentTimeMillis() - start));
  }

  public static JustDate endOfMonth(HasYearMonth ref) {
    return endOfMonth(ref, 0);
  }

  public static JustDate endOfMonth(HasYearMonth ref, int increment) {
    Assert.notNull(ref);
    return YearMonth.get(ref).shiftMonth(increment).getLast();
  }

  public static JustDate endOfPreviousMonth(HasYearMonth ref) {
    return endOfMonth(ref, -1);
  }

  public static boolean equals(HasDateValue x, HasDateValue y) {
    if (x instanceof JustDate && y instanceof JustDate) {
      return sameDate(x, y);
    }
    if (x instanceof DateTime && y instanceof DateTime) {
      return sameDateTime(x, y);
    }
    return false;
  }

  /**
   * Gets the specified field's difference between {@code start} and {@code end}.
   * 
   * @param start the start time
   * @param end the end time
   * @param field the used field. E.g 1(years),2(months),5(days) etc.
   * @return difference between {@code start} and {@code end}.
   */
  public static int fieldDifference(DateTime start, DateTime end, int field) {
    Assert.notNull(start);
    Assert.notNull(end);

    long startMs = start.getTime();
    long endMs = end.getTime();

    if (startMs == endMs) {
      return 0;
    }
    if (startMs > endMs) {
      return -fieldDifference(end, start, field);
    }

    int min = 0;
    int max = 1;

    for (;;) {
      long ms = startMs + getDelta(start, field, max);
      if (ms == endMs) {
        return max;
      } else if (ms > endMs) {
        break;
      } else {
        max <<= 1;
        Assert.isPositive(max, "Field difference too large to fit into int");
      }
    }

    while ((max - min) > 1) {
      int t = (min + max) / 2;
      long ms = startMs + getDelta(start, field, t);
      if (ms == endMs) {
        return t;
      } else if (ms > endMs) {
        max = t;
      } else {
        min = t;
      }
    }

    return min;
  }

  public static JustDate getDate(HasDateValue src, int increment) {
    Assert.notNull(src);
    return new JustDate(src.getDate().getDays() + increment);
  }

  public static int getMillis(int hour, int minute, int second, int millis) {
    int z = 0;
    if (hour != 0) {
      z += hour * MILLIS_PER_HOUR;
    }
    if (minute != 0) {
      z += minute * MILLIS_PER_MINUTE;
    }
    if (second != 0) {
      z += second * MILLIS_PER_SECOND;
    }
    return z + millis;
  }

  public static boolean isBetween(HasDateValue dt, HasDateValue min, HasDateValue max,
      RangeOptions options) {
    Assert.notNull(options);
    if (dt == null) {
      return true;
    } else if (min == null && max == null) {
      return !options.isLowerRequired() && !options.isUpperRequired();

    } else if (dt instanceof DateTime || min instanceof DateTime || max instanceof DateTime) {
      return options.contains(DateTime.get(min), DateTime.get(max), DateTime.get(dt));
    } else if (dt instanceof JustDate || min instanceof JustDate || max instanceof JustDate) {
      return options.contains(JustDate.get(min), JustDate.get(max), JustDate.get(dt));
    } else {
      return false;
    }
  }

  public static boolean isBetweenExclusiveNotRequired(HasDateValue dt, HasDateValue min,
      HasDateValue max) {
    return isBetween(dt, min, max, OPEN_NOT_REQUIRED);
  }

  public static boolean isBetweenExclusiveRequired(HasDateValue dt, HasDateValue min,
      HasDateValue max) {
    return isBetween(dt, min, max, OPEN_REQUIRED);
  }

  public static boolean isBetweenInclusiveNotRequired(HasDateValue dt, HasDateValue min,
      HasDateValue max) {
    return isBetween(dt, min, max, CLOSED_NOT_REQUIRED);
  }

  public static boolean isBetweenInclusiveRequired(HasDateValue dt, HasDateValue min,
      HasDateValue max) {
    return isBetween(dt, min, max, CLOSED_REQUIRED);
  }

  /**
   * Checks if {@code x} is and instance of HasDateValue or Date.
   * 
   * @param x the Object to check
   * @return true if {@code x} is an instance of any of these types, false otherwise.
   */
  public static boolean isDateOrDateTime(Object x) {
    return x instanceof HasDateValue || x instanceof Date;
  }

  public static boolean isLeq(HasDateValue d1, HasDateValue d2) {
    return compare(d1, d2) <= 0;
  }

  public static boolean isLess(HasDateValue d1, HasDateValue d2) {
    return compare(d1, d2) < 0;
  }

  public static boolean isMeq(HasDateValue d1, HasDateValue d2) {
    return compare(d1, d2) >= 0;
  }

  public static boolean isMore(HasDateValue d1, HasDateValue d2) {
    return compare(d1, d2) > 0;
  }

  public static boolean isToday(HasDateValue dt) {
    return sameDate(dt, today());
  }

  public static boolean isWeekend(HasDateValue dt) {
    return (dt == null) ? false : dt.getDow() >= 6;
  }

  /**
   * @param millis the value to convert
   * @return the String representation of milliseconds.
   */
  public static String millisToString(int millis) {
    if (millis >= 0 && millis < 1000) {
      return Integer.toString(millis + 1000).substring(1);
    } else {
      return Integer.toString(millis);
    }
  }

  public static int minuteDiff(DateTime start, DateTime end) {
    Assert.notNull(start);
    Assert.notNull(end);
    
    long minutes = end.getTime() / MILLIS_PER_MINUTE - start.getTime() / MILLIS_PER_MINUTE;
    return (int) minutes;
  }

  public static int minutesSinceDayStarted(DateTime dt) {
    Assert.notNull(dt);
    return dt.getHour() * MINUTES_PER_HOUR + dt.getMinute();
  }
  
  public static String monthToString(int month) {
    return padTwo(month);
  }

  public static void moveOneDayForward(JustDate date) {
    addDay(date, 1);
  }

  public static JustDate nextDay(HasDateValue ref) {
    return nextDay(ref, 1);
  }

  public static JustDate nextDay(HasDateValue ref, int increment) {
    Assert.notNull(ref);
    return new JustDate(ref.getDate().getDays() + increment);
  }

  public static DateTime nextHour(DateTime ref, int increment) {
    Assert.notNull(ref);
    long millis = (ref.getTime() / MILLIS_PER_HOUR + 1) * MILLIS_PER_HOUR;
    return new DateTime(millis + MILLIS_PER_HOUR * increment);
  }

  public static DateTime nextHour(int increment) {
    return nextHour(new DateTime(), increment);
  }

  public static String normalize(AbstractDate x) {
    if (x == null) {
      return null;
    } else {
      return x.serialize();
    }
  }

  public static int normalizeYear(int year) {
    if (year < 0 || year >= 100) {
      return year;
    } else {
      return year + 2000;
    }
  }

  /**
   * Left pads and integer {@code number} by adding "0" to size of two.
   * 
   * @param number the value to pad
   * @return a String representation of the padded value {@code number} if
   *         {@code number >=0 and number < 10}, otherwise a non-padded value String.
   */
  public static String padTwo(int number) {
    if (number >= 0 && number < 10) {
      return BeeConst.STRING_ZERO + number;
    } else {
      return String.valueOf(number);
    }
  }

  /**
   * Parses a String {@code s} to an array. Used for constructing Date etc.
   * 
   * @param s the String to parse
   * @return an Integer array with the parsed fields.
   */
  public static int[] parseFields(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    int[] arr = new int[7];
    int idx = 0;

    for (String z : FIELD_SPLITTER.split(s)) {
      arr[idx++] = BeeUtils.toInt(z);
      if (idx >= arr.length) {
        break;
      }
    }
    for (int i = idx; i < arr.length; i++) {
      arr[i] = 0;
    }
    return arr;
  }

  public static int parseTime(String s) {
    if (BeeUtils.isEmpty(s)) {
      return 0;
    }

    int[] arr = parseFields(s);
    if (Ints.max(arr) <= 0) {
      return 0;
    }

    int millis = MILLIS_PER_HOUR * arr[0] + MILLIS_PER_MINUTE * arr[1] 
        + MILLIS_PER_SECOND * arr[2] + arr[3];
    return millis;
  }
  
  public static JustDate previousDay(HasDateValue ref) {
    return nextDay(ref, -1);
  }

  /**
   * Generates a random JustDate between {@code min} and {@code max}.
   * 
   * @param min the minimum JustDate
   * @param max the maximum JustDate
   * @return a JustDate between specified {@code min} and {@code max}.
   */
  public static JustDate randomDate(JustDate min, JustDate max) {
    Assert.notNull(min);
    Assert.notNull(max);
    return new JustDate(BeeUtils.randomInt(min.getDays(), max.getDays()));
  }

  /**
   * Generates a random DateTime between {@code min} and {@code max}.
   * 
   * @param min the minimum DateTime
   * @param max the maximum DateTime
   * @return a DateTime between specified {@code min} and {@code max}.
   */
  public static DateTime randomDateTime(DateTime min, DateTime max) {
    Assert.notNull(min);
    Assert.notNull(max);
    return new DateTime(BeeUtils.randomLong(min.getTime(), max.getTime()));
  }

  public static boolean sameDate(HasDateValue x, HasDateValue y) {
    if (x == null || y == null) {
      return x == y;
    }
    return x.getYear() == y.getYear() && x.getMonth() == y.getMonth() && x.getDom() == y.getDom();
  }

  public static boolean sameDateTime(HasDateValue x, HasDateValue y) {
    if (x == null || y == null) {
      return x == y;
    }
    return x.getDateTime().getTime() == y.getDateTime().getTime();
  }

  public static boolean sameMonth(HasYearMonth x, HasYearMonth y) {
    if (x == null || y == null) {
      return false;
    }
    return x.getYear() == y.getYear() && x.getMonth() == y.getMonth();
  }

  public static DateTime startOfDay() {
    return new DateTime(today());
  }

  public static DateTime startOfDay(HasDateValue ref) {
    return startOfDay(ref, 0);
  }

  public static DateTime startOfDay(HasDateValue ref, int increment) {
    Assert.notNull(ref);
    return new DateTime(ref.getYear(), ref.getMonth(), ref.getDom() + increment);
  }

  public static DateTime startOfDay(int increment) {
    return startOfDay(today(), increment);
  }

  public static JustDate startOfMonth() {
    JustDate date = new JustDate();
    int dom = date.getDom();
    if (dom > 1) {
      addDay(date, 1 - dom);
    }
    return date;
  }

  public static JustDate startOfMonth(HasYearMonth ref) {
    return startOfMonth(ref, 0);
  }

  public static JustDate startOfMonth(HasYearMonth ref, int increment) {
    Assert.notNull(ref);
    if (increment == 0) {
      return new JustDate(ref.getYear(), ref.getMonth(), 1);
    } else {
      return YearMonth.get(ref).shiftMonth(increment).getDate();
    }
  }

  public static JustDate startOfNextMonth(HasYearMonth ref) {
    return startOfMonth(ref, 1);
  }

  public static JustDate startOfPreviousMonth(HasYearMonth ref) {
    return startOfMonth(ref, -1);
  }

  public static JustDate startOfQuarter(HasYearMonth ref, int increment) {
    Assert.notNull(ref);
    return startOfMonth(ref, increment * 3 - (ref.getMonth() - 1) % 3);
  }

  public static JustDate startOfWeek() {
    return startOfWeek(today());
  }

  public static JustDate startOfWeek(HasDateValue ref) {
    return startOfWeek(ref, 0);
  }

  public static JustDate startOfWeek(HasDateValue ref, int increment) {
    Assert.notNull(ref);
    JustDate date = new JustDate(ref.getYear(), ref.getMonth(), ref.getDom());

    int incrDays = 0;
    int dow = ref.getDow();
    if (dow > 1) {
      incrDays -= dow - 1;
    }
    if (increment != 0) {
      incrDays += increment * 7;
    }

    if (incrDays != 0) {
      addDay(date, incrDays);
    }
    return date;
  }

  public static JustDate startOfYear() {
    return startOfYear(today());
  }

  public static JustDate startOfYear(HasYearMonth ref) {
    return startOfYear(ref, 0);
  }

  public static JustDate startOfYear(HasYearMonth ref, int increment) {
    Assert.notNull(ref);
    int year = ref.getYear();
    if (increment != 0) {
      year += increment;
    }
    return new JustDate(year, 1, 1);
  }

  /**
   * Converts {@code x} to a JustDate format.
   * 
   * @param x the Object to convert
   * @return a JustDate type date.
   */
  public static JustDate toDate(Object x) {
    if (x instanceof JustDate) {
      return (JustDate) x;
    }
    if (x instanceof DateTime) {
      return new JustDate((DateTime) x);
    }
    if (x instanceof Date) {
      return new JustDate((Date) x);
    }

    assertDateOrDateTime(x);
    return null;
  }

  public static JustDate toDateOrNull(Integer day) {
    if (day == null) {
      return null;
    } else {
      return new JustDate(day);
    }
  }

  public static JustDate toDateOrNull(String s) {
    if (BeeUtils.isInt(s)) {
      return new JustDate(BeeUtils.toInt(s));
    } else {
      return null;
    }
  }

  /**
   * Converts {@code x} to a DateTime format.
   * 
   * @param x the Object to convert
   * @return a DateTime type date.
   */
  public static DateTime toDateTime(Object x) {
    if (x instanceof DateTime) {
      return (DateTime) x;
    }
    if (x instanceof JustDate) {
      return new DateTime((JustDate) x);
    }
    if (x instanceof Date) {
      return new DateTime((Date) x);
    }

    assertDateOrDateTime(x);
    return null;
  }

  public static DateTime toDateTimeOrNull(Long time) {
    if (time == null) {
      return null;
    } else {
      return new DateTime(time);
    }
  }

  public static DateTime toDateTimeOrNull(String s) {
    if (BeeUtils.isLong(s)) {
      return new DateTime(BeeUtils.toLong(s));
    } else {
      return null;
    }
  }

  public static JustDate today() {
    return new JustDate();
  }

  public static JustDate today(int increment) {
    JustDate date = new JustDate();
    if (increment != 0) {
      addDay(date, increment);
    }
    return date;
  }

  /**
   * Converts {@code x} to a Date format.
   * 
   * @param x the Object to convert
   * @return a Date type date.
   */
  public static Date toJava(Object x) {
    if (x instanceof Date) {
      return (Date) x;
    }
    if (x instanceof HasDateValue) {
      return ((HasDateValue) x).getJava();
    }

    assertDateOrDateTime(x);
    return null;
  }

  /**
   * Converts milliseconds {@code millis} to seconds. E.g 6010 is converted to 6.010.
   * 
   * @param millis value to convert
   * @return seconds.
   */
  public static String toSeconds(long millis) {
    return Long.toString(millis / 1000) + BeeConst.STRING_POINT
        + BeeUtils.toLeadingZeroes((int) (millis % 1000), 3);
  }
  
  /**
   * @param year the number to transform
   * @return a textual representation of {@code year}.
   */
  public static String yearToString(int year) {
    return Integer.toString(year);
  }

  private static void assertDateOrDateTime(Object x) {
    Assert.isTrue(isDateOrDateTime(x), "Argument must be Date or DateTime");
  }

  private static String fieldName(int field) {
    if (ArrayUtils.isIndex(FIELD_NAME, field)) {
      return FIELD_NAME[field];
    } else {
      return "Field " + field;
    }
  }

  private static long getDelta(DateTime date, int field, int amount) {
    long delta = amount;

    switch (field) {
      case FIELD_YEAR:
      case FIELD_MONTH:
        int y1 = date.getYear();
        int m1 = date.getMonth();
        int d1 = date.getDom();
        int y2 = y1;
        int m2 = m1;

        if (field == FIELD_YEAR) {
          y2 += amount;
        } else {
          m2 += amount;
          if (m2 < 1 || m2 > 12) {
            int z = y1 * 12 + m1 - 1 + amount;
            y2 = z / 12;
            m2 = z % 12 + 1;
          }
        }

        int d2 = Math.min(d1, Grego.monthLength(y2, m2));
        delta = new DateTime(y2, m2, d2).getTime() - new DateTime(y1, m1, d1).getTime();
        break;

      case FIELD_WEEK_OF_YEAR:
      case FIELD_WEEK_OF_MONTH:
      case FIELD_DAY_OF_WEEK_IN_MONTH:
        delta *= MILLIS_PER_WEEK;
        break;

      case FIELD_AM_PM:
        delta *= 12 * MILLIS_PER_HOUR;
        break;

      case FIELD_DAY_OF_MONTH:
      case FIELD_DAY_OF_YEAR:
      case FIELD_DAY_OF_WEEK:
      case FIELD_DOW_LOCAL:
      case FIELD_JULIAN_DAY:
        delta *= MILLIS_PER_DAY;
        break;

      case FIELD_HOUR_OF_DAY:
      case FIELD_HOUR:
        delta *= MILLIS_PER_HOUR;
        break;

      case FIELD_MINUTE:
        delta *= MILLIS_PER_MINUTE;
        break;

      case FIELD_SECOND:
        delta *= MILLIS_PER_SECOND;
        break;

      case FIELD_MILLISECOND:
      case FIELD_MILLISECONDS_IN_DAY:
        break;

      default:
        Assert.unsupported(BeeUtils.joinWords("delta" + fieldName(field) + "not supported"));
    }
    return delta;
  }

  private TimeUtils() {
  }
}
