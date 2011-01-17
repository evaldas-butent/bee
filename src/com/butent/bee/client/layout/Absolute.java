package com.butent.bee.client.layout;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

public class Absolute extends AbsolutePanel implements HasId {
  public Absolute() {
    createId();
  }

  public Absolute(Element elem) {
    super(elem);
    createId();
  }
  
  public String append(Widget w, int left, int top) {
    super.add(w, left, top);
    return DomUtils.getId(w);
  }

  public String append(Widget w, int left, int top, int width) {
    super.add(w, left, top);
    DomUtils.setWidth(w, width);
    return DomUtils.getId(w);
  }

  public void createId() {
    DomUtils.createId(this, "absolute");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
