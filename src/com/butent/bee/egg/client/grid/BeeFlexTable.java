package com.butent.bee.egg.client.grid;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.FlexTable;

public class BeeFlexTable extends FlexTable implements HasId {

  public BeeFlexTable() {
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "flex");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
