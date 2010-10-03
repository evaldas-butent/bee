package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.dialog.BeePopupPanel;
import com.butent.bee.egg.client.dom.DomUtils;

public class MenuPopup extends BeePopupPanel {
  private static final String STYLENAME_DEFAULT = "bee-MenuPopup";

  private BeeMenuBar parentMenu = null;
  private BeeMenuItem parentItem = null;

  public MenuPopup(BeeMenuBar bar, BeeMenuItem item) {
    super(true, false);
    setStyleName(STYLENAME_DEFAULT);
    addStyleDependentName(item.getSubMenu().getDefaultWidget().toString().toLowerCase());

    this.parentMenu = bar;
    this.parentItem = item;

    setWidget(parentItem.getSubMenu());
    parentItem.getSubMenu().onShow();
  }

  @Override
  public void createId() {
    DomUtils.createId(this, "menupopup");
  }

  public BeeMenuItem getParentItem() {
    return parentItem;
  }

  public BeeMenuBar getParentMenu() {
    return parentMenu;
  }

  public void setParentItem(BeeMenuItem parentItem) {
    this.parentItem = parentItem;
  }

  public void setParentMenu(BeeMenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
