package com.butent.bee.egg.client.tree;

import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeTreeItem extends TreeItem implements HasId {

  public BeeTreeItem() {
    super();
  }

  public BeeTreeItem(String html) {
    super(html);
  }

  public BeeTreeItem(Widget widget) {
    super(widget);
  }

  public void createId() {
    BeeDom.createId(this, "treeitem");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
