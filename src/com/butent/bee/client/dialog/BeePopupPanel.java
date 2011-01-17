package com.butent.bee.client.dialog;

import com.google.gwt.user.client.ui.PopupPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class BeePopupPanel extends PopupPanel implements HasId {
  public BeePopupPanel() {
    super(true);
    init();
  }

  public BeePopupPanel(boolean autoHide) {
    super(autoHide);
    init();
  }

  public BeePopupPanel(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    init();
  }

  public void createId() {
    DomUtils.createId(this, "popup");
  }
  
  public void enableGlass() {
    setGlassStyleName("bee-PopupGlass");
    setGlassEnabled(true);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    createId();
    setStyleName("bee-PopupPanel");
  }
}
