package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.NegationFilter;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implements database view management - contains parameters for views and their fields and methods
 * for doing operations with them.
 */

public class BeeView implements HasExtendedInfo {

  private class ViewField {

    private final String table;
    private final String alias;
    private final String field;
    private final SqlDataType type;
    private final boolean notNull;
    private boolean editable;
    private String targetAlias;

    public ViewField(String tbl, String als, String fld, SqlDataType type, boolean notNull,
        boolean editable) {
      this.table = tbl;
      this.alias = als;
      this.field = fld;
      this.type = type;
      this.notNull = notNull;
      this.editable = editable;
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

    public SqlDataType getType() {
      return type;
    }

    public boolean isEditable() {
      return editable;
    }

    public boolean isNotNull() {
      return notNull;
    }

    public void setTargetAlias(String als) {
      this.targetAlias = als;
    }
  }

  private static final String JOIN_MASK = "-<>+";
  private static final int NAME = 0;
  private static final int EXPRESSION = 1;
  private static final int LOCALE = 2;

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
    return getViewField(colName).getAlias();
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

  public IsCondition getCondition(ColumnColumnFilter filter) {
    String firstName = filter.getFirstColumn();
    String secondName = filter.getSecondColumn();
    IsExpression firstSrc = SqlUtils.field(getAlias(firstName), getField(firstName));
    IsExpression secondSrc = SqlUtils.field(getAlias(secondName), getField(secondName));

    return SqlUtils.compare(firstSrc, filter.getOperator(), secondSrc);
  }

  public IsCondition getCondition(ColumnIsEmptyFilter filter) {
    String colName = filter.getColumn();
    String als = getAlias(colName);
    String fld = getField(colName);
    IsCondition condition = SqlUtils.equal(als, fld, getType(colName).getEmptyValue());

    if (!isNotNull(colName)) {
      condition = SqlUtils.or(SqlUtils.isNull(als, fld), condition);
    }
    return condition;
  }

