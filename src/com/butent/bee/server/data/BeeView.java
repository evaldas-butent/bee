package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.CompoundCondition;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.NegationFilter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implements database view management - contains parameters for views and their fields and methods
 * for doing operations with them.
 */

public class BeeView {

  private class ViewField {

    private final String table;
    private final String alias;
    private final String field;
    private final DataType type;
    private final boolean notNull;
    private boolean sourceField;
    private String targetAlias;

    public ViewField(String tbl, String als, String fld, DataType type, boolean notNull,
        boolean sourceField) {
      this.table = tbl;
      this.alias = als;
      this.field = fld;
      this.type = type;
      this.notNull = notNull;
      this.sourceField = sourceField;
    }

    public String getAlias() {
      return alias;
    }

    public String getField() {
      return field;
    }

    public String getTable() {
      return table;
    }

    public String getTargetAlias() {
      return targetAlias;
    }

    public DataType getType() {
      return type;
    }

    public boolean isNotNull() {
      return notNull;
    }

    public boolean isSourceField() {
      return sourceField;
    }

    public void setTargetAlias(String als) {
      this.targetAlias = als;
    }
  }

  private static final String JOIN_MASK = "-<>+";
  private static final int NAME = 0;
  private static final int EXPRESSION = 1;
  private static final int LOCALE = 2;
  private static final int LOCALE_ALIAS = 3;

  private final String name;
  private final String source;
  private final String sourceIdName;
  private final boolean readOnly;
  private final SqlSelect query;
  private final Map<String, String[]> columns = Maps.newLinkedHashMap();
  private final Map<String, ViewField> expressions = Maps.newHashMap();
  private final Map<String, Boolean> orders = Maps.newLinkedHashMap();

  BeeView(String name, String source, String idName, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(source);
    Assert.notEmpty(idName);

    this.name = name;
    this.source = source;
    this.sourceIdName = idName;
    this.readOnly = readOnly;
    this.query = new SqlSelect().addFrom(source);
  }

  public String getAlias(String colName) {
    if (BeeUtils.isEmpty(getLocale(colName))) {
      return getViewField(colName).getAlias();
    }
    return getLocaleAlias(colName);
  }

  public int getColumnCount() {
    return columns.size();
  }

  public Collection<String> getColumns() {
    Collection<String> cols = Lists.newArrayList();

    for (String[] col : columns.values()) {
      cols.add(col[NAME]);
    }
    return cols;
  }

  public IsCondition getCondition(ColumnValueFilter filter) {
    IsCondition condition = null;
    String colName = filter.getColumn();
    String als = getAlias(colName);

    if (!BeeUtils.isEmpty(als)) {
      IsExpression src = SqlUtils.field(als, getField(colName));
      Operator op = filter.getOperator();
      Value value = filter.getValue();

      if (Operator.LIKE == op) {
        String val = value.getString();

        if (filter.hasLikeCharacters(val)) { // TODO: create LIKE and CONTAINS operators
          condition = SqlUtils.like(src, val);
        } else {
          condition = SqlUtils.contains(src, val);
        }
      } else {
        condition = SqlUtils.compare(src, op, SqlUtils.constant(value));
      }
    } else {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Column " + colName + " is not initialized");
    }
    return condition;
  }

