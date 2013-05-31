package com.butent.bee.client.visualization.showcase;

import com.butent.bee.client.visualization.events.OnMouseOverHandler;
import com.butent.bee.client.widget.Label;

/**
 * Handles a situation when mouse cursor moves over a particular area in visualization demos.
 */

public class OnMouseOverDemo extends OnMouseOverHandler {

  private final Label label;

  OnMouseOverDemo(Label label) {
    this.label = label;
  }

  @Override
  public void onMouseOverEvent(OnMouseOverEvent event) {
    int row = event.getRow();
    int column = event.getColumn();
    StringBuffer b = new StringBuffer();
    b.append(" row: ");
    b.append(row);
    b.append(", column: ");
    b.append(column);
    label.setText("Mouse over " + b.toString());
  }
}
