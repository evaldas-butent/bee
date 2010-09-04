package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.MenuBar;

public class BeeMenuBar extends MenuBar implements HasId {

  public BeeMenuBar() {
    super();
    createId();
  }

  public BeeMenuBar(boolean vertical, Resources resources) {
    super(vertical, resources);
    createId();
  }

  public BeeMenuBar(boolean vertical) {
    super(vertical);
    createId();
  }

  public BeeMenuBar(Resources resources) {
    super(resources);
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