  public IsCondition getCondition(ColumnValueFilter filter) {
    String colName = filter.getColumn();

    return SqlUtils.compare(SqlUtils.field(getAlias(colName), getField(colName)),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }

  public IsCondition getCondition(CompoundFilter filter) {
    HasConditions condition = null;
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

  public IsCondition getCondition(NegationFilter filter) {
    return SqlUtils.not(getCondition(filter.getSubFilter()));
  }

  public String getExpression(String colName) {
    return getColumnInfo(colName)[EXPRESSION];
  }

  public String getField(String colName) {
    return getViewField(colName).getField();
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    PropertyUtils.addProperties(info, false, "Name", getName(), "Source", getSource(),
        "Source Id Name", sourceIdName, "Read Only", isReadOnly(), "Query", query.getQuery(),
        "Columns", columns.size());

    String sub;
    int i = 0;
    for (Map.Entry<String, String[]> entry : columns.entrySet()) {
      String key = BeeUtils.concat(1, "Column", ++i, entry.getKey());

      int j = 0;
      for (String value : entry.getValue()) {
        switch (j++) {
          case NAME:
            sub = "name";
            break;
          case EXPRESSION:
            sub = "expression";
            break;
          case LOCALE:
            sub = "locale";
            break;
          default:
            sub = null;
        }
        info.add(new ExtendedProperty(key, sub, value));
      }
    }

    info.add(new ExtendedProperty("Expressions", BeeUtils.toString(expressions.size())));

    i = 0;
    for (Map.Entry<String, ViewField> entry : expressions.entrySet()) {
      String key = BeeUtils.concat(1, "Expression", ++i, entry.getKey());
      ViewField fld = entry.getValue();
      if (fld == null) {
        continue;
      }

      PropertyUtils.addChildren(info, key, "Table", fld.getTable(), "Alias", fld.getAlias(),
          "Field", fld.getField(), "Type", fld.getType(), "Not Null", fld.isNotNull(),
          "Editable", fld.isEditable(), "Target Alias", fld.getTargetAlias());
    }

    info.add(new ExtendedProperty("Orders", BeeUtils.toString(orders.size())));
    i = 0;
    for (Map.Entry<String, Boolean> entry : orders.entrySet()) {
      info.add(new ExtendedProperty(BeeUtils.concat(1, "Order", ++i), entry.getKey(),
          BeeConst.STRING_EMPTY + (entry.getValue() ? "desc" : "")));
    }

    return info;
  }

  public String getLocale(String colName) {
    return getColumnInfo(colName)[LOCALE];
  }

  public String getName() {
    return name;
  }

  public String getName(String colName) {
    return getColumnInfo(colName)[NAME];
  }

  public SqlSelect getQuery() {
    Assert.state(!isEmpty());
    return query.copyOf().addOrder(getSource(), sourceIdName);
  }

  public String getSource() {
    return source;
  }

  public String getTable(String colName) {
    return getViewField(colName).getTable();
  }

  public SqlDataType getType(String colName) {
    return getViewField(colName).getType();
  }

  public boolean hasColumn(String colName) {
    return !BeeUtils.isEmpty(colName) && columns.containsKey(BeeUtils.normalize(colName));
  }

  public boolean isEditable(String colName) {
    return getViewField(colName).isEditable();
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

  void addField(String colName, String expression, String locale, Map<String, BeeTable> tables) {
    Assert.notEmpty(colName);
    Assert.notEmpty(expression);
    Assert.state(!hasColumn(colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), colName));

    String[] colInfo = new String[Ints.max(NAME, EXPRESSION, LOCALE) + 1];
    colInfo[NAME] = colName;
    colInfo[EXPRESSION] = expressionKey(expression, locale);
    colInfo[LOCALE] = locale;
    columns.put(BeeUtils.normalize(colName), colInfo);

    loadField(expression, locale, tables);
    query.addField(getAlias(colName), getField(colName), colName);
  }

  void addOrder(String colName, boolean descending) {
    Assert.state(hasColumn(colName));
    orders.put(getName(colName), descending);

    String als = getAlias(colName);
    String fld = getField(colName);

    if (descending) {
      query.addOrderDesc(als, fld);
    } else {
      query.addOrder(als, fld);
    }
  }

  private String expressionKey(String expression, String locale) {
    return BeeUtils.normalize(BeeUtils.concat(0, expression, BeeUtils.parenthesize(locale)));
  }

  private String[] getColumnInfo(String colName) {
    Assert.state(hasColumn(colName), "Unknown view column: " + getName() + " " + colName);
    return columns.get(BeeUtils.normalize(colName));
  }

  private ViewField getViewField(String colName) {
    return expressions.get(BeeUtils.normalize(getExpression(colName)));
  }

  private void loadField(String expression, String locale, Map<String, BeeTable> tables) {
    if (expressions.containsKey(expressionKey(expression, locale))) {
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
      loadField(xpr, locale, tables);
      ViewField vf = expressions.get(expressionKey(xpr, locale));
      als = vf.getTargetAlias();
      tbl = tables.get(BeeUtils.normalize(vf.getTable())).getField(vf.getField()).getRelation();
      Assert.notEmpty(tbl,
          BeeUtils.concat(1, "Not a relation field:", vf.getTable(), vf.getField()));

      if (BeeUtils.isEmpty(als)) {
        als = SqlUtils.uniqueName();
        vf.setTargetAlias(als);
        IsCondition join = SqlUtils.join(vf.getAlias(), vf.getField(), als,
            tables.get(BeeUtils.normalize(tbl)).getIdName());

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
    BeeTable table = tables.get(BeeUtils.normalize(tbl));
    BeeField field = table.getField(fld);

    if (!BeeUtils.isEmpty(locale)) {
      als = table.joinTranslationField(query, als, field, locale);

    } else if (field.isExtended()) {
      als = table.joinExtField(query, als, field);
    }
    expressions.put(expressionKey(expression, locale),
        new ViewField(table.getName(), als, field.getName(), field.getType(), field.isNotNull(),
            BeeUtils.isEmpty(xpr)));
  }
}
