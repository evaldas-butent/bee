package com.butent.bee.egg.client.dialog;

import com.butent.bee.egg.client.widget.BeeButton;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;

public class BeeCloseButton extends BeeButton {

  public BeeCloseButton() {
    super("Close", BeeService.SERVICE_CLOSE_DIALOG);
  }

  public BeeCloseButton(Element element) {
    super(element);
  }

  public BeeCloseButton(String html, ClickHandler handler) {
    super(html, handler);
  }

  public BeeCloseButton(String html) {
    super(html, BeeService.SERVICE_CLOSE_DIALOG);
  }

  public BeeCloseButton(String html, String svc, String stg) {
    super(html, svc, stg);
  }

  public BeeCloseButton(String html, String svc) {
    super(html, svc);
  }

  public BeeCloseButton(String html, BeeStage bst) {
    super(html, bst);
  }

}
