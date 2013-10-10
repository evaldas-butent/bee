package com.butent.bee.shared.html.builder;

public abstract class Node {

  private Node parent;

  protected Node() {
  }

  public Node getParent() {
    return parent;
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return write();
  }

  protected abstract String write();
}
