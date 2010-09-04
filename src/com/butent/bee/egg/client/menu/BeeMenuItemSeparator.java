package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.MenuItemSeparator;

public class BeeMenuItemSeparator extends MenuItemSeparator implements HasId {

  public BeeMenuItemSeparator() {
    super();
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  private void createId() {
    BeeDom.setId(this);
  }

}
