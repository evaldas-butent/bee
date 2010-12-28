package com.butent.bee.egg.server.datasource.query.parser;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.DateTimeValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateValue;
import com.butent.bee.egg.server.datasource.datatable.value.TimeOfDayValue;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.logging.Logger;

final class ParserUtils {

  private static final Logger logger = Logger.getLogger(ParserUtils.class.getName());

  private static final String dateMessage = "Invalid date literal [%1$s]. "
      + "Date literals should be of form yyyy-MM-dd.";
  private static final String timeOfDayMessage = "Invalid timeofday "
      + "literal [%1$s]. Timeofday literals should be of form HH:mm:ss[.SSS]";
  private static final String dateTimeMessage =
      "Invalid datetime literal [%1$s]. Datetime literals should "
          + " be of form yyyy-MM-dd HH:mm:ss[.SSS]";

  public static DateValue stringToDate(String s) throws InvalidQueryException {
    String[] split = s.split("-");
    if (split.length != 3) {
      LogUtils.severe(logger, String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    }
    try {
      int year = Integer.parseInt(split[0]);
      int month = Integer.parseInt(split[1]);
      month--;
      int day = Integer.parseInt(split[2]);
      return new DateValue(year, month, day);
    } catch (NumberFormatException e) {
      LogUtils.severe(logger, String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    } catch (IllegalArgumentException e) {
      LogUtils.severe(logger, String.format(dateMessage, s));
      throw new InvalidQueryException(String.format(dateMessage, s));
    }
  }

  public static DateTimeValue stringToDatetime(String s) throws InvalidQueryException {
    String[] mainSplit = s.split(" ");
    if (mainSplit.length != 2) {
      LogUtils.severe(logger, String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
    String[] dateSplit = mainSplit[0].split("-");
    String[] timeSplit = mainSplit[1].split(":");
    if ((dateSplit.length != 3) || (timeSplit.length != 3)) {
      LogUtils.severe(logger, String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
    try {
      int year = Integer.parseInt(dateSplit[0]);
      int month = Integer.parseInt(dateSplit[1]);
      month--;
      int day = Integer.parseInt(dateSplit[2]);
      int hour = Integer.parseInt(timeSplit[0]);
      int minute = Integer.parseInt(timeSplit[1]);
      int second;
      int milli = 0;
      if (timeSplit[2].contains(".")) {
        String[] secondMilliSplit = timeSplit[2].split("\\.");
        if (secondMilliSplit.length != 2) {
          LogUtils.severe(logger, String.format(dateTimeMessage, s));
          throw new InvalidQueryException(String.format(dateTimeMessage, s));
        }
        second = Integer.parseInt(secondMilliSplit[0]);
        milli = Integer.parseInt(secondMilliSplit[1]);
      } else {
        second = Integer.parseInt(timeSplit[2]);
      }
      return new DateTimeValue(year, month, day, hour, minute, second, milli);
    } catch (NumberFormatException e) {
      LogUtils.severe(logger, String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    } catch (IllegalArgumentException e) {
      LogUtils.severe(logger, String.format(dateTimeMessage, s));
      throw new InvalidQueryException(String.format(dateTimeMessage, s));
    }
  }

  public static TimeOfDayValue stringToTimeOfDay(String s) throws InvalidQueryException {
    String[] split = s.split(":");
    if (split.length != 3) {
      LogUtils.severe(logger, String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    }
    try {
      int hour = Integer.parseInt(split[0]);
      int minute = Integer.parseInt(split[1]);
      int second;
      if (split[2].contains(".")) {
        String[] secondMilliSplit = split[2].split(".");
        if (secondMilliSplit.length != 2) {
          LogUtils.severe(logger, String.format(timeOfDayMessage, s));
          throw new InvalidQueryException(String.format(timeOfDayMessage, s));
        }
        second = Integer.parseInt(secondMilliSplit[0]);
        int milli = Integer.parseInt(secondMilliSplit[1]);
        return new TimeOfDayValue(hour, minute, second, milli);
      } else {
        second = Integer.parseInt(split[2]);
        return new TimeOfDayValue(hour, minute, second);
      }
    } catch (NumberFormatException e) {
      LogUtils.severe(logger, String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    } catch (IllegalArgumentException e) {
      LogUtils.severe(logger, String.format(timeOfDayMessage, s));
      throw new InvalidQueryException(String.format(timeOfDayMessage, s));
    }
  }

  public static String stripQuotes(String s) {
    if (s.length() < 2) {
      throw new RuntimeException("String is of length < 2 on call to "
          + "stripQuotes: " + s);
    }
    return s.substring(1, s.length() - 1);
  }

  private ParserUtils() {
  }
}
