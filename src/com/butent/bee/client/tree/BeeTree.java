package com.butent.bee.client.tree;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Handles a tree user interface component, manages it's ID.
 */

public class BeeTree extends Tree implements HasId {

  public BeeTree() {
    super();
    init();
  }

  public BeeTree(SelectionHandler<TreeItem> handler) {
    this();
    addSelectionHandler(handler);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "tree";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-Tree");
  }
}
