package com.butent.bee.client.utils;

import com.google.gwt.core.client.Scheduler;

import com.butent.bee.shared.HasService;

/**
 * Contains action instructions for later execution, manages it's called service and parameters.
 */

public abstract class Command implements Scheduler.ScheduledCommand, HasService {

  private String service = null;
  private String parameters = null;

  public Command() {
    super();
  }

  public Command(String service) {
    this();
    this.service = service;
  }

  public Command(String service, String parameters) {
    this();
    this.service = service;
    this.parameters = parameters;
  }

  public String getParameters() {
    return parameters;
  }

  @Override
  public String getService() {
    return service;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  @Override
  public void setService(String service) {
    this.service = service;
  }
}
