package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.data.JsData;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;

import java.util.List;
import java.util.Map;

/**
 * Contains a set of utility functions for data management, for example {@code parseExpression}.
 */

public class DataUtils {

  public static final String STATE_NAMESPACE = "http://www.butent.com/state";
  public static final String TABLE_NAMESPACE = "http://www.butent.com/table";
  public static final String VIEW_NAMESPACE = "http://www.butent.com/view";
  public static final String EXPRESSION_NAMESPACE = "http://www.butent.com/expression";

  public static final int ID_INDEX = -2;
  public static final int VERSION_INDEX = -3;

  public static final ValueType ID_TYPE = ValueType.LONG;
  public static final ValueType VERSION_TYPE = ValueType.LONG;
  
  public static final long NEW_ROW_ID = 0L;
  public static final long NEW_ROW_VERSION = 0L;

  private static int defaultAsyncThreshold = 100;
  private static int defaultSearchThreshold = 2;
  private static int defaultPagingThreshold = 20;

  private static int maxInitialRowSetSize = 50;
  
  public static BeeRow cloneRow(IsRow original, int columnCount) {
    Assert.notNull(original);
    String[] arr = new String[Assert.isPositive(columnCount)];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = original.getString(i);
    }

    BeeRow result = new BeeRow(original.getId(), original.getVersion(), arr);
    return result;
  }

  public static BeeRowSet cloneRowSet(BeeRowSet original) {
    Assert.notNull(original);
    BeeRowSet result = new BeeRowSet(original.getViewName(), original.getColumns());
    if (original.isEmpty()) {
      return result;
    }

    int cc = original.getNumberOfColumns();
    for (BeeRow row : original.getRows()) {
      result.addRow(cloneRow(row, cc));
    }
    return result;
  }
  
  public static IsRow createEmptyRow(int columnCount) {
    return new BeeRow(NEW_ROW_ID, new String[Assert.isPositive(columnCount)]);
  }

  @SuppressWarnings("unchecked")
  public static IsTable<?, ?> createTable(Object data, String... columnLabels) {
    Assert.notNull(data);
    IsTable<?, ?> table = null;

    if (data instanceof IsTable) {
      table = (IsTable<?, ?>) data;

    } else if (data instanceof String[][]) {
      table = new StringMatrix<TableColumn>((String[][]) data, columnLabels);

    } else if (data instanceof JsArrayString) {
      table = new JsData<TableColumn>((JsArrayString) data, columnLabels);

    } else if (data instanceof List) {
      Object el = BeeUtils.getQuietly((List<?>) data, 0);

      if (el instanceof ExtendedProperty) {
        table = new ExtendedPropertiesData((List<ExtendedProperty>) data, columnLabels);
      } else if (el instanceof Property) {
        table = new PropertiesData((List<Property>) data, columnLabels);
      } else if (el instanceof String[]) {
        table = new StringMatrix<TableColumn>((List<String[]>) data, columnLabels);
      }

    } else if (data instanceof Map) {
      table = new PropertiesData((Map<?, ?>) data, columnLabels);
    }

    Assert.notNull(table, "createTable: data not recognized");
    return table;
  }

  public static String defaultColumnId(int index) {
    if (BeeUtils.betweenExclusive(index, 0, 1000)) {
      return "col" + BeeUtils.toLeadingZeroes(index, 3);
    } else {
      return "col" + index;
    }
  }

  public static String defaultColumnLabel(int index) {
    return "Column " + index;
  }

  public static IsColumn getColumn(String columnId, List<? extends IsColumn> columns) {
    int index = getColumnIndex(columnId, columns);

    if (index >= 0) {
      return columns.get(index);
    } else {
      return null;
    }
  }

  public static int getColumnIndex(String columnId, List<? extends IsColumn> columns) {
    int index = BeeConst.UNDEF;
    if (BeeUtils.isEmpty(columnId) || BeeUtils.isEmpty(columns)) {
      return index;
    }

    for (int i = 0; i < columns.size(); i++) {
      if (BeeUtils.same(columns.get(i).getId(), columnId)) {
        index = i;
        break;
      }
    }
    return index;
  }

  public static String getColumnName(String input, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    if (!BeeUtils.isEmpty(columns)) {
      IsColumn column = getColumn(input, columns);

      if (column != null) {
        return column.getId();
      }
    }

    if (BeeUtils.same(input, idColumnName)) {
      return idColumnName;
    }
    if (BeeUtils.same(input, versionColumnName)) {
      return versionColumnName;
    }
    return null;
  }

  public static List<String> getColumnNames(List<? extends IsColumn> columns) {
    return getColumnNames(columns, null, null);
  }
  
  public static List<String> getColumnNames(List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    List<String> names = Lists.newArrayList();

    if (!BeeUtils.isEmpty(columns)) {
      for (IsColumn column : columns) {
        names.add(column.getId());
      }
    }

    if (!BeeUtils.isEmpty(idColumnName)) {
      names.add(idColumnName);
    }
    if (!BeeUtils.isEmpty(versionColumnName)) {
      names.add(versionColumnName);
    }
    return names;
  }
  
  public static ValueType getColumnType(String columnId, List<? extends IsColumn> columns) {
    ValueType type = null;
    IsColumn column = getColumn(columnId, columns);

    if (column != null) {
      type = column.getType();
    }
    return type;
  }

  public static int getDefaultAsyncThreshold() {
    return defaultAsyncThreshold;
  }

  public static int getDefaultPagingThreshold() {
    return defaultPagingThreshold;
  }

  public static int getDefaultSearchThreshold() {
    return defaultSearchThreshold;
  }

  public static Long getLong(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getLong(getColumnIndex(columnId, rowSet.getColumns()));
  }
  
  public static int getMaxInitialRowSetSize() {
    return maxInitialRowSetSize;
  }
  
  public static String getString(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getString(getColumnIndex(columnId, rowSet.getColumns()));
  }
  
  public static String getValue(IsRow row, int index, ValueType type) {
    if (row.isNull(index)) {
      return null;
    } else if (type == null || ValueType.isString(type)) {
      return row.getString(index);
    } else {
      return row.getValue(index, type).toString();
    }
  }
  
  public static boolean isNewRow(IsRow row) {
    return Assert.notNull(row).getId() == NEW_ROW_ID;
  }

  public static List<String> parseColumns(String input, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    } else {
      return parseColumns(Lists.newArrayList(NameUtils.NAME_SPLITTER.split(input)), columns,
          idColumnName, versionColumnName);
    }
  }

  public static List<String> parseColumns(List<String> input, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    Assert.notEmpty(columns);
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    List<String> result = Lists.newArrayList();
    for (String item : input) {
      String colName = getColumnName(item, columns, idColumnName, versionColumnName);
      if (!BeeUtils.isEmpty(colName) && !result.contains(colName)) {
        result.add(colName);
      }
    }

    if (result.isEmpty()) {
      return null;
    }
    return result;
  }
  
  public static Filter parseCondition(String cond, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(cond)) {
      List<String> parts = getParts(cond, "\\s+[oO][rR]\\s+");

      if (parts.size() > 1) {
        flt = Filter.or();
      } else {
        parts = getParts(cond, "\\s+[aA][nN][dD]\\s+");

        if (parts.size() > 1) {
          flt = Filter.and();
        }
      }
      if (!BeeUtils.isEmpty(flt)) {
        for (String part : parts) {
          Filter ff = parseCondition(part, columns, idColumnName, versionColumnName);

          if (ff == null) {
            flt = null;
            break;
          }
          ((CompoundFilter) flt).add(ff);
        }
      } else {
        String s = parts.get(0);
        String ptrn = "^\\s*" + CompoundType.NOT.toTextString() + "\\s*\\(\\s*(.*)\\s*\\)\\s*$";

        if (s.matches(ptrn)) {
          flt = parseCondition(s.replaceFirst(ptrn, "$1"), columns,
              idColumnName, versionColumnName);

          if (!BeeUtils.isEmpty(flt)) {
            flt = Filter.isNot(flt);
          }
        } else {
          flt = parseExpression(s, columns, idColumnName, versionColumnName);
        }
      }
    }
    return flt;
  }

  public static Filter parseExpression(String expr, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(expr)) {
      String s = expr.trim();
      IsColumn column = detectColumn(s, columns, idColumnName, versionColumnName);

      if (!BeeUtils.isEmpty(column)) {
        String colName = column.getId();
        String value = s.substring(colName.length()).trim();

        String pattern = "^" + CompoundType.NOT.toTextString() + "\\s*\\((.*)\\)$";
        boolean notMode = value.matches(pattern);

        if (notMode) {
          value = value.replaceFirst(pattern, "$1").trim();
        }
        Operator operator = Operator.detectOperator(value);
        boolean isOperator = !BeeUtils.isEmpty(operator);

        if (isOperator) {
          value = value.replaceFirst("^\\" + operator.toTextString() + "\\s*", "");
        } else {
          if (ValueType.isString(column.getType())) {
            operator = Operator.CONTAINS;
          } else {
            operator = Operator.EQ;
          }
        }
        IsColumn column2 = isColumn(value, columns);

        if (BeeUtils.same(colName, idColumnName)) {
          flt = ComparisonFilter.compareId(operator, value);

        } else if (BeeUtils.same(colName, versionColumnName)) {
          flt = ComparisonFilter.compareVersion(operator, value);

        } else if (column2 != null) {
          flt = ComparisonFilter.compareWithColumn(column, operator, column2);

        } else if (BeeUtils.isEmpty(value) && !isOperator) {
          flt = Filter.notEmpty(colName);

        } else {
          value = value.replaceFirst("^\"(.*)\"$", "$1") // Unquote
              .replaceAll("\"\"", "\"");

          if (BeeUtils.isEmpty(value)) {
            flt = Filter.isEmpty(colName);
          } else {
            flt = ComparisonFilter.compareWithValue(column, operator, value);
          }
        }
        if (notMode && flt != null) {
          flt = Filter.isNot(flt);
        }
      } else {
        LogUtils.warning(LogUtils.getDefaultLogger(), "Unknown column in expression: " + expr);
      }
    }
    return flt;
  }

  public static boolean same(IsRow r1, IsRow r2) {
    if (r1 == null) {
      return r2 == null;
    } else if (r2 == null) {
      return false;
    } else {
      return r1.getId() == r2.getId() && r1.getVersion() == r2.getVersion();
    }
  }
  
  public static void setValue(BeeRowSet rowSet, IsRow row, String columnId, String value) {
    row.setValue(getColumnIndex(columnId, rowSet.getColumns()), value);
  }
  
  public static void updateRow(IsRow target, IsRow source, int columnCount) {
    Assert.notNull(target);
    Assert.notNull(source);
    Assert.isPositive(columnCount);
    
    target.setId(source.getId());
    target.setVersion(source.getVersion());
    
    for (int i = 0; i < columnCount; i++) {
      target.setValue(i, source.getString(i));
    }
  }

  private static IsColumn detectColumn(String expr, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {

    if (BeeUtils.isEmpty(expr)) {
      return null;
    }
    IsColumn column = null;
    int len = 0;

    if (!BeeUtils.isEmpty(columns)) {
      for (IsColumn col : columns) {
        String s = col.getId();
        if (BeeUtils.startsWith(expr, s) && BeeUtils.hasLength(s, len + 1)) {
          column = col;
          len = s.length();
        }
      }
    }
    if (BeeUtils.hasLength(idColumnName, len + 1) && BeeUtils.startsWith(expr, idColumnName)) {
      column = new BeeColumn(ValueType.LONG, idColumnName);
      len = idColumnName.length();
    }
    if (BeeUtils.hasLength(versionColumnName, len + 1) &&
        BeeUtils.startsWith(expr, versionColumnName)) {
      column = new BeeColumn(ValueType.DATETIME, versionColumnName);
    }
    return column;
  }

  private static List<String> getParts(String expr, String pattern) {
    String s = expr;
    String ptrn = "^\\s*\\(\\s*(.+)\\s*\\)\\s*$"; // Unparenthesize
    String x = s.replaceFirst(ptrn, "$1");

    while (validPart(x)) {
      s = x;
      if (!x.matches(ptrn)) {
        break;
      }
      x = x.replaceFirst(ptrn, "$1");
    }
    List<String> parts = Lists.newArrayList();
    int cnt = s.split(pattern).length;
    boolean ok = false;

    for (int i = 2; i <= cnt; i++) {
      String[] pair = s.split(pattern, i);
      String right = pair[pair.length - 1];
      String left = s.substring(0, s.lastIndexOf(right)).replaceFirst(pattern + "$", "");

      if (validPart(left)) {
        parts.add(left);
        parts.addAll(getParts(right, pattern));
        ok = true;

      } else if (validPart(right)) {
        parts.addAll(getParts(left, pattern));
        parts.add(right);
        ok = true;
      }
      if (ok) {
        break;
      }
    }
    if (!ok) {
      parts.add(s);
    }
    return parts;
  }

  private static IsColumn isColumn(String expr, List<? extends IsColumn> columns) {
    if (!BeeUtils.isEmpty(expr) && !BeeUtils.isEmpty(columns)) {
      for (IsColumn col : columns) {
        if (BeeUtils.same(col.getId(), expr)) {
          return col;
        }
      }
    }
    return null;
  }

  private static boolean validPart(String expr) {
    String wh = expr;
    String regex = "^(.*)\"(.*)\"(.*)$";

    while (wh.matches(regex)) {
      String s = wh.replaceFirst(regex, "$2");
      wh = wh.replaceFirst(regex, "$1" + s.replaceAll("[\\(\\)]", "") + "$3");
    }
    if (wh.contains("\"")) {
      return false;
    }
    regex = "^(.*)\\((.*)\\)(.*)$";

    while (wh.matches(regex)) {
      wh = wh.replaceFirst(regex, "$1" + "$2" + "$3");
    }
    return !wh.matches(".*[\\(\\)].*");
  }

  private DataUtils() {
  }
}
