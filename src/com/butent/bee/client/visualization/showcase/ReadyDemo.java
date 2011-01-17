package com.butent.bee.client.visualization.showcase;

import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.visualization.events.ReadyHandler;

public class ReadyDemo extends ReadyHandler {
  private final Label label;

  public ReadyDemo(Label label) {
    this.label = label;
  }

  @Override
  public void onReady(ReadyEvent event) {
    label.setText("ready"); 
  }
}
