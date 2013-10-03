package com.butent.bee.client.visualization.showcase;

import com.butent.bee.client.visualization.events.OnMouseOutHandler;
import com.butent.bee.client.widget.Label;

/**
 * Handles a situation when mouse cursor leaves a particular area in visualization demos.
 */

public class OnMouseOutDemo extends OnMouseOutHandler {

  private final Label label;

  OnMouseOutDemo(Label label) {
    this.label = label;
  }

  @Override
  public void onMouseOutEvent(OnMouseOutEvent event) {
    StringBuffer b = new StringBuffer();
    b.append(" row: ");
    b.append(event.getRow());
    b.append(", column: ");
    b.append(event.getColumn());
    label.setHtml("Mouse out of " + b.toString());
  }
}
