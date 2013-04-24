package com.butent.bee.client.menu;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;

import com.butent.bee.client.tree.TreeItem;

/**
 * Checks whether selected menu item has a command, and if so, calls it.
 */

public class MenuSelectionHandler implements SelectionHandler<TreeItem> {

  @Override
  public void onSelection(SelectionEvent<TreeItem> event) {
    Object obj = event.getSelectedItem().getUserObject();
    if (obj instanceof MenuCommand) {
      ((MenuCommand) obj).execute();
    }
  }
}
