package com.butent.bee.egg.client.menu;

import com.google.gwt.cell.client.AbstractCell;

import com.butent.bee.egg.shared.menu.MenuEntry;

public class MenuCell extends AbstractCell<MenuEntry> {

  @Override
  public void render(MenuEntry value, Object key, StringBuilder sb) {
    if (value != null) {
      sb.append(value.getText());
    }
  }

}
