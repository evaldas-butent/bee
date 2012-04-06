package com.butent.bee.client.dialog;

import com.google.gwt.user.client.ui.PopupPanel;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.Stacking;
import com.butent.bee.shared.HasId;

/**
 * Implements a informational message delivering pop up window user interface component.
 */

public class Popup extends PopupPanel implements HasId {
  
  public Popup() {
    super(true);
    init();
  }

  public Popup(boolean autoHide) {
    super(autoHide);
    init();
  }

  public Popup(boolean autoHide, boolean modal) {
    super(autoHide, modal);
    init();
  }

  public void enableGlass() {
    setGlassStyleName(getDefaultStyleName() + "-glass");
    setGlassEnabled(true);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "popup";
  }

  @Override
  public void hide(boolean autoClosed) {
    if (isShowing()) {
      Stacking.removeContext(this);
    }
    super.hide(autoClosed);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  @Override
  public void show() {
    if (!isShowing()) {
      Stacking.addContext(this);
    }
    super.show();
  }
  
  protected String getDefaultStyleName() {
    return "bee-Popup";
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
    setStyleName(getDefaultStyleName());
  }
}
