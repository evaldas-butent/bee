package com.butent.bee.egg.client.layout;

import com.google.gwt.layout.client.Layout.Layer;

public class BeeLayoutData {
  public BeeDirection direction;
  public double oldSize, size;
  public double originalSize;
  public boolean hidden;
  public Layer layer;

  public BeeLayoutData(BeeDirection direction, double size, Layer layer) {
    this.direction = direction;
    this.size = size;
    this.layer = layer;
  }

}
