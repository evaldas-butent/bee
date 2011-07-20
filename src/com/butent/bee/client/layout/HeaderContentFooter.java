package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.HeaderPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class HeaderContentFooter extends HeaderPanel implements HasId {

  public HeaderContentFooter() {
    super();
    DomUtils.createId(this, getIdPrefix());
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "hcf";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
