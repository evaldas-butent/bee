package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public abstract class Expression implements IsExpression {

  public static IsExpression restore(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 2);
    String clazz = arr[0];
    String data = arr[1];
    IsExpression expr = null;

    if (data != null) {
      expr = Expression.getExpression(clazz);
      Assert.notEmpty(expr, "Unsupported class name: " + clazz);
      expr.deserialize(data);
    }
    return expr;
  }

  public static boolean supports(String clazz) {
    return !BeeUtils.isEmpty(Expression.getExpression(clazz));
  }

  private static IsExpression getExpression(String clazz) {
    IsExpression expr = null;

    if (BeeUtils.getClassName(NameExpression.class).equals(clazz)) {
      expr = new NameExpression();

    } else if (BeeUtils.getClassName(ConstantExpression.class).equals(clazz)) {
      expr = new ConstantExpression();

    } else if (BeeUtils.getClassName(ComplexExpression.class).equals(clazz)) {
      expr = new ComplexExpression();
    }
    return expr;
  }

  private boolean safe = true;

  protected Expression() {
    this.safe = false;
  }

  @Override
  public String serialize() {
    return Codec.beeSerializeAll(BeeUtils.getClassName(this.getClass()), getValue());
  }

  protected void setSafe() {
    Assert.isFalse(safe);
    this.safe = true;
  }
}
