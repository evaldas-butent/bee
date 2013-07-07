package com.butent.bee.client.canvas;

/**
 * Ensures visual components in canvas demo to have vector animation in them.
 */

public class SpringObject {
  public static final double SPRING_STRENGTH = 0.1;
  public static final double FRICTION = 0.8;

  private final Vector pos;
  private final Vector vel;
  private Vector goal;

  public SpringObject(Vector start) {
    this.pos = new Vector(start);
    this.vel = new Vector(0, 0);
    this.goal = new Vector(start);
  }

  public Vector getGoal() {
    return goal;
  }

  public Vector getPos() {
    return pos;
  }

  public Vector getVel() {
    return vel;
  }

  public void setGoal(Vector goal) {
    this.goal = goal;
  }

  public void update() {
    Vector d = Vector.sub(goal, pos);
    d.mult(SPRING_STRENGTH);
    vel.add(d);
    vel.mult(FRICTION);
    pos.add(vel);
  }
}
