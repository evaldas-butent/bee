package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasStringValue;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class InputInteger extends BeeTextBox {

  public InputInteger() {
    super();
  }

  public InputInteger(Element element) {
    super(element);
  }
  
  public InputInteger(int value) {
    this();
    setValue(value);
  }

  public InputInteger(HasStringValue source) {
    super(source);
  }

  public InputInteger(int value, String type, int min, int max) {
    this(value, type, min, max, 1);
  }

  public InputInteger(int value, String type, int min, int max, int step) {
    this(value);
    initAttributes(type, min, max, step);
  }

  public InputInteger(HasStringValue source, String type, int min, int max) {
    this(source, type, min, max, 1);
  }
  
  public InputInteger(HasStringValue source, String type, int min, int max, int step) {
    this(source);
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
  
  public void setValue(int value) {
    setValue(Integer.toString(value));
  }
  
  protected void initAttributes(String type, int min, int max, int step) {
    if (!BeeUtils.isEmpty(type)) {
      DomUtils.setType(this, type);
    }
    if (min < max) {
      DomUtils.setMin(this, min);
      DomUtils.setMax(this, max);
    }
    if (step != 0) {
      DomUtils.setStep(this, step);
    }
  }
}
