package com.butent.bee.client.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class MenuSeparator extends UIObject implements HasId {
  private static final String STYLENAME_DEFAULT = "bee-MenuItemSeparator";

  private MenuBar parentMenu;

  public MenuSeparator() {
    Element elem = DOM.createDiv();
    setElement(elem);

    setStyleName(elem, STYLENAME_DEFAULT);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "sep");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public MenuBar getParentMenu() {
    return parentMenu;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  void setParentMenu(MenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
