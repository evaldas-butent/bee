package com.butent.bee.egg.client.layout;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.TabLayoutPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeTab extends TabLayoutPanel implements HasId {

  public BeeTab(double barHeight, Unit barUnit) {
    super(barHeight, barUnit);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "tab");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
