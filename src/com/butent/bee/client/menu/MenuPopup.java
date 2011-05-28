package com.butent.bee.client.menu;

import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dom.DomUtils;

/**
 * Implements a menu element in style of {@code BeePopupPanel}.
 */

public class MenuPopup extends Popup {
  private static final String STYLENAME_DEFAULT = "bee-MenuPopup";

  private MenuBar parentMenu = null;
  private MenuItem parentItem = null;

  public MenuPopup(MenuBar bar, MenuItem item) {
    super(true, false);
    setStyleName(STYLENAME_DEFAULT);
    addStyleDependentName(item.getSubMenu().getDefaultItemType().toString().toLowerCase());

    this.parentMenu = bar;
    this.parentItem = item;

    setWidget(parentItem.getSubMenu());
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "menupopup");
  }

  public MenuItem getParentItem() {
    return parentItem;
  }

  public MenuBar getParentMenu() {
    return parentMenu;
  }

  public void setParentItem(MenuItem parentItem) {
    this.parentItem = parentItem;
  }

  public void setParentMenu(MenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
