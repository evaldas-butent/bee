package com.butent.bee.shared.html.builder;

import com.google.common.collect.Lists;

import com.butent.bee.shared.Assert;

import java.util.List;

public class FertileElement extends Element {

  private final List<Node> children = Lists.newArrayList();

  protected FertileElement() {
    super();
  }

  protected FertileElement(String tag) {
    super(tag);
  }

  public void appendChild(Node child) {
    Assert.notNull(child);
    Assert.isTrue(this != child, "cannot append a node to itself");

    child.setParent(this);
    children.add(child);
  }

  public void appendChildren(List<Node> nodes) {
    if (nodes != null) {
      for (Node node : nodes) {
        appendChild(node);
      }
    }
  }

  public void appendChildren(Node... nodes) {
    if (nodes != null) {
      for (Node node : nodes) {
        appendChild(node);
      }
    }
  }

  public void appendText(String text) {
    appendChild(new Text(text));
  }

  public void clearChildren() {
    children.clear();
  }

  public Node getChild(int index) {
    return children.get(index);
  }

  public List<Node> getChildren() {
    return children;
  }

  public void insertChild(int index, Node child) {
    Assert.betweenInclusive(index, 0, children.size());
    Assert.notNull(child);
    Assert.isTrue(this != child, "cannot append a node to itself");

    child.setParent(this);
    children.add(index, child);
  }

  public void removeChild(Node child) {
    children.remove(child);
  }

  @Override
  public String toString() {
    return write();
  }

  @Override
  public String write() {
    StringBuilder sb = new StringBuilder(writeOpen());
    for (Node child : children) {
      sb.append(child.write());
    }
    sb.append(writeClose());

    return sb.toString();
  }
}
