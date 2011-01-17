package com.butent.bee.client.layout;

import com.google.gwt.layout.client.Layout.Layer;

public class LayoutData {
  public Direction direction;
  public double oldSize, size;
  public double originalSize;
  public boolean hidden;
  public Layer layer;

  public LayoutData(Direction direction, double size, Layer layer) {
    this.direction = direction;
    this.size = size;
    this.layer = layer;
  }

}
