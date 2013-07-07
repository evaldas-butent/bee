package com.butent.bee.client.canvas;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;

/**
 * Implements ball visual component.
 */

public class Ball extends SpringObject {
  private CssColor color;
  private double posZ;
  private double velZ;
  private double goalZ;
  private double radius;
  private Vector startPos;
  private double startRadius;

  public Ball(double x, double y, double z, double radius, int r, int g, int b) {
    this(new Vector(x, y), z, radius, CssColor.make(r, g, b));
  }

  public Ball(Vector start, double startPosZ, double radius, CssColor color) {
    super(start);
    this.color = color;
    this.posZ = startPosZ;
    this.velZ = 0;
    this.goalZ = startPosZ;
    this.radius = radius;
    this.startPos = new Vector(start);
    this.startRadius = radius;
  }

  public void draw(Context2d context) {
    context.setFillStyle(color);
    context.beginPath();
    context.arc(getPos().getX(), getPos().getY(), radius, 0, Math.PI * 2.0, true);
    context.closePath();
    context.fill();
  }

  public Vector getStartPos() {
    return startPos;
  }

  @Override
  public void update() {
    super.update();

    Vector dh = Vector.sub(startPos, getPos());
    double dist = dh.mag();
    goalZ = dist / 100.0 + 1.0;
    double dgZ = goalZ - posZ;
    double aZ = dgZ * SPRING_STRENGTH;
    velZ += aZ;
    velZ *= FRICTION;
    posZ += velZ;

    radius = startRadius * posZ;
    radius = radius < 1 ? 1 : radius;
  }
}
