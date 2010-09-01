package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.SplitLayoutPanel;

public class BeeSplit extends SplitLayoutPanel implements HasId {

  public BeeSplit() {
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
