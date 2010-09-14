package com.butent.bee.egg.client.utils;

import com.google.gwt.user.client.Command;

public abstract class BeeCommand implements Command {
  private String service = null;
  private String parameters = null;

  public BeeCommand() {
    super();
  }

  public BeeCommand(String service) {
    this();
    this.service = service;
  }

  public BeeCommand(String service, String parameters) {
    this();
    this.service = service;
    this.parameters = parameters;
  }

  public abstract void execute();

  public String getParameters() {
    return parameters;
  }

  public String getService() {
    return service;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public void setService(String service) {
    this.service = service;
  }

}
