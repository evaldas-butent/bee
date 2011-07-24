package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Implements a panel, which positions all of its children absolutely, allowing them to overlap.
 */

public class Absolute extends AbsolutePanel implements HasId {

  public Absolute() {
    super();
    init();
  }

  public Absolute(Element elem) {
    super(elem);
    init();
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

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "absolute";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
  
  private void init() {
    DomUtils.createId(this, getIdPrefix());
    getElement().getStyle().setPosition(Position.ABSOLUTE);
    getElement().getStyle().setOverflow(Overflow.AUTO);
  }
}