  public IsCondition getCondition(ColumnColumnFilter filter) {
    IsCondition condition = null;
    String firstName = filter.getFirstColumn().toLowerCase();
    String secondName = filter.getSecondColumn().toLowerCase();
    String err = null;
    String als = getAlias(firstName);

    if (!BeeUtils.isEmpty(als)) {
      IsExpression firstSrc = SqlUtils.field(als, getField(firstName));
      als = getAlias(secondName);

      if (!BeeUtils.isEmpty(als)) {
        IsExpression secondSrc = SqlUtils.field(als, getField(secondName));
        condition = SqlUtils.compare(firstSrc, filter.getOperator(), secondSrc);
      } else {
        err = secondName;
      }
    } else {
      err = firstName;
    }
    if (!BeeUtils.isEmpty(err)) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Column " + err + " is not initialized");
    }
    return condition;
  }

  public IsCondition getCondition(ColumnIsEmptyFilter filter) {
    IsCondition condition = null;
    String colName = filter.getColumn();
    String als = getAlias(colName);

    if (!BeeUtils.isEmpty(als)) {
      String fld = getField(colName);
      condition = SqlUtils.equal(als, fld, getType(colName).getEmptyValue());

      if (!isNotNull(colName)) {
        condition = SqlUtils.or(SqlUtils.isNull(als, fld), condition);
      }
    } else {
      condition = SqlUtils.sqlTrue();
    }
    return condition;
  }

  public IsCondition getCondition(NegationFilter filter) {
    return SqlUtils.not(getCondition(filter.getSubFilter()));
  }

  public IsCondition getCondition(CompoundFilter filter) {
    CompoundCondition condition = null;
    List<Filter> subFilters = filter.getSubFilters();

    if (!BeeUtils.isEmpty(subFilters)) {
      switch (filter.getJoinType()) {
        case AND:
          condition = SqlUtils.and();
          break;
        case OR:
          condition = SqlUtils.or();
          break;
        default:
          Assert.unsupported();
          break;
      }
      for (Filter subFilter : subFilters) {
        condition.add(getCondition(subFilter));
      }
    }
    return condition;
  }

  public IsCondition getCondition(Filter filter) {
    if (filter != null) {
      String clazz = BeeUtils.getClassName(filter.getClass());

      if (BeeUtils.getClassName(ColumnValueFilter.class).equals(clazz)) {
        return getCondition((ColumnValueFilter) filter);

      } else if (BeeUtils.getClassName(ColumnColumnFilter.class).equals(clazz)) {
        return getCondition((ColumnColumnFilter) filter);

      } else if (BeeUtils.getClassName(ColumnIsEmptyFilter.class).equals(clazz)) {
        return getCondition((ColumnIsEmptyFilter) filter);

      } else if (BeeUtils.getClassName(NegationFilter.class).equals(clazz)) {
        return getCondition((NegationFilter) filter);

      } else if (BeeUtils.getClassName(CompoundFilter.class).equals(clazz)) {
        return getCondition((CompoundFilter) filter);

      } else {
        Assert.unsupported("Unsupported class name: " + clazz);
      }
    }
    return null;
  }

  public String getExpression(String colName) {
    return getColumnInfo(colName)[EXPRESSION];
  }

  public String getField(String colName) {
    return getViewField(colName).getField();
  }

  public String getLocale(String colName) {
    return getColumnInfo(colName)[LOCALE];
  }

  public String getName() {
    return name;
  }

  public SqlSelect getQuery(Map<String, BeeTable> tables) {
    Assert.state(!isEmpty());

    for (String colName : columns.keySet()) {
      if (query.isEmpty()
          || (!BeeUtils.isEmpty(getLocale(colName)) && BeeUtils.isEmpty(getLocaleAlias(colName)))) {
        rebuildQuery(tables);
        break;
      }
    }
    return query.copyOf();
  }

  public String getSource() {
    return source;
  }

  public String getTable(String colName) {
    return getViewField(colName).getTable();
  }

  public DataType getType(String colName) {
    return getViewField(colName).getType();
  }

  public boolean hasColumn(String colName) {
    return !BeeUtils.isEmpty(colName) && columns.containsKey(colName.toLowerCase());
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
  }

  public boolean isNotNull(String colName) {
    return getViewField(colName).isNotNull();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public boolean isSourceField(String colName) {
    return getViewField(colName).isSourceField();
  }

  void addField(String colName, String expression, String locale, Map<String, BeeTable> tables) {
    Assert.notEmpty(expression);
    Assert.state(!hasColumn(colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), colName));

    String[] colInfo = new String[4];
    colInfo[NAME] = colName;
    colInfo[EXPRESSION] = expression;
    colInfo[LOCALE] = locale;
    columns.put(colName.toLowerCase(), colInfo);
    loadField(expression, tables);
  }

  void addOrder(String colName, boolean descending) {
    Assert.state(hasColumn(colName));
    orders.put(colName, descending);
  }

  private String[] getColumnInfo(String colName) {
    Assert.state(hasColumn(colName), "Unknown view column: " + getName() + " " + colName);
    return columns.get(colName.toLowerCase());
  }

  private String getLocaleAlias(String colName) {
    return getColumnInfo(colName)[LOCALE_ALIAS];
  }

  private ViewField getViewField(String colName) {
    return expressions.get(getExpression(colName).toLowerCase());
  }

  private void loadField(String expression, Map<String, BeeTable> tables) {
    if (expressions.containsKey(expression.toLowerCase())) {
      return;
    }
    char joinMode = 0;
    int pos = -1;

    for (char c : BeeView.JOIN_MASK.toCharArray()) {
      int idx = expression.lastIndexOf(c);
      if (idx > pos) {
        joinMode = c;
        pos = idx;
      }
    }
    String xpr = "";
    String fld = expression;

    if (pos >= 0) {
      xpr = expression.substring(0, pos);
      fld = expression.substring(pos + 1);
    }
    String tbl;
    String als;

    if (BeeUtils.isEmpty(xpr)) {
      tbl = getSource();
      als = tbl;
    } else {
      loadField(xpr, tables);
      ViewField vf = expressions.get(xpr.toLowerCase());
      als = vf.getTargetAlias();
      tbl = tables.get(vf.getTable().toLowerCase()).getField(vf.getField()).getRelation();
      Assert.notEmpty(tbl,
          BeeUtils.concat(1, "Not a relation field:", vf.getTable(), vf.getField()));

      if (BeeUtils.isEmpty(als)) {
        als = SqlUtils.uniqueName();
        vf.setTargetAlias(als);
        IsCondition join =
            SqlUtils.join(vf.getAlias(), vf.getField(), als,
                tables.get(tbl.toLowerCase()).getIdName());

        switch (joinMode) {
          case '<':
            query.addFromRight(tbl, als, join);
            break;

          case '>':
            query.addFromLeft(tbl, als, join);
            break;

          case '-':
            query.addFromInner(tbl, als, join);
            break;

          case '+':
            query.addFromFull(tbl, als, join);
            break;

          default:
            Assert.untouchable("Unhandled join mode: " + joinMode);
        }
      }
    }
    BeeTable table = tables.get(tbl.toLowerCase());
    BeeField field = table.getField(fld);

    if (field.isExtended()) {
      als = table.joinExtField(query, als, field);
    }
    expressions.put(expression.toLowerCase(), new ViewField(tbl, als, fld, field.getType(),
        field.isNotNull(), BeeUtils.isEmpty(xpr)));
  }

  private synchronized void rebuildQuery(Map<String, BeeTable> tables) {
    query.resetFields();
    query.resetOrder();

    for (String colName : getColumns()) {
      String als = getAlias(colName);
      String fld = getField(colName);

      if (BeeUtils.isEmpty(als)) {
        if (BeeUtils.isEmpty(tables)) {
          continue;
        }
        BeeTable table = tables.get(getTable(colName).toLowerCase());
        BeeField field = table.getField(fld);
        String locale = getLocale(colName);
        als = table.joinTranslationField(query, getViewField(colName).getAlias(), field, locale);
        fld = table.getTranslationField(field, locale);

        if (BeeUtils.isEmpty(als)) {
          query.addEmptyField(colName, field.getType(), field.getPrecision(), field.getScale());
          continue;
        }
        getColumnInfo(colName)[LOCALE_ALIAS] = als;
      }
      query.addField(als, fld, colName);
    }
    for (String colName : orders.keySet()) {
      String als = getAlias(colName);

      if (!BeeUtils.isEmpty(als)) {
        String fld = getField(colName);

        if (orders.get(colName)) {
          query.addOrderDesc(als, fld);
        } else {
          query.addOrder(als, fld);
        }
      }
    }
    query.addOrder(getSource(), sourceIdName);
  }
}
