package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.IsExpression;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ColumnColumnFilter extends ComparisonFilter {

  private enum SerializationMembers {
    FIRSTCOLUMN, OPERATOR, SECONDCOLUMN
  }

  private static Logger logger = Logger.getLogger(ColumnColumnFilter.class.getName());

  private String firstColumn;
  private String secondColumn;

  protected ColumnColumnFilter() {
    super();
  }

  protected ColumnColumnFilter(String firstColumn, Operator operator, String secondColumn) {
    super(operator);
    Assert.state(Operator.LIKE != operator,
        "Operator " + operator.toTextString() + " is not allowed in a columns comparison");
    Assert.notEmpty(firstColumn);
    Assert.notEmpty(secondColumn);
    this.firstColumn = firstColumn;
    this.secondColumn = secondColumn;
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
        case FIRSTCOLUMN:
          firstColumn = xpr;
          break;
        case OPERATOR:
          setOperator(Operator.valueOf(xpr));
          break;
        case SECONDCOLUMN:
          secondColumn = xpr;
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
    ColumnColumnFilter other = (ColumnColumnFilter) obj;

    if (!firstColumn.equals(other.firstColumn)) {
      return false;
    }
    if (!secondColumn.equals(other.secondColumn)) {
      return false;
    }
    return true;
  }

  @Override
  public IsCondition getCondition(Map<String, String[]> columns) {
    IsCondition condition = null;

    String firstName = firstColumn.toLowerCase();
    Assert.contains(columns, firstName);
    String secondName = secondColumn.toLowerCase();
    Assert.contains(columns, secondName);

    String err = null;
    String als = columns.get(firstName)[0];

    if (!BeeUtils.isEmpty(als)) {
      IsExpression firstSrc = SqlUtils.field(als, columns.get(firstName)[1]);
      als = columns.get(secondName)[0];

      if (!BeeUtils.isEmpty(als)) {
        IsExpression secondSrc = SqlUtils.field(als, columns.get(secondName)[1]);
        condition = SqlUtils.compare(firstSrc, getOperator(), secondSrc);
      } else {
        err = secondName;
      }
    } else {
      err = firstName;
    }
    if (!BeeUtils.isEmpty(err)) {
      LogUtils.warning(logger, "Column " + err + " is not initialized");
    }
    return condition;
  }

  public String getFirstColumn() {
    return firstColumn;
  }

  public String getSecondColumn() {
    return secondColumn;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + firstColumn.hashCode();
    result = prime * result + secondColumn.hashCode();
    return result;
  }

  @Override
  public boolean involvesColumn(String colName) {
    return BeeUtils.inListSame(colName, firstColumn, secondColumn);
  }

  @Override
  public boolean isMatch(List<? extends IsColumn> columns, IsRow row) {
    Value firstValue = row.getValue(getColumnIndex(firstColumn, columns));
    Value secondValue = row.getValue(getColumnIndex(secondColumn, columns));
    return isOperatorMatch(firstValue, secondValue);
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case FIRSTCOLUMN:
          arr[i++] = firstColumn;
          break;
        case OPERATOR:
          arr[i++] = getOperator();
          break;
        case SECONDCOLUMN:
          arr[i++] = secondColumn;
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
    return BeeUtils.concat(0, firstColumn, getOperator().toTextString(), secondColumn);
  }
}