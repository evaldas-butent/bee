package com.butent.bee.client.screen;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.dom.StyleUtils.FontSize;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.widget.Toggle;

/**
 * Handles tablet computer size screen implementation.
 */

public class Tablet extends Mobile {

  public Tablet() {
    super();
  }
  
  protected int addLogToggle(BeeLayoutPanel panel) {
    final Toggle toggle = new Toggle("Hide Log", "Show Log", "toggleLog");
    StyleUtils.setFontSize(toggle, FontSize.SMALL);
    StyleUtils.setHorizontalPadding(toggle, 2);

    toggle.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        if (toggle.isDown()) {
          BeeKeeper.getLog().show();
        } else {
          BeeKeeper.getLog().hide();
        }
        toggle.invert();
      }
    });

    panel.addRightWidthTop(toggle, 3, 76, 1);
    toggle.setDown(true);
    return 80;
  }
}
