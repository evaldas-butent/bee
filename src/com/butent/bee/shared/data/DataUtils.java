package com.butent.bee.shared.data;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.CompoundType;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Contains a set of utility functions for data management, for example {@code parseExpression}.
 */

public class DataUtils {

  public static final String STATE_NAMESPACE = "http://www.butent.com/state";
  public static final String TABLE_NAMESPACE = "http://www.butent.com/table";
  public static final String VIEW_NAMESPACE = "http://www.butent.com/view";
  public static final String EXPRESSION_NAMESPACE = "http://www.butent.com/expression";
  public static final String MENU_NAMESPACE = "http://www.butent.com/menu";

  public static final String ID_TAG = "ID";
  public static final String VERSION_TAG = "VERSION";

  public static final int ID_INDEX = -2;
  public static final int VERSION_INDEX = -3;

  public static final ValueType ID_TYPE = ValueType.LONG;
  public static final ValueType VERSION_TYPE = ValueType.LONG;

  public static final long NEW_ROW_ID = 0L;
  public static final long NEW_ROW_VERSION = 0L;

  private static BeeLogger logger = LogUtils.getLogger(DataUtils.class);

  private static final Predicate<Long> IS_ID = new Predicate<Long>() {
    @Override
    public boolean apply(Long input) {
      return isId(input);
    }
  };

  private static final char ID_LIST_SEPARATOR = ',';

  private static final Joiner ID_JOINER = Joiner.on(ID_LIST_SEPARATOR).skipNulls();
  private static final Splitter ID_SPLITTER =
      Splitter.on(ID_LIST_SEPARATOR).omitEmptyStrings().trimResults();

  private static int defaultAsyncThreshold = 100;
  private static int defaultSearchThreshold = 2;
  private static int defaultPagingThreshold = 20;

  private static int maxInitialRowSetSize = 50;

  public static Filter anyItemContains(String column, Class<? extends Enum<?>> clazz,
      String value) {
    Assert.notEmpty(column);
    Assert.notNull(clazz);
    Assert.notEmpty(value);

    List<Filter> filters = Lists.newArrayList();

    String item;
    for (Enum<?> constant : clazz.getEnumConstants()) {
      if (constant instanceof HasCaption) {
        item = ((HasCaption) constant).getCaption();
      } else {
        item = constant.name();
      }

      if (BeeUtils.containsSame(item, value)) {
        filters.add(ComparisonFilter.isEqual(column, new IntegerValue(constant.ordinal())));
      }
    }

    return Filter.or(filters);
  }
  
  public static long assertId(Long id) {
    Assert.isTrue(isId(id), "invalid row id");
    return id;
  }

  public static String buildIdList(BeeRowSet rowSet) {
    if (rowSet == null) {
      return null;
    } else {
      return buildIdList(getRowIds(rowSet));
    }
  }

  public static String buildIdList(Collection<Long> ids) {
    if (BeeUtils.isEmpty(ids)) {
      return null;
    } else {
      return ID_JOINER.join(Iterables.filter(ids, IS_ID));
    }
  }

  public static String buildIdList(Long... ids) {
    if (ids == null) {
      return null;
    } else {
      return buildIdList(Lists.newArrayList(ids));
    }
  }

  public static BeeRow cloneRow(IsRow original) {
    Assert.notNull(original);
    String[] arr = new String[original.getNumberOfCells()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = original.getString(i);
    }

    BeeRow result = new BeeRow(original.getId(), original.getVersion(), arr);
    if (!BeeUtils.isEmpty(original.getProperties())) {
      result.setProperties(original.getProperties().copy());
    }
    return result;
  }

  public static BeeRowSet cloneRowSet(BeeRowSet original) {
    Assert.notNull(original);
    BeeRowSet result = new BeeRowSet(original.getViewName(), original.getColumns());
    if (original.isEmpty()) {
      return result;
    }

    for (BeeRow row : original.getRows()) {
      result.addRow(cloneRow(row));
    }
    original.copyProperties(result);
    return result;
  }

  public static boolean contains(List<? extends IsColumn> columns, String columnId) {
    return !BeeConst.isUndef(getColumnIndex(columnId, columns));
  }

