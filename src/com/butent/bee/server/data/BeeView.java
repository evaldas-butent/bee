package com.butent.bee.server.data;

import com.google.common.collect.Maps;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.Map;

class BeeView {

  static final String JOIN_MASK = "[<>]+";

  private final String name;
  private final String source;
  private final boolean readOnly;
  private Map<String, String> expressions = Maps.newLinkedHashMap();
  private Map<String, BeeField> fields = Maps.newLinkedHashMap();

  private final Map<String, String> aliases = Maps.newHashMap();
  private final SqlSelect query;

  BeeView(String name, String source, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(source);

    this.name = name;
    this.source = source;
    this.readOnly = readOnly;

    this.query = new SqlSelect().addFrom(source);
    aliases.put("", source);
  }

  public Map<String, String> getExpressions() {
    return Collections.unmodifiableMap(expressions);
  }

  public Map<String, BeeField> getFields() {
    return Collections.unmodifiableMap(fields);
  }

  public String getName() {
    return name;
  }

  public SqlSelect getQuery() {
    return query.copyOf();
  }

  public String getSource() {
    return source;
  }

  public boolean isEmpty() {
    return BeeUtils.isEmpty(getFields());
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  void addField(String colName, String xpr, BeeField field) {
    Assert.notEmpty(colName);
    Assert.notEmpty(xpr);
    expressions.put(colName, xpr);
    fields.put(colName, field);
  }

  Map<String, String> getAliases() {
    return aliases;
  }

  SqlSelect getInternalQuery() {
    return query;
  }
}
