package com.butent.bee.egg.client.menu;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class MenuPositionCallback implements PositionCallback {
  private Element parent = null;
  private Element item = null;
  private MenuPopup popup = null;
  private boolean vertical = false;

  public MenuPositionCallback(Element parent, Element item, MenuPopup popup,
      boolean vertical) {
    this.parent = parent;
    this.item = item;
    this.popup = popup;
    this.vertical = vertical;
  }

  @Override
  public void setPosition(int offsetWidth, int offsetHeight) {
    Assert.notNull(parent);
    Assert.notNull(popup);

    int x, y;
    
    if (vertical) {
      x = parent.getAbsoluteLeft() + parent.getOffsetWidth() + 5;
      y = item.getAbsoluteTop();
    } else {
      x = item.getAbsoluteLeft();
      y = parent.getAbsoluteTop() + parent.getOffsetHeight() + 2;
    }

    x = BeeUtils.fitStart(x, offsetWidth, DomUtils.getClientWidth());
    y = BeeUtils.fitStart(y, offsetHeight, DomUtils.getClientHeight());

    popup.setPopupPosition(x, y);
  }

}
