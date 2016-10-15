package com.butent.bee.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.font.FontAwesome;

public class CustomAction extends FaLabel {
  private static final String STYLE_NAME_RUNNING = "bee-rotate";

  private final FontAwesome idle;
  private FontAwesome running = FontAwesome.SPINNER;

  public CustomAction(FontAwesome fa, ClickHandler action) {
    super(Assert.notNull(fa));
    idle = fa;
    addClickHandler(action);
  }

  public void idle() {
    setChar(idle);
    removeStyleName(STYLE_NAME_RUNNING);
    setEnabled(true);
  }

  public void running() {
    setChar(running);
    addStyleName(STYLE_NAME_RUNNING);
    setEnabled(false);
  }
}
