package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.List;

public class Calculation implements BeeSerializable, HasInfo {
  
  public static final String TAG_EXPRESSION = "expression"; 
  public static final String TAG_FUNCTION = "function"; 
  
  public static final String ATTR_TYPE = "type"; 
  
  public static Calculation restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Calculation calculation = new Calculation();
    calculation.deserialize(s);
    return calculation;
  }
  
  private ValueType type = null;
  
  private String expression = null;
  private String function = null;
  
  public Calculation(ValueType type, String expression, String function) {
    this.type = type;
    this.expression = expression;
    this.function = function;
  }

  private Calculation() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, 3);
    
    setType(BeeUtils.isEmpty(arr[0]) ? null : ValueType.getByTypeCode(arr[0]));
    setExpression(BeeUtils.isEmpty(arr[1]) ? null : Codec.decodeBase64(arr[1]));
    setFunction(BeeUtils.isEmpty(arr[2]) ? null : Codec.decodeBase64(arr[2]));
  }

  public List<Property> getInfo() {
    List<Property> info = Lists.newArrayList();
    
    if (isEmpty()) {
      PropertyUtils.addWhenEmpty(info, getClass());
      return info;
    }
    
    if (getType() != null) {
      info.add(new Property("Type", getType().name()));
    }
    if (!BeeUtils.isEmpty(getExpression())) {
      info.add(new Property("Expression", getExpression()));
    }
    if (!BeeUtils.isEmpty(getFunction())) {
      info.add(new Property("Function", getFunction()));
    }
    return info;
  }

  public boolean isEmpty() {
    return BeeUtils.allEmpty(getExpression(), getFunction());
  }

  public String serialize() {
    String tp = (getType() == null) ? null : getType().getTypeCode();
    String expr = BeeUtils.isEmpty(getExpression()) ? null : Codec.encodeBase64(getExpression());
    String func = BeeUtils.isEmpty(getFunction()) ? null : Codec.encodeBase64(getFunction());
    
    return Codec.beeSerializeAll(tp, expr, func);
  }

  private String getExpression() {
    return expression;
  }

  private String getFunction() {
    return function;
  }

  private ValueType getType() {
    return type;
  }

  private void setExpression(String expression) {
    this.expression = expression;
  }

  private void setFunction(String function) {
    this.function = function;
  }
  
  private void setType(ValueType type) {
    this.type = type;
  }
}
