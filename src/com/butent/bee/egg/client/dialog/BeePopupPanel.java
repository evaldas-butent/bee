package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.PopupPanel;

import com.butent.bee.egg.client.dom.DomUtils;
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
    DomUtils.createId(this, "popup");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
