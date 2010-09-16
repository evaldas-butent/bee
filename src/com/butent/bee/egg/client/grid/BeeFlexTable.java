package com.butent.bee.egg.client.grid;

import com.google.gwt.user.client.ui.FlexTable;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeFlexTable extends FlexTable implements HasId {

  public BeeFlexTable() {
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "flex");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
