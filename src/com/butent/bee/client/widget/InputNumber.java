package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Element;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

public class InputNumber extends InputText {
  
  private int precision = BeeConst.UNDEF;
  private int scale = BeeConst.UNDEF;

  private Number minValue = null;
  private Number maxValue = null;
  private Number stepValue = null;
  
  public InputNumber() {
    super();
  }

  public InputNumber(Element element) {
    super(element);
  }

  public InputNumber(HasStringValue source) {
    super(source);
  }

  public boolean checkBounds() {
    Number v = getNumber();
    if (v == null) {
      return isNullable();
    }
    if (getMinValue() != null && v.doubleValue() < getMinValue().doubleValue()) {
      return false;
    }
    if (getMaxValue() != null && v.doubleValue() > getMaxValue().doubleValue()) {
      return false;
    }
    return true;
  }

  public Number getMaxValue() {
    return maxValue;
  }

  public Number getMinValue() {
    return minValue;
  }

  @Override
  public String getNormalizedValue() {
    String v = BeeUtils.trim(getValue());
    if (BeeUtils.isEmpty(v) && isNullable()) {
      return null;
    }
    return normalize(v);
  }
 
  public Number getNumber() {
    return BeeUtils.toDoubleOrNull(BeeUtils.trim(getValue()));
  }

  public int getPrecision() {
    return precision;
  }

  public int getScale() {
    return scale;
  }

  public Number getStepValue() {
    return stepValue;
  }
  
  public void setMaxValue(Number maxValue) {
    this.maxValue = maxValue;
  }

  public void setMinValue(Number minValue) {
    this.minValue = minValue;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public void setStepValue(Number stepValue) {
    this.stepValue = stepValue;
  }
  
  @Override
  public boolean validate() {
    if (!super.validate()) {
      return false;
    }
    
    String v = BeeUtils.trim(getValue());
    if (BeeUtils.isEmpty(v)) {
      return isNullable();
    }
    if (!checkType(v)) {
      return false;
    }
    
    return checkBounds();
  }
  
  protected boolean checkType(String v) {
    return BeeUtils.isDouble(v);
  }
  
  @Override
  protected CharMatcher getDefaultCharMatcher() {
    return CharMatcher.anyOf("0123456789 ,.-eE");
  }

  @Override
  protected String getDefaultIdPrefix() {
    return "number";
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputNumber";
  }

  protected String normalize(String v) {
    return BeeUtils.toString(BeeUtils.toDouble(v));
  }
}
