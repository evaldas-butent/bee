package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst.SqlEngine;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.Defaults.DefaultExpression;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.XmlExpression;
import com.butent.bee.shared.data.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.SqlConstants.SqlFunction;
import com.butent.bee.shared.data.XmlExpression.XmlBulk;
import com.butent.bee.shared.data.XmlExpression.XmlCase;
import com.butent.bee.shared.data.XmlExpression.XmlCast;
import com.butent.bee.shared.data.XmlExpression.XmlConcat;
import com.butent.bee.shared.data.XmlExpression.XmlDivide;
import com.butent.bee.shared.data.XmlExpression.XmlHasMember;
import com.butent.bee.shared.data.XmlExpression.XmlHasMembers;
import com.butent.bee.shared.data.XmlExpression.XmlMinus;
import com.butent.bee.shared.data.XmlExpression.XmlMultiply;
import com.butent.bee.shared.data.XmlExpression.XmlNvl;
import com.butent.bee.shared.data.XmlExpression.XmlPlus;
import com.butent.bee.shared.data.XmlExpression.XmlSwitch;
import com.butent.bee.shared.data.XmlView;
import com.butent.bee.shared.data.XmlView.XmlAggregateColumn;
import com.butent.bee.shared.data.XmlView.XmlColumn;
import com.butent.bee.shared.data.XmlView.XmlExternalJoin;
import com.butent.bee.shared.data.XmlView.XmlHiddenColumn;
import com.butent.bee.shared.data.XmlView.XmlOrder;
import com.butent.bee.shared.data.XmlView.XmlSimpleColumn;
import com.butent.bee.shared.data.XmlView.XmlSimpleJoin;
import com.butent.bee.shared.data.filter.ColumnColumnFilter;
import com.butent.bee.shared.data.filter.ColumnIsEmptyFilter;
import com.butent.bee.shared.data.filter.ColumnNotEmptyFilter;
import com.butent.bee.shared.data.filter.ColumnValueFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.filter.VersionFilter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements database view management - contains parameters for views and their fields and methods
 * for doing operations with them.
 */

public class BeeView implements BeeObject, HasExtendedInfo {

  private class ColumnInfo {
    private final String colName;
    private final String alias;
    private final BeeField field;
    private final String locale;
    private final SqlFunction aggregate;
    private final boolean hidden;
    private final String parentName;
    private final String ownerAlias;
    private final XmlExpression xmlExpression;
    private IsExpression expression = null;

    public ColumnInfo(String alias, BeeField field, String colName, String locale,
        SqlFunction aggregate, boolean hidden, String parent, String owner, XmlExpression expression) {
      this.colName = colName;
      this.alias = alias;
      this.field = field;
      this.locale = locale;
      this.aggregate = aggregate;
      this.hidden = hidden;
      this.parentName = parent;
      this.ownerAlias = owner;
      this.xmlExpression = expression;
    }

    public SqlFunction getAggregate() {
      return aggregate;
    }

    public String getAlias() {
      return alias;
    }

    public IsExpression getExpression() {
      if (expression == null) {
        expression = parse(xmlExpression, Sets.newHashSet(getName()));
      }
      return expression;
    }

    public BeeField getField() {
      return field;
    }

    public int getLevel() {
      int level = 0;

      if (!BeeUtils.isEmpty(getParent())) {
        ColumnInfo parent = getColumnInfo(getParent());

        if (!parent.getField().hasEditableRelation()) {
          level = 1;
        }
        level = level + parent.getLevel();
      }
      return level;
    }

    public String getLocale() {
      return locale;
    }

    public String getName() {
      return colName;
    }

    public String getOwner() {
      return ownerAlias;
    }

    public String getParent() {
      return parentName;
    }

    public SqlDataType getType() {
      if (xmlExpression != null) {
        return SqlDataType.valueOf(xmlExpression.type);
      } else {
        return getField().getType();
      }
    }

