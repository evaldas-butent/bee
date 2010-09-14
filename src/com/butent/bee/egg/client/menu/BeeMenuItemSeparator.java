package com.butent.bee.egg.client.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeMenuItemSeparator extends UIObject implements HasId {

  private static final String STYLENAME_DEFAULT = "gwt-MenuItemSeparator";

  private BeeMenuBar parentMenu;

  public BeeMenuItemSeparator() {
    setElement(DOM.createTD());
    setStyleName(STYLENAME_DEFAULT);

    Element div = DOM.createDiv();
    DOM.appendChild(getElement(), div);
    setStyleName(div, "menuSeparatorInner");

    createId();
  }

  public void createId() {
    BeeDom.createId(this, "separator");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public BeeMenuBar getParentMenu() {
    return parentMenu;
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  void setParentMenu(BeeMenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
