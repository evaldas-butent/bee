package com.butent.bee.client.menu;

import com.google.gwt.view.client.ListDataProvider;

import com.butent.bee.shared.menu.MenuEntry;
import com.butent.bee.shared.menu.MenuUtils;

import java.util.List;

/**
 * Fetches menu data for {@code MenuTreeViewModel}.
 */

public class MenuDataProvider extends ListDataProvider<MenuEntry> {
  private int limit = 0;

  public MenuDataProvider() {
    super();
  }

  public MenuDataProvider(List<MenuEntry> lst) {
    super(lst);
  }

  public MenuDataProvider(List<MenuEntry> lst, int limit) {
    this(lst);
    this.limit = limit;
  }

  public List<MenuEntry> getChildren(String id, boolean isOrdered) {
    return MenuUtils.getChildren(getList(), id, isOrdered, getLimit());
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

}