    public boolean isHidden() {
      return hidden;
    }

    public boolean isReadOnly() {
      return isHidden() || getAggregate() != null || getExpression() != null;
    }

    private IsExpression parse(XmlExpression xmlExpr, Set<String> history) {
      if (xmlExpr == null) {
        return null;
      }
      IsExpression expr = null;

      if (xmlExpr instanceof XmlHasMember) {
        IsExpression member = parse(((XmlHasMember) xmlExpr).member, history);

        if (xmlExpr instanceof XmlSwitch) {
          XmlSwitch sw = (XmlSwitch) xmlExpr;
          List<IsExpression> cases = Lists.newArrayList();

          for (XmlCase xmlCase : sw.cases) {
            cases.add(parse(xmlCase.whenExpression, history));
            cases.add(parse(xmlCase.thenExpression.member, history));
          }
          cases.add(parse(sw.elseExpression.member, history));

          expr = SqlUtils.sqlCase(member, cases.toArray());

        } else if (xmlExpr instanceof XmlCast) {
          XmlCast cast = (XmlCast) xmlExpr;
          expr = SqlUtils.cast(member, SqlDataType.valueOf(cast.type), cast.precision, cast.scale);
        }
      } else if (xmlExpr instanceof XmlHasMembers) {
        List<IsExpression> members = Lists.newArrayList();

        for (XmlExpression z : ((XmlHasMembers) xmlExpr).members) {
          members.add(parse(z, history));
        }
        if (xmlExpr instanceof XmlPlus) {
          expr = SqlUtils.plus(members.toArray());
        } else if (xmlExpr instanceof XmlMinus) {
          expr = SqlUtils.minus(members.toArray());
        } else if (xmlExpr instanceof XmlMultiply) {
          expr = SqlUtils.multiply(members.toArray());
        } else if (xmlExpr instanceof XmlDivide) {
          expr = SqlUtils.divide(members.toArray());
        } else if (xmlExpr instanceof XmlBulk) {
          expr = SqlUtils.expression(members.toArray());
        } else if (xmlExpr instanceof XmlNvl) {
          expr = SqlUtils.nvl(members.toArray());
        } else if (xmlExpr instanceof XmlConcat) {
          expr = SqlUtils.concat(members.toArray());
        }
      } else if (xmlExpr.content != null && !xmlExpr.content.replaceAll("\\s", "").isEmpty()) {
        List<Object> x = Lists.newArrayList();
        x.add(")");
        String xpr = xmlExpr.content;
        String regex = "^(.*)\"(\\w+)\"(.*)$";

        while (xpr.matches(regex)) {
          String s = xpr.replaceFirst(regex, "$3");
          String col = xpr.replaceFirst(regex, "$2");
          xpr = xpr.replaceFirst(regex, "$1");

          if (!BeeUtils.isEmpty(s)) {
            x.add(s);
          }
          if (hasColumn(col)) {
            ColumnInfo info = getColumnInfo(col);
            Assert.state(!history.contains(info.getName()),
                BeeUtils.concat(1, "Parsing cycle detected.",
                    "View:", BeeView.this.getName(), "Column:", info.getName()));

            if (info.expression == null) {
              history.add(info.getName());
              info.expression = parse(info.xmlExpression, history);
              history.remove(info.getName());
            }
            IsExpression xp = getSqlExpression(col);
            x.add(xp);
          } else {
            x.add("\"" + col + "\"");
          }
        }
        if (!BeeUtils.isEmpty(xpr)) {
          x.add(xpr);
        }
        x.add("(");
        Collections.reverse(x);
        expr = SqlUtils.expression(x.toArray());
      }
      if (expr == null) {
        Assert.unsupported(NameUtils.getClassName(xmlExpr.getClass()));
      }
      return expr;
    }
  }

  private enum JoinType {
    INNER, RIGHT, LEFT, FULL;
  }

