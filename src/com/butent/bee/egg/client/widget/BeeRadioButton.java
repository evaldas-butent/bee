package com.butent.bee.egg.client.widget;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.butent.bee.egg.shared.HasService;

import com.google.gwt.user.client.ui.RadioButton;

public class BeeRadioButton extends RadioButton implements HasId, HasService {

  public BeeRadioButton(String name, String label, boolean asHTML) {
    super(name, label, asHTML);
    createId();
  }

  public BeeRadioButton(String name, String label) {
    super(name, label);
    createId();
  }

  public BeeRadioButton(String name) {
    super(name);
    createId();
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

  public void createId() {
    BeeDom.createId(this, "r");
  }

  public String getService() {
    return BeeDom.getService(this);
  }

  public void setService(String svc) {
    BeeDom.setService(this, svc);
  }

}
