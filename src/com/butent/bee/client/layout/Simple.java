package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Contains a class for panels that contain only one widget.
 */

public class Simple extends SimplePanel implements IdentifiableWidget, RequiresResize,
    ProvidesResize {

  public Simple() {
    super();
    init();
  }

  public Simple(Widget child) {
    this(child, null);
  }

  public Simple(Widget child, Overflow overflow) {
    super(child);
    init();

    if (overflow != null) {
      getElement().getStyle().setOverflow(overflow);
    }
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "simple";
  }

  @Override
  public void onResize() {
    if (getWidget() instanceof RequiresResize) {
      ((RequiresResize) getWidget()).onResize();
    }
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  protected void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
