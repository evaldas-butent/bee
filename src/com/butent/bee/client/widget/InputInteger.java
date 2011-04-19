package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

public class InputInteger extends BeeTextBox {
  private Integer minValue, maxValue, stepValue = null;

  public InputInteger() {
    super();
  }

  public InputInteger(Element element) {
    super(element);
  }
  
  public InputInteger(HasStringValue source) {
    super(source);
  }

  public InputInteger(HasStringValue source, String type, int min, int max) {
    this(source, type, min, max, 1);
  }

  public InputInteger(HasStringValue source, String type, int min, int max, int step) {
    this(source);
    initAttributes(type, min, max, step);
  }

  public InputInteger(int value) {
    this();
    setValue(value);
  }

  public InputInteger(int value, String type, int min, int max) {
    this(value, type, min, max, 1);
  }
  
  public InputInteger(int value, String type, int min, int max, int step) {
    this(value);
    initAttributes(type, min, max, step);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "int");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-InputInteger";
  }
  
  public int getIntValue() {
    return BeeUtils.toInt(getValue());
  }
  
  public Integer getMaxValue() {
    return maxValue;
  }

  public Integer getMinValue() {
    return minValue;
  }

  public Integer getStepValue() {
    return stepValue;
  }

  public void setMaxValue(Integer maxValue) {
    this.maxValue = maxValue;

    if (maxValue == null) {
      DomUtils.removeMax(this);
    } else {
      DomUtils.setMax(this, maxValue);
    }
  }

  public void setMinValue(Integer minValue) {
    this.minValue = minValue;
    
    if (minValue == null) {
      DomUtils.removeMin(this);
    } else {
      DomUtils.setMin(this, minValue);
    }
  }

  public void setStepValue(Integer stepValue) {
    this.stepValue = stepValue;

    if (stepValue == null || stepValue == 0) {
      DomUtils.removeStep(this);
    } else {
      DomUtils.setStep(this, stepValue);
    }
  }

  public void setValue(int value) {
    setValue(Integer.toString(value));
  }
  
  protected void initAttributes(String type, int min, int max, int step) {
    if (!BeeUtils.isEmpty(type)) {
      DomUtils.setInputType(this, type);
    }
    if (min < max) {
      setMinValue(min);
      setMaxValue(max);
    }
    if (step != 0) {
      setStepValue(step);
    }
  }
}
