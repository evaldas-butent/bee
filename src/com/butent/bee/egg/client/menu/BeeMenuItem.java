package com.butent.bee.egg.client.menu;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeMenuItem extends UIObject implements HasHTML, HasId {
  private static final String DEPENDENT_STYLENAME_SELECTED_ITEM = "selected";

  private MenuCommand command;
  private BeeMenuBar parentMenu, subMenu;

  public BeeMenuItem(String text, BeeMenuBar subMenu) {
    this(text, false);
    setSubMenu(subMenu);
  }

  public BeeMenuItem(String text, boolean asHTML, BeeMenuBar subMenu) {
    this(text, asHTML);
    setSubMenu(subMenu);
  }

  public BeeMenuItem(String text, boolean asHTML, MenuCommand cmd) {
    this(text, asHTML);
    setCommand(cmd);
  }

  public BeeMenuItem(String text, MenuCommand cmd) {
    this(text, false);
    setCommand(cmd);
  }

  BeeMenuItem(String text, boolean asHTML) {
    setElement(DOM.createTD());
    setSelectionStyle(false);

    if (asHTML) {
      setHTML(text);
    } else {
      setText(text);
    }
    setStyleName("gwt-MenuItem");

    createId();
  }

  public void createId() {
    BeeDom.createId(this, "menuitem");
  }

  public MenuCommand getCommand() {
    return command;
  }

  public String getHTML() {
    return DOM.getInnerHTML(getElement());
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public BeeMenuBar getParentMenu() {
    return parentMenu;
  }

  public BeeMenuBar getSubMenu() {
    return subMenu;
  }

  public String getText() {
    return DOM.getInnerText(getElement());
  }

  public void setCommand(MenuCommand cmd) {
    command = cmd;
  }

  public void setHTML(String html) {
    DOM.setInnerHTML(getElement(), html);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void setSubMenu(BeeMenuBar subMenu) {
    this.subMenu = subMenu;
    if (this.parentMenu != null) {
      this.parentMenu.updateSubmenuIcon(this);
    }

    if (subMenu != null) {
      subMenu.getElement().setTabIndex(-1);
    }
  }

  public void setText(String text) {
    DOM.setInnerText(getElement(), text);
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    if (subMenu != null) {
      subMenu.setMenuItemDebugIds(baseID);
    }
  }

  protected void setSelectionStyle(boolean selected) {
    if (selected) {
      addStyleDependentName(DEPENDENT_STYLENAME_SELECTED_ITEM);
    } else {
      removeStyleDependentName(DEPENDENT_STYLENAME_SELECTED_ITEM);
    }
  }

  void setParentMenu(BeeMenuBar parentMenu) {
    this.parentMenu = parentMenu;
  }

}
