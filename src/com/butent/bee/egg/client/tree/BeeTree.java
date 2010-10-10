package com.butent.bee.egg.client.tree;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeTree extends Tree implements HasId {
  public BeeTree() {
    super();
    init();
  }

  public BeeTree(SelectionHandler<TreeItem> handler) {
    this();
    addSelectionHandler(handler);
  }

  public void createId() {
    DomUtils.createId(this, "tree");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    createId();
    setStyleName("bee-Tree");
  }

}
