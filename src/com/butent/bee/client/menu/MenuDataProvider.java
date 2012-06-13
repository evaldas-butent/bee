package com.butent.bee.client.menu;

import com.google.gwt.view.client.ListDataProvider;

import com.butent.bee.shared.menu.Menu;

import java.util.List;

/**
 * Fetches menu data for {@code MenuTreeViewModel}.
 */

public class MenuDataProvider extends ListDataProvider<Menu> {

  public MenuDataProvider() {
    super();
  }

  public MenuDataProvider(List<Menu> lst) {
    super(lst);
  }
}
