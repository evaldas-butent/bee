package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class InputSlider extends InputInteger {
  private static String inputType = "range";

  public InputSlider(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  public InputSlider(int value, int min, int max) {
    super(value, inputType, min, max);
  }

  public InputSlider(String fieldName, int min, int max) {
    super(fieldName, inputType, min, max);
  }

  public InputSlider(String fieldName, int min, int max, int step) {
    super(fieldName, inputType, min, max, step);
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
