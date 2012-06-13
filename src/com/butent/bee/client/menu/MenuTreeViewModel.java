package com.butent.bee.client.menu;

import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.shared.menu.Menu;
import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuItem;

/**
 * Enables to represent menu in a tree view.
 */

public class MenuTreeViewModel implements TreeViewModel {
  private MenuDataProvider rootProvider = null;
  private MenuCell cell = null;

  public MenuTreeViewModel(MenuDataProvider rootProvider) {
    this(rootProvider, new MenuCell());
  }

  public MenuTreeViewModel(MenuDataProvider rootProvider, MenuCell cell) {
    super();
    this.rootProvider = rootProvider;
    this.cell = cell;
  }

  public MenuCell getCell() {
    return cell;
  }

  @Override
  public <T> NodeInfo<?> getNodeInfo(T value) {
    MenuDataProvider provider;

    if (value == null) {
      return new DefaultNodeInfo<Menu>(rootProvider, cell);
    } else if (value instanceof MenuEntry) {
      provider = new MenuDataProvider(((MenuEntry) value).getItems());
      return new DefaultNodeInfo<Menu>(provider, cell);
    } else {
      return null;
    }
  }

  @Override
  public boolean isLeaf(Object value) {
    return (value instanceof MenuItem);
  }

  public void setCell(MenuCell cell) {
    this.cell = cell;
  }

}
