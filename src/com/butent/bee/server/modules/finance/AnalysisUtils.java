package com.butent.bee.server.modules.finance;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class AnalysisUtils {

  static String formatYearMonth(Integer year, Integer month) {
    String y = (year == null) ? null : TimeUtils.yearToString(year);
    String m = (month == null) ? null : TimeUtils.monthToString(month);

    return BeeUtils.join(BeeConst.STRING_POINT, y, m);
  }

  static Map<String, Integer> getIndexes(BeeRowSet rowSet) {
    Map<String, Integer> indexes = new HashMap<>();

    if (rowSet != null) {
      for (int i = 0; i < rowSet.getNumberOfColumns(); i++) {
        indexes.put(rowSet.getColumn(i).getId(), i);
      }
    }

    return indexes;
  }

  static MonthRange getRange(Integer yearFrom, Integer monthFrom,
      Integer yearUntil, Integer monthUntil) {

    if (isValidRange(yearFrom, monthFrom, yearUntil, monthUntil)) {
      Integer y1 = (yearFrom == null) ? Integer.valueOf(ANALYSIS_MIN_YEAR) : yearFrom;
      Integer y2 = (yearUntil == null && monthUntil == null)
          ? Integer.valueOf(ANALYSIS_MAX_YEAR) : yearUntil;

      return MonthRange.closed(y1, monthFrom, y2, monthUntil);

    } else {
      return null;
    }
  }

  static boolean isValidAbbreviation(String input) {
    return NameUtils.isIdentifier(input);
  }

  static boolean isValidRange(Integer yearFrom, Integer monthFrom,
      Integer yearUntil, Integer monthUntil) {

    if (yearFrom == null) {
      if (monthFrom != null) {
        return false;
      }

    } else if (isValidYear(yearFrom)) {
      if (monthFrom != null && !TimeUtils.isMonth(monthFrom)) {
        return false;
      }

    } else {
      return false;
    }

    if (yearUntil == null) {
      if (monthUntil == null) {
        return true;

      } else if (TimeUtils.isMonth(monthUntil)) {
        if (yearFrom == null) {
          return false;
        } else if (monthFrom == null) {
          return true;
        } else {
          return BeeUtils.isLeq(monthFrom, monthUntil);
        }

      } else {
        return false;
      }

    } else if (isValidYear(yearUntil)) {
      if (yearFrom != null && BeeUtils.isMore(yearFrom, yearUntil)) {
        return false;
      }

      if (monthUntil == null) {
        return true;

      } else if (TimeUtils.isMonth(monthUntil)) {
        if (yearFrom == null || monthFrom == null) {
          return true;
        } else if (Objects.equals(yearFrom, yearUntil)) {
          return BeeUtils.isLeq(monthFrom, monthUntil);
        } else {
          return true;
        }

      } else {
        return false;
      }

    } else {
      return false;
    }
  }

  private static boolean isValidYear(int year) {
    return year >= ANALYSIS_MIN_YEAR && year <= ANALYSIS_MAX_YEAR;
  }

  private AnalysisUtils() {
  }
}
