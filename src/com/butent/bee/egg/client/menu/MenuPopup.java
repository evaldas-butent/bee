package com.butent.bee.egg.client.menu;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

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
    setPreviewingAllNativeEvents(true);
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

  @Override
  protected void onPreviewNativeEvent(NativePreviewEvent event) {
    if (!event.isCanceled()) {

      switch (event.getTypeInt()) {
        case Event.ONMOUSEDOWN:
          EventTarget target = event.getNativeEvent().getEventTarget();
          Element parentMenuElement = parentItem.getParentMenu().getElement();
          if (parentMenuElement.isOrHasChild(Element.as(target))) {
            event.cancel();
            return;
          }
          super.onPreviewNativeEvent(event);
          if (event.isCanceled()) {
            parentMenu.selectItem(null);
          }
          return;
      }
    }
    super.onPreviewNativeEvent(event);
  }

}
