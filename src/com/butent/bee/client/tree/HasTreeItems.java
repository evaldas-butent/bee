package com.butent.bee.client.tree;

import com.google.gwt.user.client.ui.Widget;

import java.util.Collection;

public interface HasTreeItems {

  TreeItem addItem(String itemHtml);

  void addItem(TreeItem item);

  TreeItem addItem(Widget widget);

  int getItemCount();

  Collection<TreeItem> getTreeItems();

  void removeItem(TreeItem item);

  void removeItems();
}
