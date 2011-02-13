package com.butent.bee.client.canvas;

public class Vector {
  public static Vector mult(Vector v, double c) {
    return new Vector(v.x * c, v.y * c);
  }
  
  public static Vector sub(Vector a, Vector b) {
    return new Vector(a.x - b.x, a.y - b.y);
  }
  
  public double x, y;
  
  public Vector() {
    this(0, 0);
  }
  
  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public Vector(Vector v) {
    this(v.x, v.y);
  }
  
  public void add(double px, double py) {
    this.x += px;
    this.y += py;
  }
  
  public void add(Vector v) {
    add(v.x, v.y);
  }
  
  public double mag() {
    if (x == 0 && y == 0) {
      return 0;
    } else {
      return Math.sqrt(x * x + y * y);
    }
  }
  
  public double magSquared() {
    return x * x + y * y;
  }
  
  public void mult(double c) {
    mult(c, c);
  }
  
  public void mult(double px, double py) {
    this.x *= px;
    this.y *= py;
  }
  
  public void mult(Vector v) {
    mult(v.x, v.y);
  }
  
  public void set(Vector v) {
    x = v.x;
    y = v.y;
  }
  
  public void sub(double px, double py) {
    this.x -= px;
    this.y -= py;
  }
  
  public void sub(Vector v) {
    sub(v.x, v.y);
  }
}
