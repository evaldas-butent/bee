package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Contains a panel that wraps its contents in a scrollable area.
 */

public class Scroll extends ScrollPanel implements HasId {

  public Scroll() {
    super();
    init();
  }

  public Scroll(Widget child) {
    super(child);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "scroll";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
