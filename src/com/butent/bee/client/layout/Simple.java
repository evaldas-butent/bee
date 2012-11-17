package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Contains a class for panels that contain only one widget.
 */

public class Simple extends SimplePanel implements IdentifiableWidget, RequiresResize, ProvidesResize {

  public Simple() {
    super();
    init();
  }
  
  public Simple(Widget child) {
    super(child);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "simple";
  }

  @Override
  public void onResize() {
    if (getWidget() instanceof RequiresResize) {
      ((RequiresResize) getWidget()).onResize();
    }
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
