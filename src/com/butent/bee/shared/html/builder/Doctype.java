package com.butent.bee.shared.html.builder;

public class Doctype extends Node {

  public Doctype() {
    super();
  }

  @Override
  public String build() {
    return "<!doctype html>";
  }
}
