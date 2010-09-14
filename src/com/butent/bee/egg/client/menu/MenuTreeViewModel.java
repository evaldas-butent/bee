package com.butent.bee.egg.client.menu;

import com.google.gwt.view.client.TreeViewModel;

import com.butent.bee.egg.shared.menu.MenuEntry;

import java.util.List;

public class MenuTreeViewModel implements TreeViewModel {
  private MenuDataProvider rootProvider = null;
  private MenuDataProvider itemProvider = null;
  private MenuCell cell = null;

  public MenuTreeViewModel(MenuDataProvider rootProvider,
      MenuDataProvider itemProvider) {
    this(rootProvider, itemProvider, new MenuCell());
  }

  public MenuTreeViewModel(MenuDataProvider rootProvider,
      MenuDataProvider itemProvider, MenuCell cell) {
    super();
    this.rootProvider = rootProvider;
    this.itemProvider = itemProvider;
    this.cell = cell;
  }

  public MenuCell getCell() {
    return cell;
  }

  @Override
  public <T> NodeInfo<?> getNodeInfo(T value) {
    List<MenuEntry> lst;
    MenuDataProvider provider;

    if (value == null) {
      return new DefaultNodeInfo<MenuEntry>(rootProvider, cell);
    } else if (value instanceof MenuEntry) {
      lst = itemProvider.getChildren(((MenuEntry) value).getId(), true);
      provider = new MenuDataProvider(lst);
      return new DefaultNodeInfo<MenuEntry>(provider, cell);
    } else {
      return null;
    }
  }

  @Override
  public boolean isLeaf(Object value) {
    if (value instanceof MenuEntry) {
      return ((MenuEntry) value).isLeaf();
    } else {
      return false;
    }
  }

  public void setCell(MenuCell cell) {
    this.cell = cell;
  }

}