  private final String moduleName;
  private final String name;
  private final BeeTable source;
  private final String sourceAlias;
  private final boolean readOnly;
  private boolean hasAggregate;
  private final SqlSelect query;
  private final Map<String, ColumnInfo> columns = Maps.newLinkedHashMap();
  private Filter filter = null;
  private Order order = null;

  BeeView(String moduleName, XmlView xmlView, Map<String, BeeTable> tables) {
    Assert.notNull(xmlView);
    this.moduleName = moduleName;
    this.name = xmlView.name;
    Assert.notEmpty(name);

    this.source = tables.get(BeeUtils.normalize(xmlView.source));
    Assert.notNull(source);

    this.sourceAlias = getSourceName();
    this.readOnly = xmlView.readOnly;

    this.query = new SqlSelect().addFrom(getSourceName(), getSourceAlias());

    addColumns(source, getSourceAlias(), xmlView.columns, null, tables);
    setColumns();
    setGrouping();

    if (!BeeUtils.isEmpty(xmlView.filter)) {
      this.filter = parseFilter(xmlView.filter);
    }
    if (!BeeUtils.isEmpty(xmlView.orders)) {
      this.order = new Order();

      for (XmlOrder ord : xmlView.orders) {
        order.add(ord.column, !ord.descending);
      }
    }
  }

  public SqlFunction getColumnAggregate(String colName) {
    return getColumnInfo(colName).getAggregate();
  }

  public int getColumnCount() {
    return getColumnNames().size();
  }

  public Pair<DefaultExpression, Object> getColumnDefaults(String colName) {
    Pair<DefaultExpression, Object> defaults = null;
    BeeField field = getColumnInfo(colName).getField();

    if (field != null) {
      defaults = field.getDefaults();
    }
    return defaults;
  }

  public IsExpression getColumnExpression(String colName) {
    return getColumnInfo(colName).getExpression();
  }

  public String getColumnField(String colName) {
    String fldName = null;
    BeeField field = getColumnInfo(colName).getField();

    if (field != null) {
      fldName = field.getName();
    }
    return fldName;
  }

  public int getColumnLevel(String colName) {
    return getColumnInfo(colName).getLevel();
  }

  public String getColumnLocale(String colName) {
    return getColumnInfo(colName).getLocale();
  }

  public String getColumnName(String colName) {
    return getColumnInfo(colName).getName();
  }

  public Collection<String> getColumnNames() {
    Collection<String> cols = Lists.newArrayList();

    for (ColumnInfo col : columns.values()) {
      if (!col.isHidden()) {
        cols.add(col.getName());
      }
    }
    return cols;
  }

  public String getColumnOwner(String colName) {
    return getColumnInfo(colName).getOwner();
  }

  public String getColumnParent(String colName) {
    return getColumnInfo(colName).getParent();
  }

  public String getColumnSource(String colName) {
    return getColumnInfo(colName).getAlias();
  }

  public String getColumnTable(String colName) {
    String tblName = null;
    BeeField field = getColumnInfo(colName).getField();

    if (field != null) {
      tblName = field.getTable();
    }
    return tblName;
  }

  public SqlDataType getColumnType(String colName) {
    return getColumnInfo(colName).getType();
  }

