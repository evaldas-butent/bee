package com.butent.bee.client.utils;

import com.butent.bee.client.BeeKeeper;

public class ServiceCommand extends BeeCommand {
  
  public ServiceCommand(String service, String parameters) {
    super(service, parameters);
  }

  public ServiceCommand(String service) {
    super(service);
  }

  @Override
  public void execute() {
    BeeKeeper.getBus().dispatchService(getService(), getParameters(), null);
  }
}
