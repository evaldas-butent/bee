package com.butent.bee.shared.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;

import java.util.Date;

public class TimeUtils {
  public static final int ERA = 0;

  public static final int YEAR = 1;
  public static final int MONTH = 2;
  public static final int WEEK_OF_YEAR = 3;
  public static final int WEEK_OF_MONTH = 4;
  public static final int DATE = 5;
  public static final int DAY_OF_MONTH = 5;
  public static final int DAY_OF_YEAR = 6;
  public static final int DAY_OF_WEEK = 7;
  public static final int DAY_OF_WEEK_IN_MONTH = 8;

  public static final int AM_PM = 9;
  public static final int HOUR = 10;
  public static final int HOUR_OF_DAY = 11;
  public static final int MINUTE = 12;
  public static final int SECOND = 13;
  public static final int MILLISECOND = 14;

  public static final int ZONE_OFFSET = 15;
  public static final int DST_OFFSET = 16;

  public static final int YEAR_WOY = 17;

  public static final int DOW_LOCAL = 18;

  public static final int EXTENDED_YEAR = 19;
  public static final int JULIAN_DAY = 20;

  public static final int MILLISECONDS_IN_DAY = 21;

  public static final int MILLIS_PER_SECOND = 1000;
  public static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  public static final int MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
  public static final int MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;
  public static final int MILLIS_PER_WEEK = 7 * MILLIS_PER_DAY;

  private static final String[] FIELD_NAME = {
    "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH",
    "DAY_OF_MONTH", "DAY_OF_YEAR", "DAY_OF_WEEK",
    "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR", "HOUR_OF_DAY",
    "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
    "DST_OFFSET", "YEAR_WOY", "DOW_LOCAL", "EXTENDED_YEAR",
    "JULIAN_DAY", "MILLISECONDS_IN_DAY",
  };

  public static void add(DateTime date, int field, int amount) {
    Assert.notNull(date);
    if (amount == 0) {
      return;
    }
    date.setTime(date.getTime() + getDelta(date, field, amount));
  }
  
  public static int dateDiff(DateTime start, DateTime end) {
    return fieldDifference(start, end, DATE);
  }

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

  public static boolean isDateOrDateTime(Object x) {
    return x instanceof JustDate || x instanceof DateTime || x instanceof Date;
  }

  public static JustDate randomDate(JustDate min, JustDate max) {
    Assert.notNull(min);
    Assert.notNull(max);
    return new JustDate(BeeUtils.randomInt(min.getDay(), max.getDay()));
  }
  
  public static DateTime randomDateTime(DateTime min, DateTime max) {
    Assert.notNull(min);
    Assert.notNull(max);
    return new DateTime(BeeUtils.randomLong(min.getTime(), max.getTime()));
  }
  
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

  public static Date toJava(Object x) {
    if (x instanceof Date) {
      return (Date) x;
    }
    if (x instanceof DateTime) {
      return new Date(((DateTime) x).getTime());
    }
    if (x instanceof JustDate) {
      return new Date(new DateTime((JustDate) x).getTime());
    }

    assertDateOrDateTime(x);
    return null;
  }

  private static void assertDateOrDateTime(Object x) {
    Assert.isTrue(isDateOrDateTime(x), "Argument must be Date or DateTime");
  }
  
  private static String fieldName(int field) {
    if (BeeUtils.isIndex(FIELD_NAME, field)) {
      return FIELD_NAME[field];
    } else {
      return "Field " + field;
    }
  }

  private static long getDelta(DateTime date, int field, int amount) {
    long delta = amount;

    switch (field) {
      case YEAR:
      case MONTH:
        int y1 = date.getYear();
        int m1 = date.getMonth();
        int d1 = date.getDom();
        int y2 = y1;
        int m2 = m1;
        
        if (field == YEAR) {
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
        
      case WEEK_OF_YEAR:
      case WEEK_OF_MONTH:
      case DAY_OF_WEEK_IN_MONTH:
        delta *= MILLIS_PER_WEEK;
        break;

      case AM_PM:
        delta *= 12 * MILLIS_PER_HOUR;
        break;

      case DAY_OF_MONTH:
      case DAY_OF_YEAR:
      case DAY_OF_WEEK:
      case DOW_LOCAL:
      case JULIAN_DAY:
        delta *= MILLIS_PER_DAY;
        break;

      case HOUR_OF_DAY:
      case HOUR:
        delta *= MILLIS_PER_HOUR;
        break;

      case MINUTE:
        delta *= MILLIS_PER_MINUTE;
        break;

      case SECOND:
        delta *= MILLIS_PER_SECOND;
        break;

      case MILLISECOND:
      case MILLISECONDS_IN_DAY:
        break;

      default:
        Assert.unsupported(BeeUtils.concat(1, "delta" + fieldName(field) + "not supported"));
    }

    return delta;
  }
}
