package com.butent.bee.server.sql;

import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates a CREATE SQL statements with all necessary options and support for different SQL
 * servers.
 */

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

    /**
     * Returns a field name as an expression. See
     * {@link com.butent.bee.shared. sql.SqlUtils#name(String)} for details.
     * 
     * @return a field name as an expression
     */
    public IsExpression getName() {
      return SqlUtils.name(name);
    }

    /**
     * @return a Keyword list as a Keyword array
     */
    public Keyword[] getOptions() {
      return options;
    }

    /**
     * @return the precision of the field
     */
    public int getPrecision() {
      return precision;
    }

    /**
     * @return the scale of the field
     */
    public int getScale() {
      return scale;
    }

    /**
     * @return the type of the field
     */
    public DataType getType() {
      return type;
    }
  }

  private final IsFrom target;
  private final boolean temporary;
  private List<SqlField> fieldList = new ArrayList<SqlField>();

  private SqlSelect dataSource;

  /**
   * Creates an SqlCreate statement with a specified IsFrom target. {@code target} and sets the
   * temporary argument to true
   * 
   * @param target the IsFrom target
   */
  public SqlCreate(String target) {
    this(target, true);
  }

  /**
   * Creates an SqlCreate statement with a specified IsFrom target {@code target} and a specified
   * {@code temporary} value.
   * 
   * @param target the IsFrom target
   * @param temporary the temporary keyword value
   */
  public SqlCreate(String target, boolean temporary) {
    this.target = FromJoin.fromSingle(target, null);
    this.temporary = temporary;
  }

  /**
   * Adds a BOOLEAN type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return the object's SqlCreate instance
   */
  public SqlCreate addBoolean(String field, Keyword... options) {
    return addField(field, DataType.BOOLEAN, 0, 0, options);
  }

  /**
   * Adds a CHAR type field with a field name {@code field}, precision {@code precision} and
   * Keywords {@code options}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addChar(String field, int precision, Keyword... options) {
    Assert.isPositive(precision);
    return addField(field, DataType.CHAR, precision, 0, options);
  }

  /**
   * Adds a DATE type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addDate(String field, Keyword... options) {
    return addField(field, DataType.DATE, 0, 0, options);
  }

  /**
   * Adds a DATETIME type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addDateTime(String field, Keyword... options) {
    return addField(field, DataType.DATETIME, 0, 0, options);
  }

  /**
   * Adds a DATETIME type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addDouble(String field, Keyword... options) {
    return addField(field, DataType.DOUBLE, 0, 0, options);
  }

  /**
   * Adds a specified type {@code type} field with a field name {@code field}, precision
   * {@code precision}, scale {@code scale} and Keywords {@code options}. Note: a field name
   * {@code field} must not exist, and dataSource must be empty.
   * 
   * @param field the field's name
   * @param type the field's type
   * @param precision he field's precision
   * @param scale he field's scale
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addField(String field, DataType type, int precision, int scale,
      Keyword... options) {
    Assert.state(BeeUtils.isEmpty(dataSource));
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");

    fieldList.add(new SqlField(field, type, precision, scale, options));

    return getReference();
  }

  /**
   * Adds an INTEGER type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addInt(String field, Keyword... options) {
    return addField(field, DataType.INTEGER, 0, 0, options);
  }

  /**
   * Adds a LONG type field with a field name {@code field} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addLong(String field, Keyword... options) {
    return addField(field, DataType.LONG, 0, 0, options);
  }

  /**
   * Adds a NUMERIC type field with a field name {@code field}, precision {@code precision}, scale
   * {@code scale} and Keywords {@code options}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param scale the field's scale
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addNumeric(String field, int precision, int scale, Keyword... options) {
    Assert.isPositive(precision);
    Assert.nonNegative(scale);
    return addField(field, DataType.NUMERIC, precision, scale, options);
  }

  /**
   * Adds a STRING type field with a field name {@code field}, precision {@code precision} and
   * Keywords {@code options}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param options additional Keywords
   * @return object's SqlCreate instance
   */
  public SqlCreate addString(String field, int precision, Keyword... options) {
    Assert.isPositive(precision);
    return addField(field, DataType.STRING, precision, 0, options);
  }

  /**
   * @return the current {@code dataSource}.
   */
  public SqlSelect getDataSource() {
    return dataSource;
  }

  /**
   * Returns a field named {@code field} if such exists.
   * 
   * @param field the field's name
   * @return a field named {@code field} if such exists, {@code null} if the name was not found.
   */
  public SqlField getField(String field) {
    for (SqlField fld : fieldList) {
      if (BeeUtils.same(fld.name, field)) {
        return fld;
      }
    }
    return null;
  }

  /**
   * @return a list of current fields.
   */
  public List<SqlField> getFields() {
    return fieldList;
  }

  /**
   * Returns a list of sources found in the {@code dataSource}. For more details see
   * {@link com.butent.bee.shared.sql.SqlSelect#getSources()}.
   * 
   * @returns a list of sources found in the {@code dataSource}.
   */
  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (!BeeUtils.isEmpty(dataSource)) {
      sources = dataSource.getSources();
    }
    return sources;
  }

  /**
   * Returns a list of parameters found in the {@code dataSource}. For more details see
   * {@link com.butent.bee.shared.sql.SqlSelect#getParams()}.
   * 
   * @returns a list of parameters found in the {@code dataSource}.
   */
  @Override
  public List<Object> getSqlParams() {
    Assert.state(!isEmpty());

    List<Object> paramList = null;

    if (!BeeUtils.isEmpty(dataSource)) {
      paramList = dataSource.getSqlParams();
    }
    return paramList;
  }

  /**
   * @param builder the builder to use
   * @param paramMode sets param mode on or off
   * @return a generated SqlCreate query with a specified SqlBuilder {@code builder} and parameter
   *         mode {@code paramMode}.
   */
  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    Assert.notEmpty(builder);
    return builder.getCreate(this, paramMode);
  }

  /**
   * @return the current target {@code target}
   */
  public IsFrom getTarget() {
    return target;
  }

  /**
   * Checks if a field named {@code field} exists.
   * 
   * @param field the field's name to check
   * @return true if the field exists, otherwise false.
   */
  public boolean hasField(String field) {
    return !BeeUtils.isEmpty(getField(field));
  }

  /**
   * Checks if teh current SqlCreate instance is empty.
   * 
   * @return true if it is empty, otherwise false.
   */
  @Override
  public boolean isEmpty() {
    return BeeUtils.isEmpty(target) ||
        (BeeUtils.isEmpty(fieldList) && BeeUtils.isEmpty(dataSource));
  }

  /**
   * Checks the value of a variable {@code temporary}.
   * 
   * @return the current value
   */
  public boolean isTemporary() {
    return temporary;
  }

  /**
   * If there are no fields created sets the {@code dataSource} from an SqlSelect query
   * {@code query}.
   * 
   * @param query the query to use for setting the dataSource
   * @return object's SqlCreate instance
   */
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
