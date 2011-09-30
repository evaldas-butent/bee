package com.butent.bee.server.sql;

import com.butent.bee.server.sql.SqlConstants.SqlDataType;
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
    private final SqlDataType type;
    private final int precision;
    private final int scale;
    private final boolean notNull;

    private SqlField(String name, SqlDataType type, int precision, int scale, boolean notNull) {
      Assert.notEmpty(name);
      Assert.notEmpty(type);

      this.name = name;
      this.type = type;
      this.precision = precision;
      this.scale = scale;
      this.notNull = notNull;
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
    public SqlDataType getType() {
      return type;
    }

    /**
     * @return can field store null values
     */
    public boolean isNotNull() {
      return notNull;
    }
  }

  private IsFrom target;
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
    setTarget(target);
    this.temporary = temporary;
  }

  /**
   * Adds a BOOLEAN type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return the object's SqlCreate instance
   */
  public SqlCreate addBoolean(String field, boolean notNull) {
    return addField(field, SqlDataType.BOOLEAN, 0, 0, notNull);
  }

  /**
   * Adds a CHAR type field with a field name {@code field}, precision {@code precision} and notNull
   * {@code notNull}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addChar(String field, int precision, boolean notNull) {
    Assert.isPositive(precision);
    return addField(field, SqlDataType.CHAR, precision, 0, notNull);
  }

  /**
   * Adds a DATE type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addDate(String field, boolean notNull) {
    return addField(field, SqlDataType.DATE, 0, 0, notNull);
  }

  /**
   * Adds a DATETIME type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addDateTime(String field, boolean notNull) {
    return addField(field, SqlDataType.DATETIME, 0, 0, notNull);
  }

  /**
   * Adds a DECIMAL type field with a field name {@code field}, precision {@code precision}, scale
   * {@code scale} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param scale the field's scale
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addDecimal(String field, int precision, int scale, boolean notNull) {
    Assert.isPositive(precision);
    Assert.nonNegative(scale);
    return addField(field, SqlDataType.DECIMAL, precision, scale, notNull);
  }

  /**
   * Adds a DATETIME type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addDouble(String field, boolean notNull) {
    return addField(field, SqlDataType.DOUBLE, 0, 0, notNull);
  }

  /**
   * Adds a specified type {@code type} field with a field name {@code field}, precision
   * {@code precision}, scale {@code scale} and notNull {@code notNull}. Note: a field name
   * {@code field} must not exist, and dataSource must be empty.
   * 
   * @param field the field's name
   * @param type the field's type
   * @param precision he field's precision
   * @param scale he field's scale
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addField(String field, SqlDataType type, int precision, int scale,
      boolean notNull) {
    Assert.state(BeeUtils.isEmpty(dataSource));
    Assert.notEmpty(field);
    Assert.state(!hasField(field), "Field " + field + " already exist");

    fieldList.add(new SqlField(field, type, precision, scale, notNull));

    return getReference();
  }

  /**
   * Adds an INTEGER type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addInteger(String field, boolean notNull) {
    return addField(field, SqlDataType.INTEGER, 0, 0, notNull);
  }

  /**
   * Adds a LONG type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addLong(String field, boolean notNull) {
    return addField(field, SqlDataType.LONG, 0, 0, notNull);
  }

  /**
   * Adds a STRING type field with a field name {@code field}, precision {@code precision} and
   * notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param precision the field's precision
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addString(String field, int precision, boolean notNull) {
    Assert.isPositive(precision);
    return addField(field, SqlDataType.STRING, precision, 0, notNull);
  }

  /**
   * Adds a TEXT type field with a field name {@code field} and notNull {@code notNull}.
   * 
   * @param field the field's name
   * @param notNull field's not null
   * @return object's SqlCreate instance
   */
  public SqlCreate addText(String field, boolean notNull) {
    return addField(field, SqlDataType.TEXT, 0, 0, notNull);
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
   * Checks if the current SqlCreate instance is empty.
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

  /**
   * Sets the name of a table, to be created.
   * 
   * @param target table to create
   * @return object's SqlCreate instance
   */
  public SqlCreate setTarget(String target) {
    this.target = FromJoin.fromSingle(target, null);
    return this;
  }
}
