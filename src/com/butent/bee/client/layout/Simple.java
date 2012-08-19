package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Contains a class for panels that contain only one widget.
 */

public class Simple extends SimplePanel implements HasId, RequiresResize, ProvidesResize {

  public Simple() {
    super();
    DomUtils.createId(this, getIdPrefix());
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
}