  public IsCondition getCondition(Filter flt) {
    if (flt != null) {
      String clazz = NameUtils.getClassName(flt.getClass());

      if (NameUtils.getClassName(ColumnValueFilter.class).equals(clazz)) {
        return getCondition((ColumnValueFilter) flt);

      } else if (NameUtils.getClassName(ColumnColumnFilter.class).equals(clazz)) {
        return getCondition((ColumnColumnFilter) flt);

      } else if (NameUtils.getClassName(ColumnIsEmptyFilter.class).equals(clazz)) {
        return getCondition((ColumnIsEmptyFilter) flt);

      } else if (NameUtils.getClassName(ColumnNotEmptyFilter.class).equals(clazz)) {
        return getCondition((ColumnNotEmptyFilter) flt);

      } else if (NameUtils.getClassName(CompoundFilter.class).equals(clazz)) {
        return getCondition((CompoundFilter) flt);

      } else if (NameUtils.getClassName(IdFilter.class).equals(clazz)) {
        return getCondition((IdFilter) flt);

      } else if (NameUtils.getClassName(VersionFilter.class).equals(clazz)) {
        return getCondition((VersionFilter) flt);

      } else {
        Assert.unsupported("Unsupported class name: " + clazz);
      }
    }
    return null;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();

    PropertyUtils.addProperties(info, false, "Module Name", getModuleName(), "Name", getName(),
        "Source", getSourceName(), "Source Alias", getSourceAlias(),
        "Source Id Name", getSourceIdName(), "Source Version Name", getSourceVersionName(),
        "Filter", BeeUtils.transform(getFilter()),
        "Read Only", isReadOnly(), "Query", query.getQuery(), "Columns", columns.size());

    int i = 0;
    for (String col : columns.keySet()) {
      String key = BeeUtils.concat(1, "Column", ++i, col);

      PropertyUtils.addChildren(info, key,
          "Table", getColumnTable(col), "Alias", getColumnSource(col),
          "Field", getColumnField(col), "Type", getColumnType(col), "Locale", getColumnLocale(col),
          "Aggregate Function", getColumnAggregate(col), "Hidden", isColHidden(col),
          "Read Only", isColReadOnly(col), "Level", getColumnLevel(col),
          "Expression", isColCalculated(col) ? getColumnExpression(col)
              .getSqlString(SqlBuilderFactory.getBuilder(SqlEngine.GENERIC)) : null,
          "Parent Column", getColumnParent(col), "Owner Alias", getColumnOwner(col));
    }
    if (order != null) {
      info.add(new ExtendedProperty("Orders", BeeUtils.toString(order.getSize())));
      i = 0;
      for (Order.Column ordCol : order.getColumns()) {
        String key = BeeUtils.concat(1, "Order", ++i, ordCol.isAscending() ? "" : "DESC");
        PropertyUtils.addChildren(info, key,
            "Sources", BeeUtils.transformCollection(ordCol.getSources()));
      }
    }
    return info;
  }

  public Filter getFilter() {
    return filter;
  }

  @Override
  public String getModuleName() {
    return moduleName;
  }

  @Override
  public String getName() {
    return name;
  }

  public SqlSelect getQuery(Filter flt, Order ord, List<String> cols) {
    SqlSelect ss = query.copyOf();
    Collection<String> activeCols = null;

    if (!BeeUtils.isEmpty(cols)) {
      ss.resetFields();
      activeCols = Sets.newHashSet();

      for (String col : cols) {
        if (!isColHidden(col)) {
          String colName = getColumnName(col);
          ss.addExpr(getSqlExpression(col), colName);
          activeCols.add(colName);
        }
      }
    }
    setFilter(ss, flt);

    String src = getSourceAlias();
    String idCol = getSourceIdName();
    String verCol = getSourceVersionName();
    boolean hasId = false;
    Order o = BeeUtils.nvl(ord, order);
    String alias;
    String colName;

    if (o != null) {
      for (Order.Column ordCol : o.getColumns()) {
        for (String col : ordCol.getSources()) {
          if (hasColumn(col)) {
            if (isColAggregate(col) || isColCalculated(col)) {
              alias = null;
              colName = getColumnName(col);

              if (isColHidden(col)
                  || (!BeeUtils.isEmpty(activeCols) && !BeeUtils.contains(activeCols, colName))) {
                LogUtils.warning(LogUtils.getDefaultLogger(), "view: ", getName(),
                    "order by:", col, ". Column not in field list");
                continue;
              }
            } else {
              alias = getColumnSource(col);
              colName = getColumnField(col);
            }
          } else if (BeeUtils.inListSame(col, idCol, verCol)) {
            hasId = hasId || BeeUtils.same(col, idCol);
            alias = src;
            colName = col;

          } else {
            LogUtils.warning(LogUtils.getDefaultLogger(), "view: ", getName(), "order by:", col,
                ". Column not recognized");
            continue;
          }
          if (!ordCol.isAscending()) {
            ss.addOrderDesc(alias, colName);
          } else {
            ss.addOrder(alias, colName);
          }
        }
      }
    }
    if (!hasId) {
      ss.addOrder(src, idCol);
    }
    return ss.addFields(src, idCol, verCol);
  }

