package com.butent.bee.egg.client.layout;

import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.event.HasAfterAddHandler;
import com.butent.bee.egg.client.event.HasBeforeAddHandler;
import com.butent.bee.egg.shared.HasId;

public class BeeLayoutPanel extends LayoutPanel implements HasId {

  public BeeLayoutPanel() {
    createId();
  }

  public BeeLayoutPanel(Widget widget) {
    this();
    add(widget);
  }
  
  @Override
  public void add(Widget widget) {
    Widget w = widget;
    if (w instanceof HasBeforeAddHandler) {
      w = ((HasBeforeAddHandler) w).onBeforeAdd(this);
    }

    super.add(w);
    
    if (w instanceof HasAfterAddHandler) {
      ((HasAfterAddHandler) w).onAfterAdd(this);
    }
  }

  public void createId() {
    DomUtils.createId(this, "layout");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

}