  public static BeeRow createEmptyRow(int columnCount) {
    return new BeeRow(NEW_ROW_ID, new String[Assert.isPositive(columnCount)]);
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

  public static boolean equals(IsRow r1, IsRow r2) {
    if (r1 == null) {
      return r2 == null;
    } else if (r2 == null) {
      return false;
    } else if (r1 == r2) {
      return true;

    } else if (r1.getId() != r2.getId()) {
      return false;
    } else if (r1.getVersion() != r2.getVersion()) {
      return false;
    } else if (r1.getNumberOfCells() != r2.getNumberOfCells()) {
      return false;

    } else {
      for (int i = 0; i < r1.getNumberOfCells(); i++) {
        if (!BeeUtils.equalsTrimRight(r1.getString(i), r2.getString(i))) {
          return false;
        }
      }
      return true;
    }
  }

  public static List<BeeRow> filterRows(BeeRowSet rowSet, String columnId, String value) {
    List<BeeRow> result = Lists.newArrayList();
    int index = rowSet.getColumnIndex(columnId);

    for (BeeRow row : rowSet.getRows()) {
      if (BeeUtils.equalsTrim(row.getString(index), value)) {
        result.add(row);
      }
    }
    return result;
  }

  public static <T extends IsColumn> T getColumn(String columnId, List<T> columns) {
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

  public static String getColumnLabel(String columnId, List<? extends IsColumn> columns) {
    if (BeeUtils.isEmpty(columnId) || BeeUtils.isEmpty(columns)) {
      return null;
    }

    for (IsColumn column : columns) {
      if (BeeUtils.same(column.getId(), columnId)) {
        return column.getLabel();
      }
    }
    return null;
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

  public static int getColumnPrecision(String columnId, List<? extends IsColumn> columns) {
    IsColumn column = getColumn(columnId, columns);
    return (column == null) ? BeeConst.UNDEF : column.getPrecision();
  }

  public static List<BeeColumn> getColumns(List<BeeColumn> columns, int... indexes) {
    if (indexes == null) {
      return columns;
    }

    List<BeeColumn> result = Lists.newArrayList();
    for (int index : indexes) {
      if (index >= 0 && index < columns.size()) {
        result.add(columns.get(index));
      }
    }
    return result;
  }

  public static List<BeeColumn> getColumns(List<BeeColumn> columns, String... colNames) {
    if (colNames == null) {
      return columns;
    }

    List<BeeColumn> result = Lists.newArrayList();
    for (String colName : colNames) {
      BeeColumn column = getColumn(colName, columns);
      if (column != null) {
        result.add(column);
      }
    }
    return result;
  }

  public static ValueType getColumnType(String columnId, List<? extends IsColumn> columns) {
    ValueType type = null;
    IsColumn column = getColumn(columnId, columns);

    if (column != null) {
      type = column.getType();
    }
    return type;
  }

  public static JustDate getDate(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getDate(getColumnIndex(columnId, rowSet.getColumns()));
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

  public static List<Long> getDistinct(BeeRowSet rowSet, String columnId) {
    return getDistinct(rowSet.getRows().getList(), rowSet.getColumnIndex(columnId));
  }

  public static List<Long> getDistinct(Collection<? extends IsRow> rows, int index) {
    return getDistinct(rows, index, null);
  }

  public static List<Long> getDistinct(Collection<? extends IsRow> rows, int index, Long exclude) {
    List<Long> result = Lists.newArrayList();
    if (BeeUtils.isEmpty(rows)) {
      return result;
    }

    for (IsRow row : rows) {
      Long value = row.getLong(index);
      if (value != null && !value.equals(exclude) && !result.contains(value)) {
        result.add(value);
      }
    }
    return result;
  }

  public static Integer getInteger(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getInteger(getColumnIndex(columnId, rowSet.getColumns()));
  }

  public static Long getLong(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getLong(getColumnIndex(columnId, rowSet.getColumns()));
  }

  public static int getMaxInitialRowSetSize() {
    return maxInitialRowSetSize;
  }

  public static String getMaxValue(IsColumn column) {
    if (column == null) {
      return null;
    }

    ValueType type = column.getType();
    if (type == null) {
      return null;
    }

    String value;
    switch (type) {
      case INTEGER:
        value = BeeUtils.toString(Integer.MAX_VALUE);
        break;

      case LONG:
        value = BeeUtils.toString(Long.MAX_VALUE);
        break;

      case DECIMAL:
        int precision = column.getPrecision();
        int scale = column.getScale();

        if (precision > 0 && scale <= 0) {
          value = BeeUtils.toString(Math.pow(10, precision) - 1);
        } else if (precision > 0 && scale < precision) {
          value = BeeUtils.toString(Math.pow(10, precision - scale) - Math.pow(10, -scale));
        } else {
          value = null;
        }
        break;

      default:
        value = null;
    }
    return value;
  }

  public static String getMinValue(IsColumn column) {
    if (column == null) {
      return null;
    }

    ValueType type = column.getType();
    if (type == null) {
      return null;
    }

    String value;
    switch (type) {
      case INTEGER:
        value = BeeUtils.toString(Integer.MIN_VALUE);
        break;

      case LONG:
        value = BeeUtils.toString(Long.MIN_VALUE);
        break;

      case DECIMAL:
        int precision = column.getPrecision();
        int scale = column.getScale();

        if (precision > 0 && scale <= 0) {
          value = BeeUtils.toString(-Math.pow(10, precision) + 1);
        } else if (precision > 0 && scale < precision) {
          value = BeeUtils.toString(-Math.pow(10, precision - scale) + Math.pow(10, -scale));
        } else {
          value = null;
        }
        break;

      default:
        value = null;
    }
    return value;
  }

  public static List<Long> getRowIds(BeeRowSet rowSet) {
    List<Long> result = Lists.newArrayList();
    for (BeeRow row : rowSet.getRows()) {
      result.add(row.getId());
    }
    return result;
  }

  public static String getString(BeeRowSet rowSet, IsRow row, String columnId) {
    return row.getString(getColumnIndex(columnId, rowSet.getColumns()));
  }

  public static String getString(IsRow row, int index) {
    if (row == null) {
      return null;
    } else if (index == ID_INDEX) {
      return BeeUtils.toString(row.getId());
    } else if (index == VERSION_INDEX) {
      return BeeUtils.toString(row.getVersion());
    } else {
      return row.getString(index);
    }
  }

  public static BeeRowSet getUpdated(String viewName, List<BeeColumn> columns, IsRow oldRow,
      IsRow newRow) {
    String oldValue;
    String newValue;

    List<BeeColumn> updatedColumns = Lists.newArrayList();
    List<String> oldValues = Lists.newArrayList();
    List<String> newValues = Lists.newArrayList();

    for (int i = 0; i < columns.size(); i++) {
      BeeColumn column = columns.get(i);
      if (!column.isWritable()) {
        continue;
      }

      oldValue = oldRow.getString(i);
      newValue = newRow.getString(i);

      if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
        updatedColumns.add(column);
        oldValues.add(oldValue);
        newValues.add(newValue);
      }
    }
    if (updatedColumns.isEmpty()) {
      return null;
    }

    BeeRowSet rs = new BeeRowSet(viewName, updatedColumns);
    rs.addRow(oldRow.getId(), oldRow.getVersion(), oldValues);
    for (int i = 0; i < rs.getNumberOfColumns(); i++) {
      rs.getRow(0).preliminaryUpdate(i, newValues.get(i));
    }
    return rs;
  }

  public static boolean hasId(IsRow row) {
    return row != null && isId(row.getId());
  }
  
  public static boolean isEmpty(BeeRowSet rowSet) {
    return rowSet == null || rowSet.isEmpty();
  }

  public static boolean isId(Long id) {
    return id != null && id > 0;
  }

  public static boolean isNewRow(IsRow row) {
    return row != null && row.getId() == NEW_ROW_ID;
  }

  public static String join(String viewName, IsRow row, List<String> colNames, String separator) {
    Assert.notEmpty(viewName);
    DataInfo dataInfo = Data.getDataInfo(viewName);
    if (dataInfo == null) {
      return null;
    }

    Assert.notNull(row);
    Assert.notEmpty(colNames);

    StringBuilder sb = new StringBuilder();
    String sep = BeeUtils.nvl(separator, BeeConst.DEFAULT_LIST_SEPARATOR);

    for (String colName : colNames) {
      int i = dataInfo.getColumnIndex(colName);
      Assert.nonNegative(i, "column not found: " + colName);

      String value = transform(row, i, dataInfo.getColumns().get(i).getType());
      if (!BeeUtils.isEmpty(value)) {
        if (sb.length() > 0) {
          sb.append(sep);
        }
        sb.append(value.trim());
      }
    }
    return sb.toString();
  }

  public static String join(DataInfo dataInfo, IsRow row, String separator) {
    Assert.notNull(dataInfo);
    Assert.notNull(row);

    StringBuilder sb = new StringBuilder();
    String sep = BeeUtils.nvl(separator, BeeConst.DEFAULT_LIST_SEPARATOR);

    for (int i = 0; i < dataInfo.getColumnCount(); i++) {
      BeeColumn column = dataInfo.getColumns().get(i);
      if (dataInfo.hasRelation(column.getId())) {
        continue;
      }

      String value = transform(row, i, column.getType());
      if (!BeeUtils.isEmpty(value)) {
        if (sb.length() > 0) {
          sb.append(sep);
        }
        sb.append(value.trim());
      }
    }
    return sb.toString();
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

  public static List<String> parseColumns(String input, List<? extends IsColumn> columns,
      String idColumnName, String versionColumnName) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    } else {
      return parseColumns(Lists.newArrayList(NameUtils.NAME_SPLITTER.split(input)), columns,
          idColumnName, versionColumnName);
    }
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
      if (flt != null) {
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

          if (flt != null) {
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

      if (column != null) {
        String colName = column.getId();
        String value = s.substring(colName.length()).trim();

        String pattern = "^" + CompoundType.NOT.toTextString() + "\\s*\\((.*)\\)$";
        boolean notMode = value.matches(pattern);

        if (notMode) {
          value = value.replaceFirst(pattern, "$1").trim();
        }
        Operator operator = Operator.detectOperator(value);
        boolean isOperator = (operator != null);

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
        logger.warning("Unknown column in expression: " + expr);
      }
    }
    return flt;
  }

  public static Filter parseFilter(String input, DataInfo.Provider provider, String viewName) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    DataInfo dataInfo = provider.getDataInfo(viewName, true);
    return (dataInfo == null) ? null : dataInfo.parseFilter(input);
  }

  public static List<Long> parseIdList(String input) {
    List<Long> result = Lists.newArrayList();
    if (BeeUtils.isEmpty(input)) {
      return result;
    }

    for (String s : ID_SPLITTER.split(input)) {
      Long id = BeeUtils.toLongOrNull(s);
      if (isId(id)) {
        result.add(id);
      }
    }
    return result;
  }

  public static Set<Long> parseIdSet(String input) {
    Set<Long> result = Sets.newHashSet();
    if (BeeUtils.isEmpty(input)) {
      return result;
    }

    for (String s : ID_SPLITTER.split(input)) {
      Long id = BeeUtils.toLongOrNull(s);
      if (isId(id)) {
        result.add(id);
      }
    }
    return result;
  }
  
  public static Order parseOrder(String input, DataInfo.Provider provider, String viewName) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    DataInfo dataInfo = provider.getDataInfo(viewName, true);
    return (dataInfo == null) ? null : dataInfo.parseOrder(input);
  }

  public static List<BeeRow> restoreRows(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }

    List<BeeRow> result = Lists.newArrayList();
    String[] arr = Codec.beeDeserializeCollection(s);

    if (arr != null) {
      for (String r : arr) {
        result.add(BeeRow.restore(r));
      }
    }
    return result;
  }

