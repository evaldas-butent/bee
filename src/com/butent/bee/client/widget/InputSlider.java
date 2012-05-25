package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.ui.EditorAction;

/**
 * Implements a user interface component which lets to select a value by dragging a slider with a
 * mouse.
 */

public class InputSlider extends InputInteger {

  private static String inputType = "range";

  public InputSlider() {
    super();
    DomUtils.setInputType(this, inputType);
  }

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
  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getDefaultStyleName() {
    return "bee-InputSlider";
  }

  @Override
  public String getIdPrefix() {
    return "inp-slider";
  }
  
  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_SLIDER;
  }
}
