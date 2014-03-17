package com.butent.bee.client.modules.service;

import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;

public final class ServiceKeeper {
  
  public static void register() {
    GridFactory.registerGridInterceptor("ServiceObjects", new ServiceObjectsGrid());

    FormFactory.registerFormInterceptor("ServiceObject", new ServiceObjectForm());
  }

  private ServiceKeeper() {
  }
}
