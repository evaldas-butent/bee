package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;

public class BeeDecoratedPopupPanel extends DecoratedPopupPanel implements
    HasId {

  public BeeDecoratedPopupPanel() {
    super();
    createId();
  }

  public BeeDecoratedPopupPanel(boolean autoHide) {
    super(autoHide);
    createId();
  }

  public BeeDecoratedPopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public void createId() {
    BeeDom.createId(this, "decor");
  }

  public String getId() {
    return BeeDom.getId(this);
  }

  public void setId(String id) {
    BeeDom.setId(this, id);
  }

}
