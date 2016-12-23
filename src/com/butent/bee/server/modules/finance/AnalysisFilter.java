package com.butent.bee.server.modules.finance;

import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.utils.BeeUtils;

import static com.butent.bee.shared.modules.finance.FinanceConstants.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class AnalysisFilter {

  private static final String KEY_EMPLOYEE = "Employee";

  private final Map<String, Long> main = new HashMap<>();

  private final Collection<Map<String, Long>> include = new HashSet<>();
  private final Collection<Map<String, Long>> exclude = new HashSet<>();

  AnalysisFilter(BeeRow row, Map<String, Integer> indexes, String employeeColumnName) {
    main.putAll(parse(row, indexes, employeeColumnName));
  }

  IsCondition getBudgetCondition(String source) {
    return null;
  }

  boolean isEmpty() {
    return main.isEmpty() && include.isEmpty() && exclude.isEmpty();
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
