package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.shared.sql.BeeConstants.Keywords;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;

public class SqlCreate extends SqlQuery<SqlCreate> {

  public class SqlField {
    private final String name;
    private final DataTypes type;
    private final int precision;
    private final int scale;
    private final Keywords[] options;

    private SqlField(String name, DataTypes type, int precision, int scale, Keywords... options) {
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;

      List<Keywords> opts = new ArrayList<Keywords>();

      for (Keywords opt : options) {
        if (!BeeUtils.isEmpty(opt)) {
          opts.add(opt);
        }
      }
      this.options = opts.toArray(new Keywords[0]);
    }

    public IsExpression getName() {
      return SqlUtils.name(name);
    }

    public Keywords[] getOptions() {
      return options;
    }

    public int getPrecision() {
      return precision;
    }

    public int getScale() {
      return scale;
    }

    public DataTypes getType() {
      return type;
    }
  }

  private final IsFrom target;
  private final boolean temporary;
  private List<SqlField> fieldList = new ArrayList<SqlField>();

  private SqlSelect source;

  public SqlCreate(String target) {
    this(target, true);
  }

  public SqlCreate(String target, boolean temporary) {
    this.target = new FromSingle(target);
    this.temporary = temporary;
  }

  public SqlCreate addBoolean(String field, Keywords... options) {
    return addField(field, DataTypes.BOOLEAN, 0, 0, options);
  }

  public SqlCreate addChar(String field, int precision, Keywords... options) {
    Assert.isPositive(precision);
    return addField(field, DataTypes.CHAR, precision, 0, options);
  }

  public SqlCreate addDouble(String field, int precision, int scale, Keywords... options) {
    Assert.nonNegative(precision);
    Assert.nonNegative(scale);
    return addField(field, DataTypes.DOUBLE, precision, scale, options);
  }

  public SqlCreate addField(String field, DataTypes type, int precision, int scale,
      Keywords... options) {
    Assert.state(BeeUtils.isEmpty(source));
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");

    fieldList.add(new SqlField(field, type, precision, scale, options));

    return getReference();
  }

  public SqlCreate addFloat(String field, int precision, int scale, Keywords... options) {
    Assert.nonNegative(precision);
    Assert.nonNegative(scale);
    return addField(field, DataTypes.FLOAT, precision, scale, options);
  }

  public SqlCreate addInt(String field, Keywords... options) {
    return addField(field, DataTypes.INTEGER, 0, 0, options);
  }

  public SqlCreate addLong(String field, Keywords... options) {
    return addField(field, DataTypes.LONG, 0, 0, options);
  }

  public SqlCreate addNumeric(String field, int precision, int scale, Keywords... options) {
    Assert.isPositive(precision);
    Assert.nonNegative(scale);
    return addField(field, DataTypes.NUMERIC, precision, scale, options);
  }

  public SqlCreate addString(String field, int precision, Keywords... options) {
    Assert.isPositive(precision);
    return addField(field, DataTypes.STRING, precision, 0, options);
  }

  public SqlField getField(String field) {
    for (SqlField fld : fieldList) {
      if (BeeUtils.same(fld.name, field)) {
        return fld;
      }
    }
    return null;
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
    return !BeeUtils.isEmpty(getField(field));
  }

  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) ||
        (BeeUtils.isEmpty(fieldList) && BeeUtils.isEmpty(source));
  }

  public boolean isTemporary() {
    return temporary;
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
