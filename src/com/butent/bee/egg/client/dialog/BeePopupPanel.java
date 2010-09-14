package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.PopupPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeePopupPanel extends PopupPanel implements HasId {

  public BeePopupPanel() {
    super(true);
    createId();
  }

  public BeePopupPanel(boolean autoHide) {
    super(autoHide);
    createId();
  }

  public BeePopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "popup");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
