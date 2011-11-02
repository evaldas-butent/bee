package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.ResizeLayoutPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.HasId;

/**
 * Handles id and prefix of resizable panel layout element.
 */

public class ResizePanel extends ResizeLayoutPanel implements HasId {

  public ResizePanel() {
    super();
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "resizer";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  protected void setDummyWidget() {
    setWidget(new Html());
  }
}
