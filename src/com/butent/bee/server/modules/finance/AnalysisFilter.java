package com.butent.bee.server.modules.finance;

import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.modules.finance.analysis.AnalysisValue;
import com.butent.bee.shared.utils.BeeUtils;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class AnalysisFilter {

  static boolean allMatch(AnalysisValue analysisValue, Collection<AnalysisFilter> analysisFilters) {
    if (!BeeUtils.isEmpty(analysisFilters)) {
      for (AnalysisFilter analysisFilter : analysisFilters) {
        if (analysisFilter != null && !analysisFilter.matches(analysisValue)) {
          return false;
        }
      }
    }
    return true;
  }

  static List<AnalysisFilter> list(AnalysisFilter... analysisFilters) {
    List<AnalysisFilter> result = new ArrayList<>();

    if (analysisFilters != null) {
      for (AnalysisFilter analysisFilter : analysisFilters) {
        if (analysisFilter != null && !analysisFilter.isEmpty()) {
          result.add(analysisFilter);
        }
      }
    }

    return result;
  }

  static Predicate<AnalysisValue> predicate(AnalysisFilter analysisFilter) {
    return (analysisFilter == null) ? null : analysisFilter::matches;
  }

  private static final String KEY_EMPLOYEE = "Employee";

  private final Map<String, Long> main = new HashMap<>();

  private final Collection<Map<String, Long>> include = new HashSet<>();
  private final Collection<Map<String, Long>> exclude = new HashSet<>();

  AnalysisFilter(BeeRow row, Map<String, Integer> indexes, String employeeColumnName) {
    main.putAll(parse(row, indexes, employeeColumnName));
  }

  IsCondition getBudgetCondition(String source, String employeeColumn) {
    HasConditions conditions = SqlUtils.and();

    Collection<String> dimensionColumns = Dimensions.getObservedRelationColumns();

    if (!main.isEmpty()) {
      conditions.add(getCondition(main, source, employeeColumn, dimensionColumns));
    }

    if (!include.isEmpty()) {
      HasConditions ic = SqlUtils.or();
      include.forEach(map -> ic.add(getCondition(map, source, employeeColumn, dimensionColumns)));
      conditions.add(ic);
    }

    if (!exclude.isEmpty()) {
      HasConditions ec = SqlUtils.or();
      exclude.forEach(map -> ec.add(getCondition(map, source, employeeColumn, dimensionColumns)));
      conditions.add(SqlUtils.not(ec));
    }

    return normalize(conditions);
  }

  private static IsCondition getCondition(Map<String, Long> map, String source,
      String employeeColumnName, Collection<String> dimensionColumnNames) {

    HasConditions conditions = SqlUtils.and();

    map.forEach((k, v) -> {
      if (k.equals(employeeColumnName)) {
        conditions.add(SqlUtils.equals(source, employeeColumnName, v));
      } else if (dimensionColumnNames.contains(k)) {
        conditions.add(SqlUtils.equals(source, k, v));
      }
    });

    return normalize(conditions);
  }

  private static HasConditions normalize(HasConditions conditions) {
    if (conditions == null || conditions.isEmpty()) {
      return null;
    } else {
      return conditions;
    }
  }

  boolean isEmpty() {
    return main.isEmpty() && include.isEmpty() && exclude.isEmpty();
  }

  boolean matches(AnalysisValue analysisValue) {
    if (analysisValue == null) {
      return false;
    }

    if (isEmpty()) {
      return true;
    }
    return true;
  }

  void setSubFilters(Collection<BeeRow> data, Map<String, Integer> indexes) {
    if (!BeeUtils.isEmpty(data) && !BeeUtils.isEmpty(indexes)) {
      setSubFilters(data, indexes, COL_ANALYSIS_FILTER_EMPLOYEE, COL_ANALYSIS_FILTER_INCLUDE);
    }
  }

  void setSubFilters(Collection<BeeRow> data, Map<String, Integer> indexes,
      String employeeColumnName, String includeColumnName) {

    if (!include.isEmpty()) {
      include.clear();
    }
    if (!exclude.isEmpty()) {
      exclude.clear();
    }

    Integer includeIndex = indexes.get(includeColumnName);

    for (BeeRow row : data) {
      Map<String, Long> map = parse(row, indexes, employeeColumnName);

      if (!map.isEmpty()) {
        if (row.isTrue(includeIndex)) {
          include.add(map);
        } else {
          exclude.add(map);
        }
      }
    }
  }

  private static Map<String, Long> parse(BeeRow row, Map<String, Integer> indexes,
      String employeeColumnName) {

    Map<String, Long> result = new HashMap<>();

    put(result, KEY_EMPLOYEE, read(row, indexes, employeeColumnName));

    if (Dimensions.getObserved() > 0) {
      for (int ordinal = 1; ordinal <= Dimensions.getObserved(); ordinal++) {
        String column = Dimensions.getRelationColumn(ordinal);
        put(result, column, read(row, indexes, column));
      }
    }

    return result;
  }

  private static Long read(BeeRow row, Map<String, Integer> indexes, String column) {
    Integer index = indexes.get(column);

    if (index == null) {
      return null;
    } else {
      return row.getLong(index);
    }
  }

  private static void put(Map<String, Long> map, String key, Long value) {
    if (DataUtils.isId(value)) {
      map.put(key, value);
    }
  }
}
