package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.IsExpression;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ColumnValueFilter extends ComparisonFilter {

  private enum SerializationMembers {
    COLUMN, OPERATOR, VALUE
  }

  private static Logger logger = Logger.getLogger(ColumnValueFilter.class.getName());

  private String column;
  private Value value;

  protected ColumnValueFilter() {
    super();
  }

  protected ColumnValueFilter(String column, Operator operator, Value value) {
    super(operator);
    Assert.notEmpty(column);
    Assert.notNull(value);

    if (Operator.CONTAINS == operator) {
      Assert.state(ValueType.TEXT == value.getType(),
          "Operator " + operator.toQueryString() + " can only be used with TEXT values");
    }
    this.column = column;
    this.value = value;
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);

    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String xpr = arr[i];

      switch (member) {
        case COLUMN:
          column = xpr;
          break;
        case OPERATOR:
          setOperator(Operator.valueOf(xpr));
          break;
        case VALUE:
          value = Value.restore(xpr);
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ColumnValueFilter other = (ColumnValueFilter) obj;

    if (!column.equals(other.column)) {
      return false;
    }
    if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  public String getColumn() {
    return column;
  }

  @Override
  public IsCondition getCondition(Map<String, String[]> columns) {
    IsCondition condition = null;

    String colName = column.toLowerCase();
    Assert.contains(columns, colName);

    String als = columns.get(colName)[0];

    if (!BeeUtils.isEmpty(als)) {
      IsExpression src = SqlUtils.field(als, columns.get(colName)[1]);
      Operator op = getOperator();

      if (Operator.CONTAINS == op) {
        String val = value.getString();

        if (hasLikeCharacters(val)) {
          condition = SqlUtils.like(src, val);
        } else {
          condition = SqlUtils.contains(src, val);
        }
      } else {
        condition = SqlUtils.compare(src, op.toQueryString(), SqlUtils.constant(value));
      }
    } else {
      LogUtils.warning(logger, "Column " + colName + " is not initialized");
    }
    return condition;
  }

  public Value getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + column.hashCode();
    result = prime * result + value.hashCode();
    return result;
  }

  @Override
  public boolean involvesColumn(String colName) {
    return BeeUtils.same(colName, column);
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    Value columnValue = row.getValue(getColumnIndex(column, columns));
    return isOperatorMatch(columnValue, value);
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case COLUMN:
          arr[i++] = column;
          break;
        case OPERATOR:
          arr[i++] = getOperator();
          break;
        case VALUE:
          arr[i++] = value;
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), arr);
  }

  @Override
  public String toString() {
    return BeeUtils.concat(1, column, getOperator().toQueryString(), value.getString());
  }
}