package com.butent.bee.server.datasource.base;

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleUtil {
  
  private static final Pattern LOCALE_PATTERN =
    Pattern.compile("(^[^_-]*)(?:[_-]([^_-]*)(?:[_-]([^_-]*))?)?");

  private static ULocale defaultLocale = ULocale.US;

  public static ULocale getDefaultLocale() {
    return defaultLocale;
  }

  public static Locale getLocaleFromLocaleString(String s) {
    if (s == null) {
      return null;
    }

    Matcher matcher = LOCALE_PATTERN.matcher(s);
    matcher.find();

    String language = matcher.group(1);
    language = (language == null) ? "" : language;
    String country = matcher.group(2);
    country = (country == null) ? "" : country;
    String variant = matcher.group(3);
    variant = (variant == null) ? "" : variant;

    return new Locale(language, country, variant);
  }
  
  public static String getLocalizedMessageFromBundle(String bundleName, String key, Locale locale) {
    if (locale == null) {
      return ResourceBundle.getBundle(bundleName).getString(key);
    }
    return ResourceBundle.getBundle(bundleName, locale).getString(key);
  }

  public static String getLocalizedMessageFromBundleWithArguments(String bundleName, String key,
      String[] args, Locale locale) {
    String rawMesage = getLocalizedMessageFromBundle(bundleName, key, locale);
    if (args != null && args.length > 0) {
      return MessageFormat.format(rawMesage, args);
    }
    return rawMesage;
  }

  public static void setDefaultLocale(ULocale defaultLocale) {
    LocaleUtil.defaultLocale = defaultLocale;
  }
  
  private LocaleUtil() {
  }
}
