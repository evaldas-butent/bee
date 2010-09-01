package com.butent.bee.egg.shared;

public class BeeName implements Transformable {
  private String name;

  public BeeName() {
    super();
  }

  public BeeName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String transform() {
    return toString();
  }

  public String toString() {
    return name == null ? "" : name;
  }

}
