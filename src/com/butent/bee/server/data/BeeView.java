package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.filter.NegationFilter;
import com.butent.bee.shared.data.filter.VersionFilter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Implements database view management - contains parameters for views and their fields and methods
 * for doing operations with them.
 */

public class BeeView implements HasExtendedInfo {

  class ViewField {
    private final String table;
    private final String alias;
    private final String field;
    private final SqlDataType type;
    private final boolean notNull;
    private final String sourceExpression;
    private final String owner;

    private ViewField(String tbl, String als, String fld, SqlDataType type, boolean notNull,
        String sourceExpression, String owner) {
      this.table = tbl;
      this.alias = als;
      this.field = fld;
      this.type = type;
      this.notNull = notNull;
      this.sourceExpression = sourceExpression;
      this.owner = owner;
    }

    public String getAlias() {
      return alias;
    }

    public String getField() {
      return field;
    }

    public String getOwner() {
      return owner;
    }

    public String getSourceExpression() {
      return sourceExpression;
    }

    public String getTable() {
      return table;
    }

    public SqlDataType getType() {
      return type;
    }

    public boolean isNotNull() {
      return notNull;
    }
  }

  enum JoinType {
    INNER('-'), RIGHT('<'), LEFT('>'), FULL('+');

    private final char joinChar;

    private JoinType(char joinChar) {
      this.joinChar = joinChar;
    }

    public char getJoinChar() {
      return joinChar;
    }
  };

  private static final int NAME = 0;
  private static final int EXPRESSION = 1;
  private static final int LOCALE = 2;

  private final String name;
  private final String source;
  private final String sourceIdName;
  private final String sourceVersionName;
  private String sourceFilter;
  private final boolean readOnly;
  private final SqlSelect query;
  private final Map<String, String[]> columns = Maps.newLinkedHashMap();
  private final Map<String, ViewField> expressions = Maps.newHashMap();
  private final Map<String, Boolean> orders = Maps.newLinkedHashMap();

  BeeView(String name, String source, String idName, String versionName, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(source);
    Assert.notEmpty(idName);
    Assert.notEmpty(versionName);

    this.name = name;
    this.source = source;
    this.sourceIdName = idName;
    this.sourceVersionName = versionName;
    this.readOnly = readOnly;
    this.query = new SqlSelect().addFrom(source);
  }

  public String getAlias(String colName) {
    return getViewField(getExpression(colName)).getAlias();
  }

  public int getColumnCount() {
    return columns.size();
  }

  public Collection<String> getColumnNames() {
    Collection<String> cols = Lists.newArrayList();

    for (String[] col : columns.values()) {
      cols.add(col[NAME]);
    }
    return cols;
  }

