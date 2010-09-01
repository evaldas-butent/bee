package com.butent.bee.egg.client.layout;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class BeeScroll extends ScrollPanel implements HasId {

  public BeeScroll() {
    super();
    createId();
  }

  public BeeScroll(Widget child) {
    super(child);
    createId();
  }

  private void createId() {
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
