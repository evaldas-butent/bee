package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.shared.HasId;

/**
 * Handles a component for using scalable vector graphics.
 */
public class Svg extends Widget implements HasId {

  public Svg() {
    super();

    setElement(DomUtils.createSvg(DomUtils.TAG_SVG));
    init();
  }

  public String getId() {
    return DomUtils.getId(this);
  }

  public String getIdPrefix() {
    return "svg";
  }

  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
