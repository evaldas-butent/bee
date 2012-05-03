package com.butent.bee.client.utils;

import com.google.gwt.core.client.Scheduler;

/**
 * Contains action instructions for later execution, manages it's called service and parameters.
 */

public abstract class BeeCommand implements Scheduler.ScheduledCommand {
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
