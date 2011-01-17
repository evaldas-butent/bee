package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.visualization.events.OnMouseOutHandler;

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
    label.setText("Mouse out of " + b.toString()); 
  }
}
