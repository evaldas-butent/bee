package com.butent.bee.shared.data;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;

import com.butent.bee.client.data.JsData;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.NegationFilter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;

import java.util.List;

/**
 * Contains a set of utility functions for data management, for example {@code parseExpression}.
 */

public class DataUtils {

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
      Object el = BeeUtils.listGetQuietly((List<?>) data, 0);

      if (el instanceof ExtendedProperty) {
        table = new ExtendedPropertiesData((List<ExtendedProperty>) data, columnLabels);
      } else if (el instanceof Property) {
        table = new PropertiesData((List<Property>) data, columnLabels);
      }
    }

    Assert.notNull(table);
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

  public static Filter parseCondition(String cond, List<? extends IsColumn> columns) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(cond)) {
      List<String> parts = getParts(cond, "\\s+[oO][rR]\\s+");

      if (parts.size() > 1) {
        flt = CompoundFilter.or();
      } else {
        parts = getParts(cond, "\\s+[aA][nN][dD]\\s+");

        if (parts.size() > 1) {
          flt = CompoundFilter.and();
        }
      }
      if (!BeeUtils.isEmpty(flt)) {
        for (String part : parts) {
          ((CompoundFilter) flt).add(parseCondition(part, columns));
        }
      } else {
        String s = parts.get(0);
        String ptrn = "^\\s*" + CompoundType.NOT.toTextString() + "\\s*\\(\\s*(.*)\\s*\\)\\s*$";

        if (s.matches(ptrn)) {
          flt = parseCondition(s.replaceFirst(ptrn, "$1"), columns);

          if (!BeeUtils.isEmpty(flt)) {
            flt = new NegationFilter(flt);
          }
        } else {
          flt = parseExpression(s, columns);
        }
      }
    }
    return flt;
  }

  public static Filter parseExpression(String expr, List<? extends IsColumn> columns) {
    Filter flt = null;

    if (!BeeUtils.isEmpty(expr)) {
      String s = expr.trim();
      IsColumn column = detectColumn(s, columns);

      if (!BeeUtils.isEmpty(column)) {
        String colName = column.getId();
        String value = s.substring(colName.length()).trim();

        String pattern = "^" + CompoundType.NOT.toTextString() + "\\s*\\(\\s*(.*)\\s*\\)$";
        boolean notMode = value.matches(pattern);

        if (notMode) {
          value = value.replaceFirst(pattern, "$1");
        }
        Operator operator = Operator.detectOperator(value);

        if (!BeeUtils.isEmpty(operator)) {
          value = value.replaceFirst("^\\" + operator.toTextString() + "\\s*", "");
        } else {
          if (ValueType.TEXT == column.getType()) {
            operator = Operator.LIKE;
          } else {
            operator = Operator.EQ;
          }
        }
        if (isColumn(value, columns)) {
          flt = ComparisonFilter.compareWithColumn(colName, operator, value);

        } else {
          value = value.replaceFirst("^\"(.*)\"$", "$1") // Unquote
              .replaceAll("\"\"", "\"");

          if (BeeUtils.isEmpty(value)) {
            flt = new ColumnIsEmptyFilter(colName);
          } else {
            flt = ComparisonFilter.compareWithValue(colName, operator,
                  BeeUtils.isNumeric(value) ? BeeUtils.toDouble(value) : value);
          }
        }
        if (notMode) {
          flt = new NegationFilter(flt);
        }
      } else {
        LogUtils.warning(LogUtils.getDefaultLogger(), "Wrong condition expression: " + expr);
      }
    }
    return flt;
  }

  private static IsColumn detectColumn(String expr, List<? extends IsColumn> columns) {
    IsColumn column = null;

    if (!BeeUtils.isEmpty(expr) && !BeeUtils.isEmpty(columns)) {
      for (IsColumn col : columns) {
        String s = col.getId();

        if (BeeUtils.same(s, expr.substring(0, s.length()))
            && (column == null || s.length() > column.getId().length())) {
          column = col;
        }
      }
    }
    return column;
  }

  private static List<String> getParts(String expr, String pattern) {
    String s = expr.replaceFirst("^\\s*\\(\\s*(.+)\\s*\\)\\s*$", "$1"); // Unparenthesize
    if (!validPart(s) && validPart(expr)) {
      s = expr;
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

  private static boolean isColumn(String expr, List<? extends IsColumn> columns) {
    if (!BeeUtils.isEmpty(expr) && !BeeUtils.isEmpty(columns)) {
      for (IsColumn column : columns) {
        if (BeeUtils.same(column.getId(), expr)) {
          return true;
        }
      }
    }
    return false;
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
