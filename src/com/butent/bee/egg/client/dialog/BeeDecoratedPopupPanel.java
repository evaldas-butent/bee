package com.butent.bee.egg.client.dialog;

import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.shared.HasId;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;

public class BeeDecoratedPopupPanel extends DecoratedPopupPanel implements
    HasId {

  public BeeDecoratedPopupPanel() {
    super();
    createId();
  }

  public BeeDecoratedPopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    createId();
  }

  public BeeDecoratedPopupPanel(boolean autoHide) {
    super(autoHide);
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