  public SqlSelect getQuery(Filter flt) {
    return getQuery(flt, null, null);
  }
  
  public SqlSelect getQuery() {
    return getQuery(null, null, null);
  }

  public String getSourceAlias() {
    return sourceAlias;
  }

  public String getSourceIdName() {
    return source.getIdName();
  }

  public String getSourceName() {
    return source.getName();
  }

  public String getSourceVersionName() {
    return source.getVersionName();
  }
  
  public List<ViewColumn> getViewColumns() {
    List<ViewColumn> result = Lists.newArrayList();

    for (ColumnInfo cInf : columns.values()) {
      BeeField cf = cInf.getField();
      
      String table = (cf == null) ? null : cf.getTable(); 
      String field = (cf == null) ? null : cf.getName(); 
      String relation = (cf == null) ? null : cf.getRelation();
      
      String agg = (cInf.getAggregate() == null) ? null : cInf.getAggregate().name();
      String expr = (cInf.getExpression() == null) ? null 
          : cInf.getExpression().getSqlString(SqlBuilderFactory.getBuilder(SqlEngine.GENERIC));

      result.add(new ViewColumn(cInf.getName(), cInf.getParent(), table, field, relation,
          cInf.getLevel(), cInf.getLocale(), agg, expr, cInf.isHidden(), cInf.isReadOnly()));
    }
    return result;
  }

  public boolean hasColumn(String colName) {
    return !BeeUtils.isEmpty(colName) && columns.containsKey(BeeUtils.normalize(colName));
  }

  public boolean isColAggregate(String colName) {
    return getColumnAggregate(colName) != null;
  }

  public boolean isColCalculated(String colName) {
    return getColumnExpression(colName) != null;
  }

  public boolean isColHidden(String colName) {
    return getColumnInfo(colName).isHidden();
  }

  public boolean isColReadOnly(String colName) {
    return getColumnInfo(colName).isReadOnly();
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public Filter parseFilter(String flt) {
    Assert.notEmpty(flt);
    List<IsColumn> cols = Lists.newArrayListWithCapacity(columns.size());

    for (String col : columns.keySet()) {
      cols.add(new BeeColumn(getColumnType(col).toValueType(), col));
    }
    Filter f = DataUtils.parseCondition(flt, cols, getSourceIdName(), getSourceVersionName());

    if (f == null) {
      LogUtils.warning(LogUtils.getDefaultLogger(), "Error in filter expression:", flt);
    }
    return f;
  }

  public Order parseOrder(String input) {
    Assert.notEmpty(input);

    Set<String> colNames = Sets.newHashSet(getSourceIdName(), getSourceVersionName());
    colNames.addAll(getColumnNames());

    return Order.parse(input, colNames);
  }

  private void addColumn(String alias, BeeField field, String colName, String locale,
      String aggregateType, boolean hidden, String parent, XmlExpression expression) {

    Assert.state(!BeeUtils.inListSame(colName, getSourceIdName(), getSourceVersionName()),
        BeeUtils.concat(1, "Reserved column name:", getName(), colName));
    Assert.state(!hasColumn(colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), colName));

    String ownerAlias = null;
    SqlFunction aggregate = null;

    if (expression == null) {
      BeeTable table = field.getOwner();

      if (!BeeUtils.isEmpty(locale)) {
        if (field.isTranslatable()) {
          ownerAlias = alias;
          alias = table.joinTranslationField(query, ownerAlias, field, locale);
        } else {
          LogUtils.warning(LogUtils.getDefaultLogger(),
              "Field is not translatable:", table.getName() + "." + field.getName(),
              "View:", getName());
          return;
        }
      } else if (field.isExtended()) {
        ownerAlias = alias;
        alias = table.joinExtField(query, ownerAlias, field);
      }
    }
    if (!BeeUtils.isEmpty(aggregateType)) {
      aggregate = SqlFunction.valueOf(aggregateType);
    }
    columns.put(BeeUtils.normalize(colName),
        new ColumnInfo(alias, field, colName, locale, aggregate, hidden, parent, ownerAlias,
            expression));
  }

