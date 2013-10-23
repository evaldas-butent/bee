package com.butent.bee.shared.modules.ec;

import com.google.common.collect.Range;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public final class EcUtils {

  private static final int currentYear = TimeUtils.today().getYear();

  private static final String IMAGE_DIR = "ec";

  public static String imageUrl(String name) {
    return Paths.buildPath(Paths.IMAGE_DIR, IMAGE_DIR, name);
  }

  public static boolean isProduced(Integer producedFrom, Integer producedTo, int year) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null || yearFrom > year) {
      return false;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }

    if (yearTo > yearFrom) {
      return year <= yearTo;
    } else {
      return year <= yearFrom;
    }
  }

  public static Integer normalizeYear(Integer year) {
    if (year == null) {
      return null;
    } else if (year > 1900 * 100) {
      return year / 100;
    } else if (year > 1900) {
      return year;
    } else {
      return null;
    }
  }

  public static String renderCents(int cents) {
    if (cents >= 0) {
      String s = BeeUtils.toLeadingZeroes(cents, 3);
      int len = s.length();
      return s.substring(0, len - 2) + BeeConst.STRING_POINT + s.substring(len - 2);
    } else {
      return BeeConst.STRING_MINUS + renderCents(-cents);
    }
  }

  public static String renderProduced(Integer producedFrom, Integer producedTo) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null) {
      return BeeConst.STRING_EMPTY;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }

    if (yearTo > yearFrom) {
      return yearFrom.toString() + BeeConst.STRING_MINUS + yearTo.toString();
    } else {
      return yearFrom.toString();
    }
  }

  public static String string(Double value) {
    return (value == null) ? null : BeeUtils.toString(value);
  }

  public static String string(Integer value) {
    return (value == null) ? null : value.toString();
  }

  public static int toCents(Double d) {
    return BeeUtils.isDouble(d) ? BeeUtils.round(d * 100) : 0;
  }

  public static Range<Integer> yearsProduced(Integer producedFrom, Integer producedTo) {
    Integer yearFrom = normalizeYear(producedFrom);
    if (yearFrom == null) {
      return null;
    }

    Integer yearTo = normalizeYear(producedTo);
    if (yearTo == null) {
      yearTo = currentYear;
    }

    return Range.closed(yearFrom, Math.max(yearFrom, yearTo));
  }
  
  private EcUtils() {
  }
}
