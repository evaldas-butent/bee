package com.butent.bee.client.tree;

import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class BeeCellBrowser extends CellBrowser implements HasId {

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    createId();
  }

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue,
      Resources resources) {
    super(viewModel, rootValue, resources);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "cellbrowser");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
