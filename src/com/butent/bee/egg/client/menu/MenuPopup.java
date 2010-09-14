package com.butent.bee.egg.client.menu;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;

import com.butent.bee.egg.client.dialog.BeeDecoratedPopupPanel;
import com.butent.bee.egg.client.utils.BeeDom;

public class MenuPopup extends BeeDecoratedPopupPanel {
  private BeeMenuBar parentMenu = null;
  private BeeMenuItem parentItem = null;

  public MenuPopup() {
    super();
  }

  public MenuPopup(BeeMenuBar bar, BeeMenuItem item, boolean autoHide,
      boolean modal) {
    this(autoHide, modal);
    this.parentMenu = bar;
    this.parentItem = item;

    setWidget(parentItem.getSubMenu());
    setPreviewingAllNativeEvents(true);
    parentItem.getSubMenu().onShow();
  }

  public MenuPopup(boolean autoHide) {
    super(autoHide);
  }

  public MenuPopup(boolean autoHide, boolean modal) {
    super(autoHide, modal);
  }

  @Override
  public void createId() {
    BeeDom.createId(this, "menupopup");
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
