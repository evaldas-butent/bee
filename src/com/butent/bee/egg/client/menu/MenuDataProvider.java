package com.butent.bee.egg.client.menu;

import java.util.List;

import com.butent.bee.egg.shared.menu.MenuEntry;
import com.butent.bee.egg.shared.menu.MenuUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.view.client.ListDataProvider;

public class MenuDataProvider extends ListDataProvider<MenuEntry> {

  public MenuDataProvider() {
    super();
  }

  public MenuDataProvider(List<MenuEntry> lst) {
    super(lst);
  }

  @SuppressWarnings("unchecked")
  public MenuDataProvider(List<MenuEntry> roots, List<MenuEntry> items) {
    this(BeeUtils.join(roots, items));
  }
  
  public List<MenuEntry> getChildren(String id, boolean isOrdered) {
    return MenuUtils.getChildren(getList(), id, isOrdered);
  }
  
}
