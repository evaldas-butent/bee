package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.RadioButton;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a mutually-exclusive selection radio button user interface component.
 */

public class BeeRadioButton extends RadioButton implements HasId {
  
  public BeeRadioButton(String name, String label) {
    super(name, label);
    init();
  }

  public BeeRadioButton(String name, String label, boolean asHTML) {
    super(name, label, asHTML);
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "rb";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName("bee-RadioButton");
  }
}
