package com.butent.bee.client.widget;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.shared.html.Tags;

/**
 * Handles a component for using scalable vector graphics.
 */
public class Svg extends Widget implements IdentifiableWidget {

  public Svg() {
    super();

    setElement(DomUtils.createSvg(Tags.SVG));
    init();
  }

  @Override
  public String getId() {
    return DomUtils.getId(this);
  }

  @Override
  public String getIdPrefix() {
    return "svg";
  }

  @Override
  public void setId(String id) {
    DomUtils.setId(this, id);
  }

  private void init() {
    DomUtils.createId(this, getIdPrefix());
  }
}
