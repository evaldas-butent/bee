package com.butent.bee.egg.client.menu;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.utils.BeeCommand;

public class MenuCommand extends BeeCommand {
  private String service = null;
  private String parameters = null;

  public MenuCommand() {
    super();
  }

  public MenuCommand(String service) {
    this();
    this.service = service;
  }

  public MenuCommand(String service, String parameters) {
    this();
    this.service = service;
    this.parameters = parameters;
  }

  @Override
  public void execute() {
    BeeGlobal.showDialog(getService(), getParameters());
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

}
