package com.butent.bee.egg.client.widget;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.HasService;
import com.butent.bee.egg.shared.HasStage;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeButton extends Button implements HasId, HasService, HasStage, HasCommand {
  private BeeCommand command = null;

  public BeeButton() {
    super();
    init();
  }

  public BeeButton(Element element) {
    super(element);
    init();
  }

  public BeeButton(String html) {
    super(html);
    init();
  }

  public BeeButton(String html, BeeCommand cmnd) {
    this(html);

    if (cmnd != null) {
      setCommand(cmnd);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }

  public BeeButton(String html, BeeStage bst) {
    this(html, bst.getService(), bst.getStage());
  }

  public BeeButton(String html, ClickHandler handler) {
    super(html, handler);
    init();
  }

  public BeeButton(String html, String svc) {
    this(html);

    if (!BeeUtils.isEmpty(svc)) {
      setService(svc);
      BeeKeeper.getBus().addClickHandler(this);
    }
  }

  public BeeButton(String html, String svc, String stg) {
    this(html, svc);

    if (!BeeUtils.isEmpty(stg)) {
      setStage(stg);
    }
  }

  public void createId() {
    DomUtils.createId(this, DomUtils.BUTTON_ID_PREFIX);
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getService() {
    return DomUtils.getService(this);
  }

  public String getStage() {
    return DomUtils.getStage(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  public void setService(String svc) {
    DomUtils.setService(this, svc);
  }

  public void setStage(String stg) {
    DomUtils.setStage(this, stg);
  }

  private void init() {
    setStyleName("bee-Button");
    createId();
  }
  
}
