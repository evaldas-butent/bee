package com.butent.bee.shared.data.filter;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;
import java.util.logging.Logger;

/**
 * Enables to compare values in two columns.
 */

public class ColumnColumnFilter extends ComparisonFilter {

  /**
   * Contains a list of filter parts which go through serialization.
   */

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
    super(operator == Operator.LIKE ? Operator.EQ : operator);
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
    int firstIndex = getColumnIndex(firstColumn, columns);
    Value firstValue = row.getValue(firstIndex, columns.get(firstIndex).getType());
    int secondIndex = getColumnIndex(secondColumn, columns);
    Value secondValue = row.getValue(secondIndex, columns.get(secondIndex).getType());
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