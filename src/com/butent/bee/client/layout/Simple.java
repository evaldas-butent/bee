package com.butent.bee.client.layout;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Contains a class for panels that contain only one widget.
 */

public class Simple extends SimplePanel implements HasId {

  public Simple() {
    super();
    DomUtils.createId(this, getIdPrefix());
  }
  
  public Simple(Element elem) {
    super(elem);
  }

  public Simple(Widget child) {
    super(child);
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "simple";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }
}
