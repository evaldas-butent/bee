package com.butent.bee.shared.html.builder;

import com.google.common.collect.Lists;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;

public abstract class Node {

  private final String tag;
  private final List<Attribute> attributes = Lists.newArrayList();
  private Node parent;

  protected Node() {
    this.tag = NameUtils.getClassName(getClass()).toLowerCase();
  }

  protected Node(String tag) {
    this.tag = tag;
  }

  public String getAttribute(String name) {
    for (Attribute attribute : attributes) {
      if (BeeUtils.same(attribute.getName(), name)) {
        return attribute.getValue();
      }
    }
    return null;
  }

  public Node getParent() {
    return this.parent;
  }

  public String getTag() {
    return tag;
  }

  public boolean removeAttribute(String name) {
    int index = BeeConst.UNDEF;

    for (int i = 0; i < attributes.size(); i++) {
      if (BeeUtils.same(attributes.get(i).getName(), name)) {
        index = i;
        break;
      }
    }

    if (BeeConst.isUndef(index)) {
      return false;
    } else {
      return attributes.remove(index) != null;
    }
  }

  public void setAttribute(String name, String value) {
    if (!BeeUtils.isEmpty(name) && value != null) {
      for (Attribute attribute : attributes) {
        if (BeeUtils.same(attribute.getName(), name)) {
          attribute.setValue(value);
          return;
        }
      }

      attributes.add(new Attribute(name, value));
    }
  }

  public void setParent(Node parent) {
    this.parent = parent;
  }

  @Override
  public String toString() {
    return write();
  }

  public String write() {
    StringBuilder sb = new StringBuilder(writeOpen());
    sb.append(writeClose());
    return sb.toString();
  }

  protected String writeClose() {
    StringBuilder sb = new StringBuilder("</");
    sb.append(tag);
    sb.append(">");
    return sb.toString();
  }

  protected String writeOpen() {
    StringBuilder sb = new StringBuilder("<");
    sb.append(tag);

    for (Attribute attr : attributes) {
      sb.append(attr.write());
    }

    sb.append(">");
    return sb.toString();
  }
}
