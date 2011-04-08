package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

class FunctionCondition extends Condition {

  private enum SerializationMembers {
    FUNC, EXPR, VALUES
  }

  private static Logger logger = Logger.getLogger(FunctionCondition.class.getName());

  private String function;
  private IsExpression expression;
  private IsExpression[] values;

  protected FunctionCondition() {
    super();
  }

  public FunctionCondition(String func, IsExpression expr, IsExpression... vals) {
    Assert.notEmpty(func);
    Assert.notEmpty(expr);
    Assert.minLength(vals, 1);

    function = func;
    expression = expr;
    values = vals;
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
        case FUNC:
          function = value;
          break;
        case EXPR:
          expression = Expression.restore(value);
          break;
        case VALUES:
          String[] vals = Codec.beeDeserialize(value);
          values = new IsExpression[vals.length];

          for (int j = 0; j < vals.length; j++) {
            values[j] = Expression.restore(vals[j]);
          }
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
  }

  @Override
  public Collection<String> getSources() {
    return null;
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (IsExpression value : values) {
      paramList = (List<Object>) SqlUtils.addCollection(paramList, value.getSqlParams());
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder sb = new StringBuilder();

    sb.append(function).append("(").append(
        expression.getSqlString(builder, false));

    for (IsExpression value : values) {
      sb.append(", ").append(value.getSqlString(builder, paramMode));
    }
    return sb.append(")").toString();
  }

  @Override
  public String serialize() {
    SerializationMembers[] members = SerializationMembers.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMembers member : members) {
      switch (member) {
        case FUNC:
          arr[i++] = function;
          break;
        case EXPR:
          arr[i++] = expression;
          break;
        case VALUES:
          arr[i++] = values;
          break;
        default:
          logger.severe("Unhandled serialization member: " + member);
          break;
      }
    }
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), arr);
  }
}
