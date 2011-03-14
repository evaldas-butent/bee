package com.butent.bee.client.i18n;

import com.google.common.collect.Lists;
import com.google.gwt.i18n.client.DateTimeFormatInfo;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.i18n.client.LocalizedNames;
import com.google.gwt.i18n.client.constants.NumberConstants;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class LocaleUtils {
  public static final String LOCALE_SEPARATOR = "_";
  
  public static List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> lst = Lists.newArrayList();
    
    String[] names = LocaleInfo.getAvailableLocaleNames();
    PropertyUtils.addExtended(lst, "Available Locale Names", ArrayUtils.length(names));
    int i = 0;
    for (String name : names) {
      PropertyUtils.addExtended(lst, "Name", i++, name);
    }
    
    PropertyUtils.addProperties(lst, false,
        "Locale Cookie Name", LocaleInfo.getLocaleCookieName(),
        "Locale Query Param", LocaleInfo.getLocaleQueryParam(),
        "Has Any RTL", LocaleInfo.hasAnyRTL());
    
    LocaleInfo cl = LocaleInfo.getCurrentLocale();
    Assert.notNull(cl);
    
    PropertyUtils.addProperties(lst, false, "Current Locale", cl.getLocaleName(),
        "Locale Native Display Name", LocaleInfo.getLocaleNativeDisplayName(cl.getLocaleName()),
        "Is RTL", cl.isRTL());
    
    LocalizedNames localizedNames = cl.getLocalizedNames();
    if (localizedNames != null) {
      String[] codes = localizedNames.getLikelyRegionCodes();
      PropertyUtils.addExtended(lst, "Likely Region Codes", ArrayUtils.length(codes));
      i = 0;
      for (String code : codes) {
        PropertyUtils.addExtended(lst, BeeUtils.concat(1, "Region", i++),
            localizedNames.getRegionName(code), code);
      }
    
      codes = localizedNames.getSortedRegionCodes();
      PropertyUtils.addExtended(lst, "Sorted Region Codes", ArrayUtils.length(codes));
      i = 0;
      for (String code : codes) {
        PropertyUtils.addExtended(lst, BeeUtils.concat(1, "Region", i++),
            localizedNames.getRegionName(code), code);
      }
    }

    DateTimeFormatInfo dtf = cl.getDateTimeFormatInfo();
    if (dtf != null) {
      String aSep = BeeConst.DEFAULT_ROW_SEPARATOR;
      PropertyUtils.addChildren(lst, "Date Time Format",
          "ampms", ArrayUtils.transform(dtf.ampms(), aSep),
          "Date Format", dtf.dateFormat(),
          "Date Format Full", dtf.dateFormatFull(),
          "Date Format Long", dtf.dateFormatLong(),
          "Date Format Medium", dtf.dateFormatMedium(),
          "Date Format Short", dtf.dateFormatShort(),
          "Eras Full", ArrayUtils.transform(dtf.erasFull(), aSep),
          "Eras Short", ArrayUtils.transform(dtf.erasShort(), aSep),
          "First Day Of The Week", dtf.firstDayOfTheWeek(),
          "Format Day", dtf.formatDay(),
          "Format Hour 12 Minute", dtf.formatHour12Minute(),
          "Format Hour 12 Minute Second", dtf.formatHour12MinuteSecond(),
          "Format Hour 24 Minute", dtf.formatHour24Minute(),
          "Format Hour 24 Minute Second", dtf.formatHour24MinuteSecond(),
          "Format Minute Second", dtf.formatMinuteSecond(),
          "Format Month Abbrev", dtf.formatMonthAbbrev(),
          "Format Month Abbrev Day", dtf.formatMonthAbbrevDay(),
          "Format Month Full", dtf.formatMonthFull(),
          "Format Month Full Day", dtf.formatMonthFullDay(),
          "Format Month Full Weekday Day", dtf.formatMonthFullWeekdayDay(),
          "Format Month Num Day", dtf.formatMonthNumDay(),
          "Format Year", dtf.formatYear(),
          "Format Year Month Abbrev", dtf.formatYearMonthAbbrev(),
          "Format Year Month Abbrev Day", dtf.formatYearMonthAbbrevDay(),
          "Format Year Month Full", dtf.formatYearMonthFull(),
          "Format Year Month Full Day", dtf.formatYearMonthFullDay(),
          "Format Year Month Num", dtf.formatYearMonthNum(),
          "Format Year Month Num Day", dtf.formatYearMonthNumDay(),
          "Format Year Month Weekday Day", dtf.formatYearMonthWeekdayDay(),
          "Format Year Quarter Full", dtf.formatYearQuarterFull(),
          "Format Year Quarter Short", dtf.formatYearQuarterShort(),
          "Months Full", ArrayUtils.transform(dtf.monthsFull(), aSep),
          "Months Full Standalone", ArrayUtils.transform(dtf.monthsFullStandalone(), aSep),
          "Months Narrow", ArrayUtils.transform(dtf.monthsNarrow(), aSep),
          "Months Narrow Standalone", ArrayUtils.transform(dtf.monthsNarrowStandalone(), aSep),
          "Months Short", ArrayUtils.transform(dtf.monthsShort(), aSep),
          "Months Short Standalone", ArrayUtils.transform(dtf.monthsShortStandalone(), aSep),
          "Quarters Full", ArrayUtils.transform(dtf.quartersFull(), aSep),
          "Quarters Short", ArrayUtils.transform(dtf.quartersShort(), aSep),
          "Time Format", dtf.timeFormat(),
          "Time Format Full", dtf.timeFormatFull(),
          "Time Format Long", dtf.timeFormatLong(),
          "Time Format Medium", dtf.timeFormatMedium(),
          "Time Format Short", dtf.timeFormatShort(),
          "Weekdays Full", ArrayUtils.transform(dtf.weekdaysFull(), aSep),
          "Weekdays Full Standalone", ArrayUtils.transform(dtf.weekdaysFullStandalone(), aSep),
          "Weekdays Narrow", ArrayUtils.transform(dtf.weekdaysNarrow(), aSep),
          "Weekdays Narrow Standalone", ArrayUtils.transform(dtf.weekdaysNarrowStandalone(), aSep),
          "Weekdays Short", ArrayUtils.transform(dtf.weekdaysShort(), aSep),
          "Weekdays Short Standalone", ArrayUtils.transform(dtf.weekdaysShortStandalone(), aSep),
          "Weekend End", dtf.weekendEnd(),
          "Weekend Start", dtf.weekendStart());
    }

    NumberConstants nc = cl.getNumberConstants();
    if (nc != null) {
      PropertyUtils.addChildren(lst, "Number Constants",
          "Currency Pattern", nc.currencyPattern(),
          "Decimal Pattern", nc.decimalPattern(),
          "Decimal Separator", nc.decimalSeparator(),
          "Def Currency Code", nc.defCurrencyCode(),
          "Exponential Symbol", nc.exponentialSymbol(),
          "Grouping Separator", nc.groupingSeparator(),
          "Infinity", nc.infinity(),
          "Minus Sign", nc.minusSign(),
          "Monetary Grouping Separator", nc.monetaryGroupingSeparator(),
          "Monetary Separator", nc.monetarySeparator(),
          "Not A Number", nc.notANumber(),
          "Percent", nc.percent(),
          "Percent Pattern", nc.percentPattern(),
          "Per Mill", nc.perMill(),
          "Plus Sign", nc.plusSign(),
          "Scientific Pattern", nc.scientificPattern(),
          "Zero Digit", nc.zeroDigit());
    }
    return lst;
  }
  
  public static String getLanguageCode(LocaleInfo locale) {
    if (locale == null) {
      return BeeConst.STRING_EMPTY;
    }
    String name = locale.getLocaleName();
    int p = name.indexOf(LOCALE_SEPARATOR);
    if (p > 0) {
      return name.substring(0, p);
    } else {
      return name;
    }
  }
}
