package com.butent.bee.egg.client.tree;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.Tree;

public class BeeTree extends Tree implements HasId {

  public BeeTree() {
    super();
    createId();
  }

  public BeeTree(Resources resources, boolean useLeafImages) {
    super(resources, useLeafImages);
    createId();
  }

  public BeeTree(Resources resources) {
    super(resources);
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
