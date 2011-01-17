package com.butent.bee.server.datasource.util;

import com.google.common.collect.Maps;

import com.butent.bee.server.datasource.base.BooleanFormat;
import com.butent.bee.server.datasource.base.LocaleUtil;
import com.butent.bee.server.datasource.base.TextFormat;
import com.butent.bee.shared.BeeDate;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.UFormat;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class ValueFormatter {

  private static final String DEFAULT_TEXT_DUMMY_PATTERN = "dummy";

  private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  private static final String DEFAULT_DATE_PATTERNS = "yyyy-MM-dd";
  private static final String DEFAULT_TIMEOFDAY_PATTERN = "HH:mm:ss";
  private static final String DEFAULT_BOOLEAN_PATTERN = "true:false";
  private static final String DEFAULT_NUMBER_PATTERN = "";

  public static ValueFormatter createDefault(ValueType type, ULocale locale) {
    String pattern = getDefaultPatternByType(type);
    return createFromPattern(type, pattern, locale);
  }

  public static Map<ValueType, ValueFormatter> createDefaultFormatters(ULocale locale) {
    Map<ValueType, ValueFormatter> foramtters = Maps.newHashMap();
    for (ValueType type : ValueType.values()) {
      foramtters.put(type, createDefault(type, locale));
    }
    return foramtters;
  }

  public static ValueFormatter createFromPattern(ValueType type, String pattern, ULocale locale) {
    UFormat uFormat = null;
    if (pattern == null) {
      pattern = getDefaultPatternByType(type);
    }

    if (locale == null) {
      locale = LocaleUtil.getDefaultLocale();
    }

    try {
      switch (type) {
        case BOOLEAN:
          uFormat = new BooleanFormat(pattern);
          uFormat.format(BooleanValue.TRUE.getObjectValue());
          break;
        case TEXT:
          uFormat = new TextFormat();
          break;
        case DATE:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new DateValue(1995, 7, 3).getObjectValue());
          break;
        case TIMEOFDAY:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new TimeOfDayValue(2, 59, 12, 123).getObjectValue());
          break;
        case DATETIME:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new DateTimeValue(1995, 7, 3, 2, 59, 12, 123).getObjectValue());
          break;
        case NUMBER:
          DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
          uFormat = new DecimalFormat(pattern, symbols);
          uFormat.format(new NumberValue(-12.3).getObjectValue());
          break;
      }
    } catch (RuntimeException e) {
      return null;
    }
    return new ValueFormatter(pattern, uFormat, type, locale);
  }
  private static String getDefaultPatternByType(ValueType type) {
    String defaultPattern;
    switch (type) {
      case TEXT:
        defaultPattern = DEFAULT_TEXT_DUMMY_PATTERN;
        break;
      case DATE:
        defaultPattern = DEFAULT_DATE_PATTERNS;
        break;
      case DATETIME:
        defaultPattern = DEFAULT_DATETIME_PATTERN;
        break;
      case TIMEOFDAY:
        defaultPattern = DEFAULT_TIMEOFDAY_PATTERN;
        break;
      case BOOLEAN:
        defaultPattern = DEFAULT_BOOLEAN_PATTERN;
        break;
      case NUMBER:
        defaultPattern = DEFAULT_NUMBER_PATTERN;
        break;
      default:
        defaultPattern = null;
    }
    return defaultPattern;
  }

  private UFormat uFormat;
  private String pattern;
  
  private ULocale locale;

  private ValueType type;

  private ValueFormatter(String pattern, UFormat uFormat, ValueType type, ULocale locale) {
    this.pattern = pattern;
    this.uFormat = uFormat;
    this.type = type;
    this.locale = locale;
  }

  public String format(Value value) {
    if (value.isNull()) {
      return "";
    }
    return uFormat.format(value.getObjectValue());
  }

  public ULocale getLocale() {
    return locale;
  }

  public String getPattern() {
    return pattern;
  }

  public ValueType getType() {
    return type;
  }

  public UFormat getUFormat() {
    return uFormat;
  }

  public Value parse(String val) {
    Value value = null;
    try {
      switch(type) {
        case DATE:
          value = parseDate(val);
          break;
        case TIMEOFDAY:
          value = parseTimeOfDay(val);
          break;
        case DATETIME:
          value = parseDateTime(val);
          break;
        case NUMBER:
          value = parseNumber(val);
          break;
        case BOOLEAN:
          value = parseBoolean(val);
          break;
        case TEXT:
          value = new TextValue(val);
          break;
      }
    } catch (ParseException pe) {
      value = Value.getNullValueFromValueType(type);
    }
    return value;
  }

  private BooleanValue parseBoolean(String val) throws ParseException {
    Boolean bool = ((BooleanFormat) uFormat).parse(val);
    return BooleanValue.getInstance(bool);
  }

  private DateValue parseDate(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    return new DateValue(new BeeDate(date));
  }

  private DateTimeValue parseDateTime(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    return new DateTimeValue(new BeeDate(date));
  }

  private NumberValue parseNumber(String val) throws ParseException {
    Number n = ((NumberFormat) uFormat).parse(val);
    return new NumberValue(n.doubleValue());
  }

  private TimeOfDayValue parseTimeOfDay(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    return new TimeOfDayValue(new BeeDate(date));
  }
}
