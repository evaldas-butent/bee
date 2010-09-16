package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.DialogBox;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class BeeDialogBox extends DialogBox implements HasId {

  public BeeDialogBox() {
    super();
    createId();
  }

  public BeeDialogBox(boolean autoHide) {
    super(autoHide);
    createId();
  }

  public BeeDialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public void createId() {
    DomUtils.createId(this, "dialog");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
