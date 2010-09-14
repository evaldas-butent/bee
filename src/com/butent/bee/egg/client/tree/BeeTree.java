package com.butent.bee.egg.client.tree;

import com.google.gwt.user.client.ui.Tree;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeTree extends Tree implements HasId {

  public BeeTree() {
    super();
    createId();
  }

  public BeeTree(Resources resources) {
    super(resources);
    createId();
  }

  public BeeTree(Resources resources, boolean useLeafImages) {
    super(resources, useLeafImages);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "tree");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