  private void addColumns(BeeTable table, String alias, Collection<XmlColumn> cols, String parent,
      Map<String, BeeTable> tables) {
    Assert.notNull(table);
    Assert.notEmpty(alias);
    Assert.notEmpty(cols);

    for (XmlColumn column : cols) {
      if (column instanceof XmlSimpleJoin) {
        XmlSimpleJoin col = (XmlSimpleJoin) column;
        BeeTable relTable;
        BeeField field;
        IsCondition join;
        String als;
        String relAls = SqlUtils.uniqueName();

        if (col instanceof XmlExternalJoin) {
          relTable = tables.get(BeeUtils.normalize(((XmlExternalJoin) col).source));
          Assert.notEmpty(relTable);
          als = relAls;
          field = relTable.getField(col.name);

          if (field.isExtended()) {
            LogUtils.warning(LogUtils.getDefaultLogger(),
                "Inverse join is not supported on extended fields:",
                relTable.getName() + "." + field.getName(), "View:", getName());
            continue;
          }
          join = SqlUtils.join(alias, table.getIdName(), relAls, field.getName());
        } else {
          field = table.getField(col.name);

          if (field.isExtended()) {
            als = table.joinExtField(query, alias, field);
          } else {
            als = alias;
          }
          relTable = tables.get(BeeUtils.normalize(field.getRelation()));
          Assert.notEmpty(relTable);
          join = SqlUtils.join(als, field.getName(), relAls, relTable.getIdName());
        }
        String relTbl = relTable.getName();

        switch (JoinType.valueOf(col.joinType)) {
          case INNER:
            query.addFromInner(relTbl, relAls, join);
            break;
          case LEFT:
            query.addFromLeft(relTbl, relAls, join);
            break;
          case RIGHT:
            query.addFromRight(relTbl, relAls, join);
            break;
          case FULL:
            query.addFromFull(relTbl, relAls, join);
            break;
        }
        String colName = SqlUtils.uniqueName();
        addColumn(als, field, colName, null, null, true, parent, null);
        addColumns(relTable, relAls, col.columns, colName, tables);

      } else if (column instanceof XmlSimpleColumn) {
        XmlSimpleColumn col = (XmlSimpleColumn) column;

        String colName = BeeUtils.ifString(col.alias, col.name);
        String aggregate = null;
        boolean hidden = (col instanceof XmlHiddenColumn);

        if (col instanceof XmlAggregateColumn) {
          aggregate = ((XmlAggregateColumn) col).aggregate;
        }
        if (col.expr != null) {
          addColumn(null, null, colName, null, aggregate, hidden, null, col.expr);
        } else {
          addColumn(alias, table.getField(col.name), colName, col.locale, aggregate, hidden,
              parent, null);
        }
      }
    }
  }

  private ColumnInfo getColumnInfo(String colName) {
    Assert.state(hasColumn(colName), "Unknown view column: " + getName() + "." + colName);
    return columns.get(BeeUtils.normalize(colName));
  }

  private IsCondition getCondition(ColumnColumnFilter flt) {
    return SqlUtils.compare(
        getSqlExpression(flt.getColumn()), flt.getOperator(), getSqlExpression(flt.getValue()));
  }

