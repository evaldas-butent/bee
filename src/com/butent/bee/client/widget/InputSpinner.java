package com.butent.bee.client.widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.ui.EditorAction;

/**
 * Implements standard spinner user interface component, letting user to increase and decrease input
 * value as well as input it straight from the keyboard.
 */

public class InputSpinner extends InputInteger {

  private static String inputType = "number";
  
  public InputSpinner() {
    super();
    DomUtils.setInputType(this, inputType);
  }

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
  public EditorAction getDefaultFocusAction() {
    return null;
  }
  
  @Override
  public String getDefaultStyleName() {
    return "bee-InputSpinner";
  }

  @Override
  public String getIdPrefix() {
    return "inp-spin";
  }
}
