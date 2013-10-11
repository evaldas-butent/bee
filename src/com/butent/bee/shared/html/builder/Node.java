package com.butent.bee.shared.html.builder;

public abstract class Node {

  private Node parent;

  protected Node() {
  }

  public int getLevel() {
    return (getParent() == null) ? 0 : getParent().getLevel() + 1;
  }
  
  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return build();
  }

  protected abstract String build();
}
