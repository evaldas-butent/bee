package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class BeeMenuItem extends MenuItem implements HasId {

  public BeeMenuItem(String text, boolean asHTML, Command cmd) {
    super(text, asHTML, cmd);
  }

  public BeeMenuItem(String text, boolean asHTML, MenuBar subMenu) {
    super(text, asHTML, subMenu);
  }

  public BeeMenuItem(String text, Command cmd) {
    super(text, cmd);
  }

  public BeeMenuItem(String text, MenuBar subMenu) {
    super(text, subMenu);
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
