package com.butent.bee.client.tree;

import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Handles a cell tree user interface component, manages it's ID.
 */

public class BeeCellTree extends CellTree implements HasId {
  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    init();
  }

  public <T> BeeCellTree(TreeViewModel viewModel, T rootValue, Resources resources) {
    super(viewModel, rootValue, resources);
    init();
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

  private void init() {
    createId();
    setStyleName("bee-CellTree");
  }
}
