package com.butent.bee.egg.client.dialog;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.DialogBox;

public class BeeDialogBox extends DialogBox implements HasId {

  public BeeDialogBox() {
    super();
    createId();
  }

  public BeeDialogBox(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public BeeDialogBox(boolean autoHide) {
    super(autoHide);
    createId();
  }

  private void createId() {
    BeeDom.setId(this);
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
