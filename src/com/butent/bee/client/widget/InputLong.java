package com.butent.bee.client.widget;

import com.google.gwt.dom.client.Element;

import com.butent.bee.client.ui.FormWidget;
import com.butent.bee.shared.HasStringValue;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Enables to use a component for input of {@code Long} type values.
 */

public class InputLong extends InputNumber {

  public InputLong() {
    super();
  }

  public InputLong(Element element) {
    super(element);
  }

  public InputLong(HasStringValue source) {
    super(source);
  }

  @Override
  public String getIdPrefix() {
    return "long";
  }

  @Override
  public FormWidget getWidgetType() {
    return FormWidget.INPUT_LONG;
  }
  
  @Override
  protected boolean checkType(String v) {
    return BeeUtils.isLong(v);
  }

  @Override
  protected String getDefaultStyleName() {
    return "bee-InputLong";
  }
}