  public static boolean sameId(IsRow r1, IsRow r2) {
    if (r1 == null) {
      return r2 == null;
    } else if (r2 == null) {
      return false;
    } else {
      return r1.getId() == r2.getId();
    }
  }

  public static boolean sameIdAndVersion(IsRow r1, IsRow r2) {
    if (r1 == null) {
      return r2 == null;
    } else if (r2 == null) {
      return false;
    } else {
      return r1.getId() == r2.getId() && r1.getVersion() == r2.getVersion();
    }
  }
  
  public static boolean sameIdSet(String s, Collection<Long> col) {
    Set<Long> set = parseIdSet(s);
    if (col == null) {
      return set.isEmpty();
    } else {
      return set.size() == col.size() && set.containsAll(col);
    }
  }

  public static int setDefaults(IsRow row, Collection<String> colNames, List<BeeColumn> columns,
      Defaults defaults) {
    int result = 0;
    if (row == null || BeeUtils.isEmpty(colNames) || BeeUtils.isEmpty(columns)
        || defaults == null) {
      return result;
    }

    for (String colName : colNames) {
      int index = getColumnIndex(colName, columns);
      if (BeeConst.isUndef(index)) {
        continue;
      }

      BeeColumn column = columns.get(index);
      if (!column.hasDefaults()) {
        continue;
      }

      Object value = defaults.getValue(column.getDefaults().getA(), column.getDefaults().getB());
      if (value == null) {
        continue;
      }

      row.setValue(index, Value.getValue(value));
      result++;
    }
    return result;
  }

