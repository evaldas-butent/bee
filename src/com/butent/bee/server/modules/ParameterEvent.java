package com.butent.bee.server.modules;


public class ParameterEvent {

  private final String parameter;

  public ParameterEvent(String parameter) {
    this.parameter = parameter;
  }

  public String getParameter() {
    return parameter;
  }
}
