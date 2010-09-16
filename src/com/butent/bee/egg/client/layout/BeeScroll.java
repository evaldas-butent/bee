package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeScroll extends ScrollPanel implements HasId {

  public BeeScroll() {
    super();
    createId();
  }

  public BeeScroll(Widget child) {
    super(child);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "scroll");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
