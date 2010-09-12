package com.butent.bee.egg.client.grid;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.cellview.client.CellTable;

public class BeeCellTable extends CellTable<Integer> implements HasId {

  public BeeCellTable() {
    createId();
  }

  public BeeCellTable(int pageSize, Resources resources) {
    super(pageSize, resources);
    createId();
  }

  public BeeCellTable(int pageSize) {
    super(pageSize);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "celltable");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }
  
  public void initData(int rows) {
    Assert.isPositive(rows);
    
    List<Integer> lst = new ArrayList<Integer>(rows);
    for (int i = 0; i < rows; i++) {
      lst.add(i);
    }
    
    setRowData(0, lst);
  }

}
