package com.butent.bee.client.i18n;

import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;

final class TimeZone {

  private static final int STD_SHORT_NAME = 0;
  private static final int STD_LONG_NAME = 1;
  private static final int DLT_SHORT_NAME = 2;
  private static final int DLT_LONG_NAME = 3;

  static TimeZone createTimeZone(int timeZoneOffsetInMinutes) {
    TimeZone tz = new TimeZone();
    tz.standardOffset = timeZoneOffsetInMinutes;
    tz.timezoneID = composePosixTimeZoneID(timeZoneOffsetInMinutes);
    tz.tzNames = new String[2];
    tz.tzNames[0] = composeUTCString(timeZoneOffsetInMinutes);
    tz.tzNames[1] = composeUTCString(timeZoneOffsetInMinutes);
    tz.transitionPoints = null;
    tz.adjustments = null;
    return tz;
  }

  private static String composeGMTString(int offset) {
    char[] data = {'G', 'M', 'T', '-', '0', '0', ':', '0', '0'};
    int x = offset;

    if (x <= 0) {
      data[3] = '+';
      x = -x;
    }
    data[4] += (x / 60) / 10;
    data[5] += (x / 60) % 10;
    data[7] += (x % 60) / 10;
    data[8] += x % 10;

    return new String(data);
  }

  private static String composePosixTimeZoneID(int offset) {
    int x = offset;
    if (x == 0) {
      return "Etc/GMT";
    }

    String str;
    if (x < 0) {
      x = -x;
      str = "Etc/GMT-";
    } else {
      str = "Etc/GMT+";
    }
    return str + offsetDisplay(x);
  }

  private static String composeUTCString(int offset) {
    int x = offset;
    if (x == 0) {
      return "UTC";
    }

    String str;
    if (x < 0) {
      x = -x;
      str = "UTC+";
    } else {
      str = "UTC-";
    }
    return str + offsetDisplay(x);
  }

  private static String offsetDisplay(int offset) {
    int hour = offset / 60;
    int minutes = offset % 60;
    if (minutes == 0) {
      return Integer.toString(hour);
    }
    return Integer.toString(hour) + ":" + Integer.toString(minutes);
  }

  private String timezoneID;
  private int standardOffset;
  private String[] tzNames;
  private int[] transitionPoints;
  private int[] adjustments;

  private TimeZone() {
  }

  int getDaylightAdjustment(HasDateValue date) {
    if (!(date instanceof DateTime)) {
      return 0;
    }
    if (transitionPoints == null) {
      return 0;
    }
    long timeInHours = date.getTime() / 1000 / 3600;
    int index = 0;
    while (index < transitionPoints.length && timeInHours >= transitionPoints[index]) {
      ++index;
    }
    return (index == 0) ? 0 : adjustments[index - 1];
  }

  String getGMTString(HasDateValue date) {
    return composeGMTString(getOffset(date));
  }

  String getID() {
    return timezoneID;
  }

  String getISOTimeZoneString(HasDateValue date) {
    int offset = -getOffset(date);
    char[] data = {'+', '0', '0', ':', '0', '0'};
    if (offset < 0) {
      data[0] = '-';
      offset = -offset;
    }
    data[1] += (offset / 60) / 10;
    data[2] += (offset / 60) % 10;
    data[4] += (offset % 60) / 10;
    data[5] += offset % 10;
    return new String(data);
  }

  String getLongName(HasDateValue date) {
    return tzNames[isDaylightTime(date) ? DLT_LONG_NAME : STD_LONG_NAME];
  }

  int getOffset(HasDateValue date) {
    return standardOffset - getDaylightAdjustment(date);
  }

  String getRFCTimeZoneString(HasDateValue date) {
    int offset = -getOffset(date);
    char[] data = {'+', '0', '0', '0', '0'};
    if (offset < 0) {
      data[0] = '-';
      offset = -offset;
    }
    data[1] += (offset / 60) / 10;
    data[2] += (offset / 60) % 10;
    data[3] += (offset % 60) / 10;
    data[4] += offset % 10;
    return new String(data);
  }

  String getShortName(HasDateValue date) {
    return tzNames[isDaylightTime(date) ? DLT_SHORT_NAME : STD_SHORT_NAME];
  }

  boolean isDaylightTime(HasDateValue date) {
    return getDaylightAdjustment(date) > 0;
  }
}