  public IsCondition getCondition(ColumnColumnFilter filter) {
    String firstName = filter.getColumn();
    String secondName = filter.getValue();
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

      } else if (BeeUtils.getClassName(IdFilter.class).equals(clazz)) {
        return getCondition((IdFilter) filter);

      } else if (BeeUtils.getClassName(VersionFilter.class).equals(clazz)) {
        return getCondition((VersionFilter) filter);

      } else {
        Assert.unsupported("Unsupported class name: " + clazz);
      }
    }
    return null;
  }

  public IsCondition getCondition(IdFilter filter) {
    return SqlUtils.compare(SqlUtils.field(source, sourceIdName),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }

  public IsCondition getCondition(NegationFilter filter) {
    return SqlUtils.not(getCondition(filter.getSubFilter()));
  }

  public IsCondition getCondition(VersionFilter filter) {
    return SqlUtils.compare(SqlUtils.field(source, sourceVersionName),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }

  public String getExpression(String colName) {
    return getColumnInfo(colName)[EXPRESSION];
  }

  public String getField(String colName) {
    return getViewField(getExpression(colName)).getField();
  }

  public String getFilter() {
    return sourceFilter;
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();
    PropertyUtils.addProperties(info, false, "Name", getName(), "Source", getSource(),
        "Source Id Name", getSourceIdName(), "Source Version Name", getSourceVersionName(),
        "Filter", getFilter(), "Read Only", isReadOnly(), "Query", query.getQuery(),
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
          "Source Expression", fld.getSourceExpression(), "Owner", fld.getOwner());
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

  public String getRelSource(String colName) {
    String relSource = getViewField(getExpression(colName)).getSourceExpression();

    if (!BeeUtils.isEmpty(relSource)) {
      for (String[] colInfo : columns.values()) {
        if (BeeUtils.equals(relSource, colInfo[EXPRESSION])) {
          return colInfo[NAME];
        }
      }
    }
    return null;
  }

  public IsCondition getRowCondition(long rowId) {
    return SqlUtils.equal(source, sourceIdName, rowId);
  }
  
  public String getSource() {
    return source;
  }

  public String getSourceIdName() {
    return sourceIdName;
  }

  public String getSourceVersionName() {
    return sourceVersionName;
  }

  public String getTable(String colName) {
    return getViewField(getExpression(colName)).getTable();
  }

  public SqlDataType getType(String colName) {
    return getViewField(getExpression(colName)).getType();
  }

  public ViewField getViewField(String expression) {
    return expressions.get(expression);
  }

  public boolean hasColumn(String colName) {
    return !BeeUtils.isEmpty(colName) && columns.containsKey(BeeUtils.normalize(colName));
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
  }

  public boolean isNotNull(String colName) {
    return getViewField(getExpression(colName)).isNotNull();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public Filter parseFilter(String filter) {
    Assert.notEmpty(filter);
    
    List<IsColumn> cols = Lists.newArrayListWithCapacity(columns.size());
    for (String col : columns.keySet()) {
      cols.add(new BeeColumn(getType(col).toValueType(), col));
    }

    Filter flt = DataUtils.parseCondition(filter, cols, getSourceIdName(), getSourceVersionName());
    if (flt == null) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Error in filter expression:", filter);
    }
    return flt;
  }
  
  public Order parseOrder(String input) {
    Assert.notEmpty(input);
    
    Set<String> colNames = Sets.newHashSet(getSourceIdName(), getSourceVersionName());
    colNames.addAll(getColumnNames());
    
    return Order.parse(input, colNames);
  }
  
  void addColumn(String colName, String expression, String locale, Map<String, BeeTable> tables) {
    Assert.notEmpty(colName);
    Assert.notEmpty(expression);
    Assert.state(!hasColumn(colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), colName));

    if (loadExpression(expression, locale, tables)) {
      String[] colInfo = new String[Ints.max(NAME, EXPRESSION, LOCALE) + 1];
      colInfo[NAME] = colName;
      colInfo[EXPRESSION] = expressionKey(expression, locale);
      colInfo[LOCALE] = locale;
      columns.put(BeeUtils.normalize(colName), colInfo);

      query.addField(getAlias(colName), getField(colName), colName);
    }
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

  void setFilter(String filter) {
    String strFilter = null;

    if (!BeeUtils.isEmpty(filter)) {
      Filter flt = parseFilter(filter);

      if (flt != null) {
        query.setWhere(getCondition(flt));
        strFilter = flt.transform();
      }
    }
    this.sourceFilter = strFilter;
  }

  private String expressionKey(String expression, String locale) {
    String xpr = expression;

    for (JoinType join : JoinType.values()) {
      xpr = xpr.replace(join.getJoinChar(), '.');
    }
    return BeeUtils.concat(0, xpr, BeeUtils.parenthesize(BeeUtils.normalize(locale)));
  }

  private String[] getColumnInfo(String colName) {
    Assert.state(hasColumn(colName), "Unknown view column: " + getName() + " " + colName);
    return columns.get(BeeUtils.normalize(colName));
  }

  private boolean loadExpression(String expression, String locale, Map<String, BeeTable> tables) {
    String keyExpr = expressionKey(expression, locale);
    Logger logger = LogUtils.getDefaultLogger();

    if (expressions.containsKey(keyExpr)) {
      return true;
    }
    JoinType joinType = null;
    int pos = -1;

    for (JoinType join : BeeView.JoinType.values()) {
      int idx = expression.lastIndexOf(join.getJoinChar());
      if (idx > pos) {
        joinType = join;
        pos = idx;
      }
    }
    String xpr = null;
    String fld = expression;

    if (pos >= 0) {
      xpr = expression.substring(0, pos);
      fld = expression.substring(pos + 1);
    }
    String tbl;
    String als = null;
    String owner = null;
    BeeTable table = null;

    if (BeeUtils.isEmpty(xpr)) {
      tbl = getSource();
      als = tbl;
      table = tables.get(BeeUtils.normalize(tbl));
    } else {
      if (!loadExpression(xpr, null, tables)) {
        return false;
      }
      xpr = expressionKey(xpr, null);
      ViewField src = expressions.get(xpr);
      tbl = tables.get(BeeUtils.normalize(src.getTable())).getField(src.getField()).getRelation();

      if (BeeUtils.isEmpty(tbl)) {
        LogUtils.warning(logger, "Not a relation field:", xpr, "View:", getName());
        return false;
      }
      table = tables.get(BeeUtils.normalize(tbl));

      if (table == null) {
        LogUtils.warning(logger, "Unknown relation table:", tbl, "View:", getName(), xpr);
        return false;
      }
      for (ViewField v : expressions.values()) {
        if (BeeUtils.same(v.getSourceExpression(), xpr) && BeeUtils.isEmpty(v.getOwner())) {
          als = v.getAlias();
          break;
        }
      }
      if (BeeUtils.isEmpty(als)) {
        als = SqlUtils.uniqueName();
        IsCondition join = SqlUtils.join(src.getAlias(), src.getField(), als, table.getIdName());

        switch (joinType) {
          case RIGHT:
            query.addFromRight(tbl, als, join);
            break;

          case LEFT:
            query.addFromLeft(tbl, als, join);
            break;

          case INNER:
            query.addFromInner(tbl, als, join);
            break;

          case FULL:
            query.addFromFull(tbl, als, join);
            break;
        }
      }
    }
    BeeField field = table.getField(fld);

    if (!BeeUtils.isEmpty(locale)) {
      if (field.isTranslatable()) {
        owner = als;
        als = table.joinTranslationField(query, owner, field, locale);
      } else {
        LogUtils.warning(logger, "Field is not translatable:", tbl + "." + fld, "View:", getName());
        return false;
      }
    } else if (field.isExtended()) {
      owner = als;
      als = table.joinExtField(query, owner, field);
    }
    expressions.put(keyExpr,
        new ViewField(tbl, als, fld, field.getType(), field.isNotNull(), xpr, owner));
    return true;
  }
}
