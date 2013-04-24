package com.butent.bee.client.layout;

import com.google.gwt.user.client.ui.HeaderPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;

/**
 * Handles id and prefix of header content footer layout element.
 */

public class HeaderContentFooter extends HeaderPanel implements IdentifiableWidget {

  public HeaderContentFooter() {
    super();
    DomUtils.createId(this, getIdPrefix());
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "hcf";
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
