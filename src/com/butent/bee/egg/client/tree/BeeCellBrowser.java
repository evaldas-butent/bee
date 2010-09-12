package com.butent.bee.egg.client.tree;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.cellview.client.CellBrowser;
import com.google.gwt.view.client.TreeViewModel;

public class BeeCellBrowser extends CellBrowser implements HasId {

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue,
      Resources resources) {
    super(viewModel, rootValue, resources);
    createId();
  }

  public <T> BeeCellBrowser(TreeViewModel viewModel, T rootValue) {
    super(viewModel, rootValue);
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void createId() {
    BeeDom.createId(this, "cellbrowser");
  }

}
