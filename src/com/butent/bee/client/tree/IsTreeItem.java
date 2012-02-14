package com.butent.bee.client.tree;

/**
 * Extended by objects which have underlying {@link TreeItem}.
 * Provides access to that item, if it exists, without compromising the
 * ability to provide a mock object instance in JRE unit tests.
 */
public interface IsTreeItem {

  /**
   * Returns the {@link TreeItem} aspect of the receiver.
   */
  TreeItem asTreeItem();
}
