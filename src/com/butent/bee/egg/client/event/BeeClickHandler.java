package com.butent.bee.egg.client.event;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.widget.BeeButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class BeeClickHandler implements ClickHandler {

  public void onClick(ClickEvent event) {
    Object source = event.getSource();

    if (source instanceof BeeButton) {
      BeeButton b = (BeeButton) source;
      BeeKeeper.getBus().dispatchService(b.getService(), b.getStage(), event);
    }
  }

}
