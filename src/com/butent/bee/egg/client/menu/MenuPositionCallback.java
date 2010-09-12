package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.user.client.Element;

import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

public class MenuPositionCallback implements PositionCallback {
  private Element parent = null;
  private MenuPopup popup = null;

  private boolean vertical = false;;
  private boolean rtl = false;

  public MenuPositionCallback(Element parent, MenuPopup popup,
      boolean vertical, boolean rtl) {
    this.parent = parent;
    this.popup = popup;
    this.vertical = vertical;
    this.rtl = rtl;
  }

  @Override
  public void setPosition(int offsetWidth, int offsetHeight) {
    Assert.notNull(parent);
    Assert.notNull(popup);

    int x, y;

    if (rtl) {
      if (vertical) {
        x = parent.getAbsoluteLeft() - offsetWidth - 1;
        y = parent.getAbsoluteTop();
      } else {
        x = parent.getAbsoluteLeft() + parent.getOffsetWidth() - offsetWidth;
        y = parent.getAbsoluteTop() + parent.getOffsetHeight() + 1;
      }
    } else {
      if (vertical) {
        x = parent.getAbsoluteLeft() + parent.getOffsetWidth() + 3;
        y = parent.getAbsoluteTop();
      } else {
        x = parent.getAbsoluteLeft();
        y = parent.getAbsoluteTop() + parent.getOffsetHeight() + 2;
      }
    }
    
    x = BeeUtils.fitStart(x, offsetWidth, BeeDom.getClientWidth() - 10, 10);
    y = BeeUtils.fitStart(y, offsetHeight, BeeDom.getClientHeight() - 10, 10);

    popup.setPopupPosition(x, y);
  }

}
