package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasStringValue;

public class InputSlider extends InputInteger {
  private static String inputType = "range";

  public InputSlider(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  public InputSlider(int value, int min, int max) {
    super(value, inputType, min, max);
  }

  public InputSlider(HasStringValue source, int min, int max) {
    super(source, inputType, min, max);
  }

  public InputSlider(HasStringValue source, int min, int max, int step) {
    super(source, inputType, min, max, step);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "inp-slider");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-InputSlider";
  }
  
}