  public static void setValue(BeeRowSet rowSet, IsRow row, String columnId, String value) {
    row.setValue(getColumnIndex(columnId, rowSet.getColumns()), value);
  }

  public static String transform(IsRow row, int index, ValueType type) {
    if (row.isNull(index)) {
      return null;
    } else if (type == null || ValueType.isString(type)) {
      return row.getString(index);
    } else {
      return row.getValue(index, type).toString();
    }
  }

  public static List<String> translate(List<String> input, List<? extends IsColumn> columns,
      IsRow row) {
    List<String> result = Lists.newArrayList();

    if (BeeUtils.isEmpty(input) || BeeUtils.isEmpty(columns)) {
      BeeUtils.overwrite(result, input);
      return result;
    }

    for (String expr : input) {
      int index = getColumnIndex(expr, columns);

      if (BeeConst.isUndef(index)) {
        result.add(expr);
      } else if (row == null) {
        result.add(BeeConst.STRING_EMPTY);
      } else {
        result.add(row.getString(index));
      }
    }
    return result;
  }

  public static void updateRow(IsRow target, IsRow source) {
    Assert.notNull(target);
    Assert.notNull(source);

    target.setId(source.getId());
    target.setVersion(source.getVersion());

    for (int i = 0; i < target.getNumberOfCells(); i++) {
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
