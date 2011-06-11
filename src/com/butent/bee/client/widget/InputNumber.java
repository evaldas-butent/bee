package com.butent.bee.client.widget;

import com.google.common.base.CharMatcher;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.NumberFormat;

import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.i18n.HasNumberFormat;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasNumberBounds;
import com.butent.bee.shared.HasNumberStep;
import com.butent.bee.shared.HasPrecision;
import com.butent.bee.shared.HasScale;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

public class InputNumber extends InputText implements HasNumberBounds, HasNumberStep,
    HasNumberFormat, HasPrecision, HasScale {

  private int precision = BeeConst.UNDEF;
  private int scale = BeeConst.UNDEF;

  private Number minValue = null;
  private Number maxValue = null;
  private Number stepValue = null;

  private NumberFormat format = null;

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
    
    if (getNumberFormat() != null) {
      Double d = Format.parseQuietly(getNumberFormat(), v);
      if (d == null) {
        return null;
      }
      v = BeeUtils.toString(d);
    }
    return normalize(v);
  }

  public Number getNumber() {
    return BeeUtils.toDoubleOrNull(getNormalizedValue());
  }

  public NumberFormat getNumberFormat() {
    return format;
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

  public void setNumberFormat(NumberFormat format) {
    this.format = format;
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

  public void setValue(Number value) {
    if (value == null) {
      setValue(BeeConst.STRING_EMPTY);
    } else if (getNumberFormat() != null) {
      setValue(getNumberFormat().format(value));
    } else {
      setValue(BeeUtils.toString(value.doubleValue()));
    }
  }

  @Override
  public void startEdit(String oldValue, char charCode) {
    if (BeeUtils.isEmpty(oldValue) || acceptChar(charCode) || getNumberFormat() == null) {
      super.startEdit(oldValue, charCode);
    } else {
      setValue(getNumberFormat().format(BeeUtils.toDouble(oldValue)));
    }
  }

  @Override
  public String validate() {
    String msg = super.validate();
    if (!BeeUtils.isEmpty(msg)) {
      return msg;
    }

    String v = BeeUtils.trim(getValue());
    if (BeeUtils.isEmpty(v)) {
      if (isNullable()) {
        return null;
      } else {
        return "Value must not be null";
      }
    }
    
    if (getNumberFormat() != null) {
      Double d = Format.parseQuietly(getNumberFormat(), v);
      if (d == null) {
        return "Number format exception " + getNumberFormat().getPattern();
      }
      v = normalize(BeeUtils.toString(d));
    }
    if (!checkType(v)) {
      return "Not a number";
    }

    if (!checkBounds()) {
      StringBuilder sb = new StringBuilder("Value out of bounds:");
      if (getMinValue() != null) {
        sb.append(" min ").append(BeeUtils.toString(getMinValue().doubleValue()));
      }
      if (getMaxValue() != null) {
        sb.append(" max ").append(BeeUtils.toString(getMaxValue().doubleValue()));
      }
      return sb.toString();
    }
    return null;
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
