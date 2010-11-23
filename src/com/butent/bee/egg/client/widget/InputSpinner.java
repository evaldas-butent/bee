package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class InputSpinner extends InputInteger {
  private static String inputType = "number";

  public InputSpinner(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  public InputSpinner(int value, int min, int max) {
    super(value, inputType, min, max);
  }

  public InputSpinner(String fieldName, int min, int max) {
    super(fieldName, inputType, min, max);
  }

  public InputSpinner(String fieldName, int min, int max, int step) {
    super(fieldName, inputType, min, max, step);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "inp-spin");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-InputSpinner";
  }
  
}
