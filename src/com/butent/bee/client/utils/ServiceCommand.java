package com.butent.bee.client.utils;

import com.butent.bee.client.BeeKeeper;

public class ServiceCommand extends Command {
  
  public ServiceCommand(String service) {
    super(service);
  }

  @Override
  public void execute() {
    BeeKeeper.getBus().dispatchService(getService(), null);
  }
}
