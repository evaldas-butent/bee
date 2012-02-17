package com.butent.bee.client.tree;

import com.google.gwt.user.client.ui.Widget;

public interface HasTreeItems {

  TreeItem addItem(String itemHtml);
  
  void addItem(TreeItem item);

  TreeItem addItem(Widget widget);

  void removeItem(TreeItem item);

  void removeItems();
}
