package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

class JoinCondition extends Condition {

  private enum SerializationMembers {
    LEFT, OP, RIGHT
  }

  private static Logger logger = Logger.getLogger(JoinCondition.class.getName());

  private IsExpression leftExpression;
  private String operator;
  private Object rightExpression;

  public JoinCondition(IsExpression left, String op, IsExpression right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notEmpty(right);

    rightExpression = right;
  }

  public JoinCondition(IsExpression left, String op, SqlSelect right) {
    Assert.notEmpty(left);
    Assert.notEmpty(op);

    leftExpression = left;
    operator = op;

    Assert.notNull(right);
    Assert.state(!right.isEmpty());

    rightExpression = right;
  }

  protected JoinCondition() {
    super();
  }

  @Override
  public void deserialize(String s) {
    setSafe();
    SerializationMembers[] members = SerializationMembers.values();
    String[] arr = Codec.beeDeserialize(s);

    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMembers member = members[i];
      String value = arr[i];

      switch (member) {
        case LEFT:
          leftExpression = Expression.restore(value);
          break;
        case OP:
          operator = value;
          break;
        case RIGHT:
          rightExpression = Expression.restore(value);
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  @Override
  public Collection<String> getSources() {
    Collection<String> sources = null;

    if (rightExpression instanceof HasSource) {
      sources = ((HasSource) rightExpression).getSources();
    }
    return sources;
  }

  @Override
  public List<Object> getSqlParams() {
    return ((IsSql) rightExpression).getSqlParams();
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    String expr = ((IsSql) rightExpression).getSqlString(builder, paramMode);

    if (rightExpression instanceof SqlSelect) {
      expr = "(" + expr + ")";
    }
    return leftExpression.getSqlString(builder, false) + operator + expr;
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case LEFT:
          arr[i++] = leftExpression;
          break;
        case OP:
          arr[i++] = operator;
          break;
        case RIGHT:
          arr[i++] = rightExpression;
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), arr);
  }
}
