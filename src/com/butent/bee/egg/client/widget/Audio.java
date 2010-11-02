package com.butent.bee.egg.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.shared.HasId;

public class Audio extends Widget implements HasId {

  public Audio() {
    super();
    
    setElement(DomUtils.createElement(DomUtils.TAG_AUDIO));
    init();
  }

  public void createId() {
    DomUtils.createId(this, "audio");
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    createId();
    setStyleName("bee-Audio");
  }
  
}
