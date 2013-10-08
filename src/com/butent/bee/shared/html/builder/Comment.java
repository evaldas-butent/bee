package com.butent.bee.shared.html.builder;


public class Comment extends FertileNode {

  public Comment() {
    super();
  }

  public Comment(String text) {
    this();
    appendText(text);
  }

  @Override
  public String write() {
    StringBuilder sb = new StringBuilder("<!-- >");

    for (Node child : getChildren()) {
      sb.append(child.write());
    }
    sb.append("< -->");

    return sb.toString();
  }
}
