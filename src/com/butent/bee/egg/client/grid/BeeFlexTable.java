package com.butent.bee.egg.client.grid;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.FlexTable;

public class BeeFlexTable extends FlexTable implements HasId {

  public BeeFlexTable() {
    super();
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
