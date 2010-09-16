package com.butent.bee.egg.client.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeMenuItemSeparator extends UIObject implements HasId {
  private static final String STYLENAME_DEFAULT = "bee-MenuItemSeparator";

  private BeeMenuBar parentMenu;

  public BeeMenuItemSeparator() {
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

  public BeeMenuBar getParentMenu() {
    return parentMenu;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  void setParentMenu(BeeMenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
