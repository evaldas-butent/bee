package com.butent.bee.egg.client.menu;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.UIObject;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.HasId;

public class BeeMenuItem extends UIObject implements HasId {
  public static BeeWidget DEFAULT_WIDGET = BeeWidget.LABEL;
  private static final String DEPENDENT_STYLENAME_SELECTED_ITEM = "selected";

  private MenuCommand command;
  private BeeMenuBar parentMenu, subMenu;
  
  private BeeWidget widgetType;

  public BeeMenuItem(BeeMenuBar parent, String text, BeeMenuBar subMenu) {
    init(parent, text, getDefaultWidget(parent));
    setSubMenu(subMenu);
  }

  public BeeMenuItem(BeeMenuBar parent, String text, BeeWidget type, BeeMenuBar subMenu) {
    init(parent, text, type);
    setSubMenu(subMenu);
  }

  public BeeMenuItem(BeeMenuBar parent, String text, BeeWidget type, MenuCommand cmd) {
    init(parent, text, type);
    setCommand(cmd);
  }

  public BeeMenuItem(BeeMenuBar parent, String text, MenuCommand cmd) {
    init(parent, text, getDefaultWidget(parent));
    setCommand(cmd);
  }

  public void createId() {
    DomUtils.createId(this, "menuitem");
  }

  public MenuCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public BeeMenuBar getParentMenu() {
    return parentMenu;
  }

  public BeeMenuBar getSubMenu() {
    return subMenu;
  }

  public BeeWidget getWidgetType() {
    return widgetType;
  }

  public void setCommand(MenuCommand cmd) {
    command = cmd;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
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

  public void setWidgetType(BeeWidget widgetType) {
    this.widgetType = widgetType;
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

  private BeeWidget getDefaultWidget(BeeMenuBar parent) {
    BeeWidget w = null;
    
    if (parent != null) {
      w = parent.getDefaultWidget();
    }
    if (w == null) {
      w = DEFAULT_WIDGET;
    }
    
    return w;
  }
  
  private void init(BeeMenuBar parent, String text, BeeWidget type) {
    Element elem;
    
    switch (type) {
      case BUTTON :
        elem = DomUtils.createButton(text).cast();
        break;
      case HTML :
        elem = DomUtils.createHtml(text).cast();
        break;
      case RADIO :
        elem = DomUtils.createRadio(parent.getName(), text).cast();
        break;
      default :
        elem = DomUtils.createLabel(text).cast();
    }
    
    setElement(elem);

    setStyleName("bee-MenuItem");
    setSelectionStyle(false);
    
    createId();
    
    setParentMenu(parent);
    setWidgetType(type);
  }

}
