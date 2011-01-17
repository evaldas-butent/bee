package com.butent.bee.client.visualization;

public class LegendPosition {
  public static final LegendPosition BOTTOM = new LegendPosition("bottom");
  public static final LegendPosition LEFT = new LegendPosition("left");
  public static final LegendPosition NONE = new LegendPosition("none");
  public static final LegendPosition RIGHT = new LegendPosition("right");
  public static final LegendPosition TOP = new LegendPosition("top");

  private final String name;

  protected LegendPosition(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}