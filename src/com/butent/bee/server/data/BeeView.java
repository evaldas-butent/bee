package com.butent.bee.server.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
import com.butent.bee.shared.data.XmlView.XmlColumn;
import com.butent.bee.shared.data.XmlView.XmlJoinColumn;
import com.butent.bee.shared.data.XmlView.XmlSimpleColumn;
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

/**
 * Implements database view management - contains parameters for views and their fields and methods
 * for doing operations with them.
 */

public class BeeView implements HasExtendedInfo {

  private class ColumnInfo {
    private final String colName;
    private final String alias;
    private final BeeField field;
    private final String locale;
    private final String parentName;
    private final String ownerAlias;

    public ColumnInfo(String alias, BeeField field, String colName, String locale, String parent,
        String owner) {
      this.colName = colName;
      this.alias = alias;
      this.field = field;
      this.locale = locale;
      this.parentName = parent;
      this.ownerAlias = owner;
    }

    public String getAlias() {
      return alias;
    }

    public BeeField getField() {
      return field;
    }

    public String getLocale() {
      return locale;
    }

    public String getName() {
      return colName;
    }

    public String getOwnerAlias() {
      return ownerAlias;
    }

    public String getParent() {
      return parentName;
    }
  }

  private enum JoinType {
    INNER, RIGHT, LEFT, FULL;
  };

  private final String name;
  private final BeeTable source;
  private final String sourceAlias;
  private String sourceFilter;
  private final boolean readOnly;
  private final SqlSelect query;
  private final Map<String, ColumnInfo> columns = Maps.newLinkedHashMap();
  private final Map<String, Boolean> orders = Maps.newLinkedHashMap();

  BeeView(String name, BeeTable source, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notNull(source);

    this.name = name;
    this.source = source;
    this.sourceAlias = getSourceName();
    this.readOnly = readOnly;
    this.query = new SqlSelect().addFrom(getSourceName(), getSourceAlias());
  }

  public int getColumnCount() {
    return columns.size();
  }

