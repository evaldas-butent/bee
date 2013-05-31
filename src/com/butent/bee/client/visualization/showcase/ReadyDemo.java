package com.butent.bee.client.visualization.showcase;

import com.butent.bee.client.visualization.events.ReadyHandler;
import com.butent.bee.client.widget.Label;

/**
 * Creates a label with text "ready" when ready event occurs.
 */

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
