package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasStringValue;

public class InputSpinner extends InputInteger {
  private static String inputType = "number";

  public InputSpinner(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  public InputSpinner(int value, int min, int max) {
    super(value, inputType, min, max);
  }

  public InputSpinner(HasStringValue source, int min, int max) {
    super(source, inputType, min, max);
  }

  public InputSpinner(HasStringValue source, int min, int max, int step) {
    super(source, inputType, min, max, step);
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