  public String getColumnField(String colName) {
    return getColumnInfo(colName).getField().getName();
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
      cols.add(col.getName());
    }
    return cols;
  }

  public String getColumnOwner(String colName) {
    return getColumnInfo(colName).getOwnerAlias();
  }

  public String getColumnParent(String colName) {
    return getColumnInfo(colName).getParent();
  }

  public String getColumnSource(String colName) {
    return getColumnInfo(colName).getAlias();
  }

  public String getColumnTable(String colName) {
    return getColumnInfo(colName).getField().getTable();
  }

  public SqlDataType getColumnType(String colName) {
    return getColumnInfo(colName).getField().getType();
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

  public String getFilter() {
    return sourceFilter;
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();

    PropertyUtils.addProperties(info, false, "Name", getName(), "Source", getSourceName(),
        "Source Alias", getSourceAlias(), "Source Id Name", getSourceIdName(),
        "Source Version Name", getSourceVersionName(), "Filter", getFilter(),
        "Read Only", isReadOnly(), "Query", query.getQuery(), "Columns", getColumnCount());

    int i = 0;
    for (String col : getColumnNames()) {
      String key = BeeUtils.concat(1, "Column", ++i, col);

      PropertyUtils.addChildren(info, key,
          "Table", getColumnTable(col), "Alias", getColumnSource(col),
          "Field", getColumnField(col), "Type", getColumnType(col), "Locale", getColumnLocale(col),
          "Parent Column", getColumnParent(col), "Owner Alias", getColumnOwner(col));
    }
    info.add(new ExtendedProperty("Orders", BeeUtils.toString(orders.size())));
    i = 0;
    for (Map.Entry<String, Boolean> entry : orders.entrySet()) {
      info.add(new ExtendedProperty(BeeUtils.concat(1, "Order", ++i), entry.getKey(),
          BeeConst.STRING_EMPTY + (entry.getValue() ? "desc" : "")));
    }

    return info;
  }

  public String getName() {
    return name;
  }

  public SqlSelect getQuery() {
    Assert.state(!isEmpty());
    return query.copyOf().addOrder(getSourceName(), getSourceIdName());
  }

  public IsCondition getRowCondition(long rowId) {
    return SqlUtils.equal(getSourceName(), getSourceIdName(), rowId);
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

  public boolean hasColumn(String colName) {
    return !BeeUtils.isEmpty(colName) && columns.containsKey(BeeUtils.normalize(colName));
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public Filter parseFilter(String filter) {
    Assert.notEmpty(filter);

    List<IsColumn> cols = Lists.newArrayListWithCapacity(columns.size());
    for (String col : columns.keySet()) {
      cols.add(new BeeColumn(getColumnType(col).toValueType(), col));
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

  void addColumns(BeeTable table, String alias, Collection<XmlColumn> cols, String parent,
      Map<String, BeeTable> tables) {
    Assert.notNull(table);
    Assert.notEmpty(alias);
    Assert.notEmpty(cols);

    for (XmlColumn column : cols) {
      if (column instanceof XmlJoinColumn) {
        XmlJoinColumn col = (XmlJoinColumn) column;
        BeeTable relTable;
        BeeField field;
        IsCondition join;
        String als = alias;
        String relAls = SqlUtils.uniqueName();

        if (!BeeUtils.isEmpty(col.source)) {
          relTable = tables.get(BeeUtils.normalize(col.source));
          Assert.notEmpty(relTable);
          field = relTable.getField(col.expression);

          if (field.isExtended()) {
            LogUtils.warning(LogUtils.getDefaultLogger(),
                "Inverse join is not supported on extended fields:",
                relTable.getName() + "." + field.getName(), "View:", getName());
            continue;
          }
          join = SqlUtils.join(als, table.getIdName(), relAls, field.getName());
        } else {
          field = table.getField(col.expression);

          if (field.isExtended()) {
            als = table.joinExtField(query, alias, field);
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
        String colName = addColumn(alias, field, col.name, null, parent);
        addColumns(relTable, relAls, col.columns, colName, tables);

      } else if (column instanceof XmlSimpleColumn) {
        XmlSimpleColumn col = (XmlSimpleColumn) column;
        BeeField field = table.getField(col.expression);
        addColumn(alias, field, BeeUtils.ifString(col.name, field.getName()), col.locale, parent);
      }
    }
  }

  void addOrder(String colName, boolean descending) {
    Assert.state(hasColumn(colName));
    orders.put(getColumnName(colName), descending);

    String als = getColumnSource(colName);
    String fld = getColumnField(colName);

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

  private String addColumn(String alias, BeeField field, String col, String locale, String parent) {
    String colName = BeeUtils.ifString(col, SqlUtils.uniqueName());

    Assert.state(!hasColumn(colName),
        BeeUtils.concat(1, "Dublicate column name:", getName(), colName));

    BeeTable table = field.getOwner();
    String ownerAlias = null;

    if (!BeeUtils.isEmpty(locale)) {
      if (field.isTranslatable()) {
        ownerAlias = alias;
        alias = table.joinTranslationField(query, ownerAlias, field, locale);
      } else {
        LogUtils.warning(LogUtils.getDefaultLogger(),
            "Field is not translatable:", table.getName() + "." + field.getName(),
            "View:", getName());
        return null;
      }
    } else if (field.isExtended()) {
      ownerAlias = alias;
      alias = table.joinExtField(query, ownerAlias, field);
    }
    if (!BeeUtils.isEmpty(col)) {
      query.addField(alias, field.getName(), colName);
    }
    columns.put(BeeUtils.normalize(colName),
        new ColumnInfo(alias, field, colName, locale, parent, ownerAlias));

    return colName;
  }

  private ColumnInfo getColumnInfo(String colName) {
    Assert.state(hasColumn(colName), "Unknown view column: " + getName() + "." + colName);
    return columns.get(BeeUtils.normalize(colName));
  }

  private IsCondition getCondition(ColumnColumnFilter filter) {
    String firstName = filter.getColumn();
    String secondName = filter.getValue();
    IsExpression firstSrc = SqlUtils.field(getColumnSource(firstName), getColumnField(firstName));
    IsExpression secondSrc =
        SqlUtils.field(getColumnSource(secondName), getColumnField(secondName));

    return SqlUtils.compare(firstSrc, filter.getOperator(), secondSrc);
  }

  private IsCondition getCondition(ColumnIsEmptyFilter filter) {
    String colName = filter.getColumn();
    String als = getColumnSource(colName);
    String fld = getColumnField(colName);
    IsCondition condition = SqlUtils.equal(als, fld, getColumnType(colName).getEmptyValue());

    if (!getColumnInfo(colName).getField().isNotNull()) {
      condition = SqlUtils.or(SqlUtils.isNull(als, fld), condition);
    }
    return condition;
  }

  private IsCondition getCondition(ColumnValueFilter filter) {
    String colName = filter.getColumn();

    return SqlUtils.compare(SqlUtils.field(getColumnSource(colName), getColumnField(colName)),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }

  private IsCondition getCondition(CompoundFilter filter) {
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

  private IsCondition getCondition(IdFilter filter) {
    return SqlUtils.compare(SqlUtils.field(getSourceName(), getSourceIdName()),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }

  private IsCondition getCondition(NegationFilter filter) {
    return SqlUtils.not(getCondition(filter.getSubFilter()));
  }

  private IsCondition getCondition(VersionFilter filter) {
    return SqlUtils.compare(SqlUtils.field(getSourceName(), getSourceVersionName()),
        filter.getOperator(), SqlUtils.constant(filter.getValue()));
  }
}
