package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlCreate extends SqlQuery<SqlCreate> {

  public class SqlField {
    private final String name;
    private final DataType type;
    private final int precision;
    private final int scale;
    private final Keyword[] options;

    private SqlField(String name, DataType type, int precision, int scale, Keyword... options) {
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;

      List<Keyword> opts = new ArrayList<Keyword>();

      for (Keyword opt : options) {
        if (!BeeUtils.isEmpty(opt)) {
          opts.add(opt);
        }
      }
      this.options = opts.toArray(new Keyword[0]);
    }

    public IsExpression getName() {
      return SqlUtils.name(name);
    }

    public Keyword[] getOptions() {
      return options;
    }

    public int getPrecision() {
      return precision;
    }

    public int getScale() {
      return scale;
    }

    public DataType getType() {
      return type;
    }
  }

  private final IsFrom target;
  private final boolean temporary;
  private List<SqlField> fieldList = new ArrayList<SqlField>();

  private SqlSelect dataSource;

  public SqlCreate(String target) {
    this(target, true);
  }

  public SqlCreate(String target, boolean temporary) {
    this.target = FromJoin.fromSingle(target, null);
    this.temporary = temporary;
  }

  public SqlCreate addBoolean(String field, Keyword... options) {
    return addField(field, DataType.BOOLEAN, 0, 0, options);
  }

  public SqlCreate addChar(String field, int precision, Keyword... options) {
    Assert.isPositive(precision);
    return addField(field, DataType.CHAR, precision, 0, options);
  }

  public SqlCreate addDate(String field, Keyword... options) {
    return addField(field, DataType.DATE, 0, 0, options);
  }

  public SqlCreate addDateTime(String field, Keyword... options) {
    return addField(field, DataType.DATETIME, 0, 0, options);
  }

  public SqlCreate addDouble(String field, Keyword... options) {
    return addField(field, DataType.DOUBLE, 0, 0, options);
  }

  public SqlCreate addField(String field, DataType type, int precision, int scale,
      Keyword... options) {
    Assert.state(BeeUtils.isEmpty(dataSource));
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");

    fieldList.add(new SqlField(field, type, precision, scale, options));

    return getReference();
  }

  public SqlCreate addInt(String field, Keyword... options) {
    return addField(field, DataType.INTEGER, 0, 0, options);
  }

  public SqlCreate addLong(String field, Keyword... options) {
    return addField(field, DataType.LONG, 0, 0, options);
  }

  public SqlCreate addNumeric(String field, int precision, int scale, Keyword... options) {
    Assert.isPositive(precision);
    Assert.nonNegative(scale);
    return addField(field, DataType.NUMERIC, precision, scale, options);
  }

  public SqlCreate addString(String field, int precision, Keyword... options) {
    Assert.isPositive(precision);
    return addField(field, DataType.STRING, precision, 0, options);
  }

  public SqlSelect getDataSource() {
    return dataSource;
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

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (!BeeUtils.isEmpty(dataSource)) {
      sources = dataSource.getSources();
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(dataSource)) {
      paramList = dataSource.getSqlParams();
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
        (BeeUtils.isEmpty(fieldList) && BeeUtils.isEmpty(dataSource));
  }

  public boolean isTemporary() {
    return temporary;
  }

  public SqlCreate setDataSource(SqlSelect query) {
    Assert.notNull(query);
    Assert.state(!query.isEmpty());
    Assert.state(BeeUtils.isEmpty(fieldList));

    dataSource = query;

    return getReference();
  }

  @Override
  protected SqlCreate getReference() {
    return this;
  }
}
