package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

class ComplexExpression extends Expression {

  private Object[] content;

  public ComplexExpression(Object... expr) {
    Assert.minLength(expr, 1);
    Assert.noNulls(expr);
    content = expr;
  }

  protected ComplexExpression() {
    super();
  }

  @Override
  public void deserialize(String s) {
    setSafe();

    String[] arr = Codec.beeDeserialize(s);
    this.content = new Object[arr.length];

    for (int i = 0; i < arr.length; i++) {
      String[] data = Codec.beeDeserialize(arr[i]);

      switch (data.length) {
        case 0:
          this.content[i] = arr[i];
          break;

        case 1:
          this.content[i] = data[0];
          break;

        case 2:
          if (Value.supports(data[0])) {
            this.content[i] = Value.restore(arr[i]);

          } else if (Expression.supports(data[0])) {
            this.content[i] = Expression.restore(arr[i]);

          } else if (Condition.supports(data[0])) {
            this.content[i] = Condition.restore(arr[i]);
          }
          break;

        default:
          Assert.unsupported(arr[i]);
      }
    }
  }

  @Override
  public List<Object> getSqlParams() {
    List<Object> paramList = null;

    for (Object o : content) {
      if (o instanceof IsSql) {
        paramList = (List<Object>) SqlUtils.addCollection(paramList, ((IsSql) o).getSqlParams());
      }
    }
    return paramList;
  }

  @Override
  public String getSqlString(SqlBuilder builder, boolean paramMode) {
    StringBuilder s = new StringBuilder();

    for (Object o : content) {
      if (o instanceof IsSql) {
        s.append(((IsSql) o).getSqlString(builder, paramMode));
      } else {
        s.append(BeeUtils.transformNoTrim(o));
      }
    }
    return s.toString();
  }

  @Override
  public Object getValue() {
    return content;
  }
}
