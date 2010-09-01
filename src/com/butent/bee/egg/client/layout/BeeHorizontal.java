package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class BeeHorizontal extends HorizontalPanel implements HasId {

  public BeeHorizontal() {
    super();
    BeeDom.setId(this);
  }

  @Override
  public String getId() {
    return BeeDom.getId(this);
  }

  @Override
  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
