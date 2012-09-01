package com.butent.bee.client.dialog;

import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;

/**
 * Implements a button with "close" function.
 */
public class CloseButton extends BeeButton {

  public CloseButton(String html) {
    super(html, Service.CLOSE_DIALOG);
  }

  public CloseButton(String html, Stage bst) {
    super(html, bst);
  }

  public CloseButton(String html, ClickHandler handler) {
    super(html, handler);
  }

  public CloseButton(String html, String svc) {
    super(html, svc);
  }

  public CloseButton(String html, String svc, String stg) {
    super(html, svc, stg);
  }
}
