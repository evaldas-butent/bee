package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.ui.RadioButton;

import com.butent.bee.egg.client.utils.BeeCommand;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.utils.HasCommand;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.HasService;

public class BeeRadioButton extends RadioButton implements HasId, HasService,
    HasCommand {
  private BeeCommand command = null;

  public BeeRadioButton(String name) {
    super(name);
    createId();
  }

  public BeeRadioButton(String name, BeeCommand cmnd) {
    this(name);
    setCommand(cmnd);
  }

  public BeeRadioButton(String name, String label) {
    super(name, label);
    createId();
  }

  public BeeRadioButton(String name, String label, boolean asHTML) {
    super(name, label, asHTML);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "r");
  }

  public BeeCommand getCommand() {
    return command;
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public String getService() {
    return BeeDom.getService(this);
  }

  public void setCommand(BeeCommand command) {
    this.command = command;
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void setService(String svc) {
    BeeDom.setService(this, svc);
  }

}
