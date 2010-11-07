package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.dom.DomUtils;

public class Spinner extends InputInteger {
  private static String inputType = "number";

  public Spinner(int value, int min, int max, int step) {
    super(value, inputType, min, max, step);
  }

  public Spinner(int value, int min, int max) {
    super(value, inputType, min, max);
  }

  public Spinner(String fieldName, int min, int max) {
    super(fieldName, inputType, min, max);
  }

  public Spinner(String fieldName, int min, int max, int step) {
    super(fieldName, inputType, min, max, step);
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "spin");
  }

  @Override
  public String getDefaultStyleName() {
    return "bee-Spinner";
  }
  
}
