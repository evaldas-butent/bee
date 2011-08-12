package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Frame;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Handles id and prefix of frame elements.
 */

public class BeeFrame extends Frame implements HasId {

  public BeeFrame() {
    super();
    init();
  }

  public BeeFrame(String url) {
    super(url);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "frame";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Frame");
  }
}
