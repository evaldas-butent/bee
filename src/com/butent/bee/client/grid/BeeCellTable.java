package com.butent.bee.client.grid;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.view.client.ProvidesKey;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.IsTable;

public class BeeCellTable extends CellTable<IsRow> implements HasId {

  public BeeCellTable() {
    init();
  }

  public BeeCellTable(int pageSize) {
    super(pageSize);
    init();
  }

  public BeeCellTable(int pageSize, ProvidesKey<IsRow> keyProvider) {
    super(pageSize, keyProvider);
    init();
  }

  public void createId() {
    DomUtils.createId(this, "celltable");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void initData(IsTable<?, ?> data) {
    Assert.notNull(data);

    setRowCount(data.getNumberOfRows());
    setRowData(0, data.getRows().getList());
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    createId();
  }
}
