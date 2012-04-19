package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

/**
 * Enables using calculation expressions and functions in user interface components.
 */

public class Calculation implements BeeSerializable, HasInfo, Transformable {

  public static final String TAG_EXPRESSION = "expression";
  public static final String TAG_FUNCTION = "function";

  public static Calculation restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Calculation calculation = new Calculation();
    calculation.deserialize(s);
    return calculation;
  }

  private String expression = null;
  private String function = null;

  public Calculation(String expression, String function) {
    this.expression = expression;
    this.function = function;
  }

  private Calculation() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Assert.lengthEquals(arr, 2);

    setExpression(BeeUtils.isEmpty(arr[0]) ? null : Codec.decodeBase64(arr[0]));
    setFunction(BeeUtils.isEmpty(arr[1]) ? null : Codec.decodeBase64(arr[1]));
  }

  public String getExpression() {
    return expression;
  }

  public String getFunction() {
    return function;
  }

  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();

    if (isEmpty()) {
      PropertyUtils.addWhenEmpty(info, getClass());
      return info;
    }

    if (!BeeUtils.isEmpty(getExpression())) {
      info.add(new Property(TAG_EXPRESSION, getExpression()));
    }
    if (!BeeUtils.isEmpty(getFunction())) {
      info.add(new Property(TAG_FUNCTION, getFunction()));
    }
    return info;
  }

  public boolean hasExpressionOrFunction() {
    return !BeeUtils.isEmpty(getExpression()) || !BeeUtils.isEmpty(getFunction());
  }

  public String serialize() {
    String expr = BeeUtils.isEmpty(getExpression()) ? null : Codec.encodeBase64(getExpression());
    String func = BeeUtils.isEmpty(getFunction()) ? null : Codec.encodeBase64(getFunction());

    return Codec.beeSerialize(new Object[] {expr, func});
  }

  public String transform() {
    return BeeUtils.transformOptions(TAG_EXPRESSION, getExpression(),
        TAG_FUNCTION, getFunction());
  }

  private boolean isEmpty() {
    return BeeUtils.allEmpty(getExpression(), getFunction());
  }

  private void setExpression(String expression) {
    this.expression = expression;
  }

  private void setFunction(String function) {
    this.function = function;
  }
}
