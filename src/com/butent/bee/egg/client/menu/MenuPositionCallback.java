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
  private boolean flow = false;

  public MenuPositionCallback(Element parent, Element item, MenuPopup popup,
      boolean vertical, boolean flow) {
    this.parent = parent;
    this.item = item;
    this.popup = popup;
    this.vertical = vertical;
    this.flow = flow;
  }

  @Override
  public void setPosition(int offsetWidth, int offsetHeight) {
    Assert.notNull(parent);
    Assert.notNull(popup);

    int x, y;
    
    if (flow) {
      x = item.getAbsoluteLeft() + 20;
      y = item.getAbsoluteTop() + item.getOffsetHeight() + 2;
    } else if (vertical) {
      x = parent.getAbsoluteLeft() + parent.getOffsetWidth() + 5;
      y = item.getAbsoluteTop();
    } else {
      x = item.getAbsoluteLeft();
      y = parent.getAbsoluteTop() + parent.getOffsetHeight() + 2;
    }

    x = BeeUtils.fitStart(x, offsetWidth, DomUtils.getClientWidth() - 20, 10);
    y = BeeUtils.fitStart(y, offsetHeight, DomUtils.getClientHeight() - 20, 10);

    popup.setPopupPosition(x, y);
  }

}
