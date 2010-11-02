package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class Svg extends Widget implements HasId {

  public Svg() {
    super();
    
    setElement(DomUtils.createElement(DomUtils.TAG_SVG));
    init();
  }

  public void createId() {
    DomUtils.createId(this, "svg");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
  }
  
}
