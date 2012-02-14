package com.butent.bee.client.tree;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that implements this interface contains {@link com.google.gwt.user.client.ui.TreeItem
 * items} and can operate them.
 */
public interface HasTreeItems {

  /**
   * Extends this interface with convenience methods to handle {@link IsWidget}.
   */
  interface ForIsWidget extends HasTreeItems {
    TreeItem addItem(IsWidget w);
  }

  /**
   * Adds a simple tree item containing the specified html.
   * 
   * @param itemHtml the html of the item to be added
   * @return the item that was added
   */
  TreeItem addItem(SafeHtml itemHtml);

  /**
   * Adds an tree item.
   * 
   * @param item the item to be added
   */
  void addItem(TreeItem item);

  /**
   * Adds an item wrapped by specified {@link IsTreeItem}.
   * 
   * @param isItem the wrapper of item to be added
   */
  void addItem(IsTreeItem isItem);

  /**
   * Adds a new tree item containing the specified widget.
   * 
   * @param widget the widget to be added
   * @return the new item
   */
  TreeItem addItem(Widget widget);

  /**
   * Adds a simple tree item containing the specified text.
   * 
   * @param itemText the text of the item to be added
   * @return the item that was added
   */
  TreeItem addTextItem(String itemText);

  /**
   * Removes an item.
   * 
   * @param item the item to be removed
   */
  void removeItem(TreeItem item);

  /**
   * Removes an item.
   * 
   * @param isItem the wrapper of item to be removed
   */
  void removeItem(IsTreeItem isItem);

  /**
   * Removes all items.
   */
  void removeItems();

}
