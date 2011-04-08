package com.butent.bee.shared.sql;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

public abstract class Condition implements IsCondition {

  public static IsCondition restore(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 2);
    String clazz = arr[0];
    String data = arr[1];
    IsCondition cond = null;

    if (data != null) {
      cond = Condition.getCondition(clazz);
      Assert.notEmpty(cond, "Unsupported class name: " + clazz);
      cond.deserialize(data);
    }
    return cond;
  }

  public static boolean supports(String clazz) {
    return !BeeUtils.isEmpty(Condition.getCondition(clazz));
  }

  private static IsCondition getCondition(String clazz) {
    IsCondition cond = null;

    if (BeeUtils.getClassName(JoinCondition.class).equals(clazz)) {
      cond = new JoinCondition();

    } else if (BeeUtils.getClassName(FunctionCondition.class).equals(clazz)) {
      cond = new FunctionCondition();

    } else if (BeeUtils.getClassName(AndConditions.class).equals(clazz)) {
      cond = new AndConditions();

    } else if (BeeUtils.getClassName(OrConditions.class).equals(clazz)) {
      cond = new OrConditions();
    }
    return cond;
  }

  private boolean safe = true;

  protected Condition() {
    this.safe = false;
  }

  protected void setSafe() {
    Assert.isFalse(safe);
    this.safe = true;
  }
}
