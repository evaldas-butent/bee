package com.butent.bee.client.modules.commons;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory.FormCallback;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

public class ParameterHandler extends AbstractFormCallback {
  private static final String CONTAINER = "Container";
  private final String module;

  private HasWidgets container = null;

  public ParameterHandler(String module) {
    Assert.notEmpty(module);
    this.module = module;
  }

  @Override
  public void afterCreateWidget(String name, Widget widget) {
    if (BeeUtils.same(name, CONTAINER) && widget instanceof HasWidgets) {
      container = (HasWidgets) widget;
      container.clear();
      container.add(new BeeLabel(module));
    }
  }

  @Override
  public FormCallback getInstance() {
    return new ParameterHandler(module);
  }
}
