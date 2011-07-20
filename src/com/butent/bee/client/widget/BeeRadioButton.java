package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.RadioButton;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.utils.HasCommand;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.HasService;

/**
 * Implements a mutually-exclusive selection radio button user interface component.
 */

public class BeeRadioButton extends RadioButton implements HasId, HasService, HasCommand {
  
  private BeeCommand command = null;

  public BeeRadioButton(String name) {
    super(name);
    init();
  }

  public BeeRadioButton(String name, BeeCommand cmnd) {
    this(name);
    setCommand(cmnd);
  }

  public BeeRadioButton(String name, String label) {
    super(name, label);
    init();
  }

  public BeeRadioButton(String name, String label, boolean asHTML) {
    super(name, label, asHTML);
    init();
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return DomUtils.RADIO_ID_PREFIX;
  }

  public String getService() {
    return DomUtils.getService(this);
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

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-RadioButton");
  }
}