  private IsCondition getCondition(ColumnIsEmptyFilter flt) {
    String colName = flt.getColumn();
    SqlDataType type = getColumnType(colName);
    IsCondition cond = SqlUtils.isNull(getSqlExpression(colName));

    switch (type) {
      case DATE:
      case DATETIME:
        break;

      default:
        cond = SqlUtils.or(cond, SqlUtils.equal(getSqlExpression(colName), type.getEmptyValue()));
        break;
    }
    return cond;
  }

  private IsCondition getCondition(ColumnNotEmptyFilter flt) {
    String colName = flt.getColumn();
    SqlDataType type = getColumnType(colName);
    IsCondition cond = SqlUtils.notNull(getSqlExpression(colName));

    switch (type) {
      case DATE:
      case DATETIME:
        break;

      default:
        cond = SqlUtils.and(cond,
            SqlUtils.notEqual(getSqlExpression(colName), type.getEmptyValue()));
        break;
    }
    return cond;
  }

  private IsCondition getCondition(ColumnValueFilter flt) {
    return SqlUtils.compare(
        getSqlExpression(flt.getColumn()), flt.getOperator(), SqlUtils.constant(flt.getValue()));
  }

  private IsCondition getCondition(CompoundFilter flt) {
    HasConditions condition = null;

    if (!flt.isEmpty()) {
      switch (flt.getType()) {
        case AND:
          condition = SqlUtils.and();
          break;
        case OR:
          condition = SqlUtils.or();
          break;
        case NOT:
          return SqlUtils.not(getCondition(flt.getSubFilters().get(0)));
      }
      for (Filter subFilter : flt.getSubFilters()) {
        condition.add(getCondition(subFilter));
      }
    }
    return condition;
  }

  private IsCondition getCondition(IdFilter flt) {
    return SqlUtils.compare(SqlUtils.field(getSourceAlias(), getSourceIdName()),
        flt.getOperator(), SqlUtils.constant(flt.getValue()));
  }

  private IsCondition getCondition(VersionFilter flt) {
    return SqlUtils.compare(SqlUtils.field(getSourceAlias(), getSourceVersionName()),
        flt.getOperator(), SqlUtils.constant(flt.getValue()));
  }

  private IsExpression getSqlExpression(String colName) {
    IsExpression expr = getColumnExpression(colName);

    if (expr == null) {
      expr = SqlUtils.field(getColumnSource(colName), getColumnField(colName));
    }
    SqlFunction aggregate = getColumnAggregate(colName);

    if (aggregate != null) {
      expr = SqlUtils.aggregate(aggregate, expr);
    }
    return expr;
  }

  private void setColumns() {
    for (String colName : getColumnNames()) {
      query.addExpr(getSqlExpression(colName), colName);
    }
  }

  private void setFilter(SqlSelect ss, Filter flt) {
    CompoundFilter f = Filter.and();
    f.add(filter, flt);

    if (!f.isEmpty()) {
      IsCondition condition = getCondition(f);

      if (hasAggregate) {
        for (String col : columns.keySet()) {
          if (isColAggregate(col) && f.involvesColumn(col)) {
            ss.setHaving(condition);
            break;
          }
        }
        if (ss.getHaving() != null) {
          for (String col : columns.keySet()) {
            if (isColHidden(col) && !isColCalculated(col) && f.involvesColumn(col)) {
              ss.addGroup(getSqlExpression(col));
            }
          }
          return;
        }
      }
      ss.setWhere(condition);
    }
  }

  private void setGrouping() {
    List<String> group = Lists.newArrayList();

    for (String col : getColumnNames()) {
      if (isColAggregate(col)) {
        hasAggregate = true;
      } else if (!isColCalculated(col)) {
        group.add(col);
      }
    }
    if (hasAggregate) {
      query.addGroup(getSourceAlias(), getSourceIdName(), getSourceVersionName());

      for (String col : group) {
        query.addGroup(getSqlExpression(col));
      }
    }
  }
}
