package com.butent.bee.client.tree;

import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Handles a tree item user interface component, manages it's ID.
 */

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

  public BeeTreeItem(String html, Object obj) {
    this(html);
    setUserObject(obj);
  }

  public void addText(Object... obj) {
    Assert.notNull(obj);
    Assert.parameterCount(obj.length, 1);
    addItem(BeeUtils.concat(1, obj));
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "treeitem";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
