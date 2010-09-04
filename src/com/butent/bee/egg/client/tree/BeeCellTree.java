package com.butent.bee.egg.client.tree;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.TreeViewModel;

public class BeeCellTree extends CellTree implements HasId {

  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue,
      Resources resources) {
    super(viewModel, rootValue, resources);
    createId();
  }

  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  private void createId() {
    BeeDom.setId(this);
  }

}
