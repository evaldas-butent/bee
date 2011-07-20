package com.butent.bee.client.tree;

import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a browsable view of a tree in which only a single node per level may be open at one
 * time, manages it's ID.
 */

public class BeeCellBrowser extends CellBrowser implements HasId {

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    init();
  }

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue, Resources resources) {
    super(viewModel, rootValue, resources);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "cellbrowser";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
