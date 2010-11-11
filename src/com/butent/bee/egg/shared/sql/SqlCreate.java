package com.butent.bee.egg.shared.sql;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.egg.shared.sql.BeeConstants.Keywords;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlCreate extends SqlQuery<SqlCreate> {

  public class SqlField {
    String field;
    DataTypes type;
    int precission;
    int scale;
    Keywords[] params;

    private SqlField(String field, DataTypes type,
        int precission, int scale, Keywords[] params) {

      this.field = field;
      this.type = type;
      this.precission = precission;
      this.scale = scale;
      this.params = params;
    }

    public IsExpression getField() {
      return SqlUtils.field(field);
    }

    public Keywords[] getParams() {
      return params;
    }

    public int getPrecission() {
      return precission;
    }

    public int getScale() {
      return scale;
    }

    public DataTypes getType() {
      return type;
    }
  }

  private final IsFrom target;
  private List<SqlField> fieldList = new ArrayList<SqlField>();

  private SqlSelect source;

  public SqlCreate(String source) {
    target = new FromSingle(source);
  }

  public SqlCreate addBoolean(String field, Keywords... params) {
    return addField(field, DataTypes.BOOLEAN, 0, 0, params);
  }

  public SqlCreate addChar(String field, int precission, Keywords... params) {
    Assert.nonNegative(precission);
    return addField(field, DataTypes.CHAR, precission, 0, params);
  }

  public SqlCreate addDouble(String field, int precission, int scale,
      Keywords... params) {
    Assert.nonNegative(precission);
    Assert.nonNegative(scale);
    return addField(field, DataTypes.DOUBLE, precission, scale, params);
  }

  public SqlCreate addField(String field, DataTypes type,
      int precission, int scale, Keywords... params) {

    Assert.state(BeeUtils.isEmpty(source));
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");

    fieldList.add(new SqlField(field, type, precission, scale, params));

    return getReference();
  }

  public SqlCreate addInt(String field, Keywords... params) {
    return addField(field, DataTypes.INTEGER, 0, 0, params);
  }

  public SqlCreate addLong(String field, Keywords... params) {
    return addField(field, DataTypes.LONG, 0, 0, params);
  }

  public SqlCreate addNumeric(String field, int precission, int scale,
      Keywords... params) {
    Assert.nonNegative(precission);
    Assert.nonNegative(scale);
    return addField(field, DataTypes.NUMERIC, precission, scale, params);
  }

  public SqlCreate addString(String field, int precission, Keywords... params) {
    Assert.nonNegative(precission);
    return addField(field, DataTypes.STRING, precission, 0, params);
  }

  public List<SqlField> getFields() {
    return fieldList;
  }

  public SqlSelect getSource() {
    return source;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(source)) {
      SqlUtils.addParams(paramList, source.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getCreate(this, paramMode);
  }

  public IsFrom getTarget() {
    return target;
  }

  public boolean hasField(String field) {
    for (SqlField fld : fieldList) {
      if (BeeUtils.same(fld.field, field)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) ||
        (BeeUtils.isEmpty(fieldList) && BeeUtils.isEmpty(source));
  }

  public SqlCreate setSource(SqlSelect query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    Assert.state(BeeUtils.isEmpty(fieldList));

    source = query;

    return getReference();
  }

  @Override
  protected SqlCreate getReference() {
    return this;
  }
}
