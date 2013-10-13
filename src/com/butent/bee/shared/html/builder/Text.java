package com.butent.bee.shared.html.builder;

public class Text extends Node {

  private final String text;

  public Text(String text) {
    super();
    this.text = text;
  }

  @Override
  public String build(int indentStart, int indentStep) {
    return Node.indent(indentStart, text);
  }

  public String getText() {
    return text;
  }
}
