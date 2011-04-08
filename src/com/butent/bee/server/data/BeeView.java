package com.butent.bee.server.data;

import com.google.common.collect.Maps;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class BeeView {

  private class ViewField {

    private final String table;
    private final String alias;
    private final String field;
    private boolean sourceField;
    private String targetAlias;

    public ViewField(String tbl, String als, String fld, boolean sourceField) {
      this.table = tbl;
      this.alias = als;
      this.field = fld;
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

    public boolean isSourceField() {
      return sourceField;
    }

    public void setTargetAlias(String als) {
      this.targetAlias = als;
    }
  }

  private static final String JOIN_MASK = "-<>+";
  private static final int EXPRESSION = 0;
  private static final int LOCALE = 1;
  private static final int ALIAS = 2;

  private final String name;
  private final String source;
  private final String sourceIdName;
  private final boolean readOnly;
  private final SqlSelect query;
  private final Map<String, String[]> columns = Maps.newLinkedHashMap();
  private final Map<String, ViewField> expressions = Maps.newLinkedHashMap();
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

  public Set<String> getColumns() {
    return Collections.unmodifiableSet(columns.keySet());
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

  public String getLocaleAlias(String colName) {
    return getColumnInfo(colName)[ALIAS];
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

  public boolean hasColumn(String colName) {
    Assert.notEmpty(colName);
    return columns.containsKey(colName);
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getColumnCount());
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

    columns.put(colName, new String[]{expression, locale, null});
    loadField(expression, tables);
  }

  void addOrder(String colName, boolean descending) {
    Assert.state(hasColumn(colName));
    orders.put(colName, descending);
  }

  private String[] getColumnInfo(String colName) {
    Assert.state(hasColumn(colName));
    return columns.get(colName);
  }

  private ViewField getViewField(String colName) {
    return expressions.get(getExpression(colName));
  }

  private void loadField(String expression, Map<String, BeeTable> tables) {
    if (expressions.containsKey(expression)) {
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
      ViewField vf = expressions.get(xpr);
      als = vf.getTargetAlias();
      tbl = tables.get(vf.getTable()).getField(vf.getField()).getRelation();
      Assert.notEmpty(tbl,
          BeeUtils.concat(1, "Not a relation field:", vf.getTable(), vf.getField()));

      if (BeeUtils.isEmpty(als)) {
        als = SqlUtils.uniqueName();
        vf.setTargetAlias(als);
        IsCondition join =
              SqlUtils.join(vf.getAlias(), vf.getField(), als, tables.get(tbl).getIdName());

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
    BeeTable table = tables.get(tbl);
    BeeField field = table.getField(fld);

    if (field.isExtended()) {
      als = table.joinExtField(query, als, field);
    }
    expressions.put(expression, new ViewField(tbl, als, fld, BeeUtils.isEmpty(xpr)));
  }

  private synchronized void rebuildQuery(Map<String, BeeTable> tables) {
    query.resetFields();
    query.resetOrder();

    for (String colName : columns.keySet()) {
      String als = getAlias(colName);
      String fld = getField(colName);

      if (BeeUtils.isEmpty(als)) {
        if (BeeUtils.isEmpty(tables)) {
          continue;
        }
        BeeTable table = tables.get(getTable(colName));
        BeeField field = table.getField(fld);
        String locale = getLocale(colName);
        als = table.joinTranslationField(query, getViewField(colName).getAlias(), field, locale);
        fld = table.getTranslationField(field, locale);

        if (BeeUtils.isEmpty(als)) {
          query.addEmptyField(colName, field.getType(), field.getPrecision(), field.getScale());
          continue;
        }
        getColumnInfo(colName)[ALIAS] = als;
      }
      query.addField(als, fld, colName);
    }
    for (String colName : orders.keySet()) {
      String als = getAlias(colName);
      String fld = getField(colName);

      if (BeeUtils.isEmpty(als)) {
        continue;
      }
      if (orders.get(colName)) {
        query.addOrderDesc(als, fld);
      } else {
        query.addOrder(als, fld);
      }
    }
    query.addOrder(getSource(), sourceIdName);
  }
}
