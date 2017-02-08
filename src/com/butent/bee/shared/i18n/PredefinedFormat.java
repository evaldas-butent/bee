package com.butent.bee.shared.i18n;

import com.butent.bee.shared.i18n.DateTimeFormatInfo.DateTimeFormatInfo;

public enum PredefinedFormat {
  ISO_8601 {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return "yyyy-MM-dd'T'HH:mm:ss.SSSZZZ";
    }
  },
  RFC_2822 {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return "EEE, d MMM yyyy HH:mm:ss Z";
    }
  },

  DATE_FULL {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateFormatFull();
    }
  },
  DATE_LONG {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateFormatLong();
    }
  },
  DATE_MEDIUM {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateFormatMedium();
    }
  },
  DATE_SHORT {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateFormatShort();
    }
  },

  TIME_FULL {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.timeFormatFull();
    }
  },
  TIME_LONG {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.timeFormatLong();
    }
  },
  TIME_MEDIUM {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.timeFormatMedium();
    }
  },
  TIME_SHORT {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.timeFormatShort();
    }
  },

  DATE_TIME_FULL {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateTimeFull(dateTimeFormatInfo.timeFormatFull(),
          dateTimeFormatInfo.dateFormatFull());
    }
  },
  DATE_TIME_LONG {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateTimeLong(dateTimeFormatInfo.timeFormatLong(),
          dateTimeFormatInfo.dateFormatLong());
    }
  },
  DATE_TIME_MEDIUM {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateTimeMedium(dateTimeFormatInfo.timeFormatMedium(),
          dateTimeFormatInfo.dateFormatMedium());
    }
  },
  DATE_TIME_SHORT {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateTimeShort(dateTimeFormatInfo.timeFormatShort(),
          dateTimeFormatInfo.dateFormatShort());
    }
  },
  DATE_SHORT_TIME_MEDIUM {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.dateTime(dateTimeFormatInfo.timeFormatMedium(),
          dateTimeFormatInfo.dateFormatShort());
    }
  },

  HOUR_MINUTE {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatHour12Minute();
    }
  },
  HOUR_MINUTE_SECOND {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatHour12MinuteSecond();
    }
  },
  HOUR24_MINUTE {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatHour24Minute();
    }
  },
  HOUR24_MINUTE_SECOND {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatHour24MinuteSecond();
    }
  },
  MINUTE_SECOND {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMinuteSecond();
    }
  },

  YEAR {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYear();
    }
  },
  YEAR_MONTH {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthFull();
    }
  },
  YEAR_MONTH_ABBR {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthAbbrev();
    }
  },
  YEAR_MONTH_ABBR_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthAbbrevDay();
    }
  },
  YEAR_MONTH_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthFullDay();
    }
  },
  YEAR_MONTH_NUM {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthNum();
    }
  },
  YEAR_MONTH_NUM_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthNumDay();
    }
  },
  YEAR_MONTH_WEEKDAY_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearMonthWeekdayDay();
    }
  },
  YEAR_QUARTER {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearQuarterFull();
    }
  },
  YEAR_QUARTER_ABBR {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatYearQuarterShort();
    }
  },

  MONTH {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthFull();
    }
  },
  MONTH_ABBR {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthAbbrev();
    }
  },
  MONTH_ABBR_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthAbbrevDay();
    }
  },
  MONTH_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthFullDay();
    }
  },
  MONTH_NUM_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthNumDay();
    }
  },
  MONTH_WEEKDAY_DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatMonthFullWeekdayDay();
    }
  },

  DAY {
    @Override
    String getPattern(DateTimeFormatInfo dateTimeFormatInfo) {
      return dateTimeFormatInfo.formatDay();
    }
  };

  abstract String getPattern(DateTimeFormatInfo dateTimeFormatInfo);
}
