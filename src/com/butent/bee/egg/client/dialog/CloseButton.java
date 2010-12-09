package com.butent.bee.egg.client.dialog;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;

public class CloseButton extends BeeButton {

  public CloseButton() {
    super("Close", BeeService.SERVICE_CLOSE_DIALOG);
  }

  public CloseButton(Element element) {
    super(element);
  }

  public CloseButton(String html) {
    super(html, BeeService.SERVICE_CLOSE_DIALOG);
  }

  public CloseButton(String html, BeeStage bst) {
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
