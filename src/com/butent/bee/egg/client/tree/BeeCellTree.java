package com.butent.bee.egg.client.tree;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeCellTree extends CellTree implements HasId {

  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    createId();
  }

  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue,
      Resources resources) {
    super(viewModel, rootValue, resources);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "celltree");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
