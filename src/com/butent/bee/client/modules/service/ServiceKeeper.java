package com.butent.bee.client.modules.service;

import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.service.ServiceConstants;

public final class ServiceKeeper {
  
  public static void register() {
    GridFactory.registerGridInterceptor(ServiceConstants.VIEW_OBJECTS, new ServiceObjectsGrid());

    GridFactory.registerGridInterceptor(ServiceConstants.VIEW_OBJECT_FILES,
        new FileGridInterceptor(ServiceConstants.COL_SERVICE_OBJECT,
            AdministrationConstants.COL_FILE, "Caption",
            AdministrationConstants.ALS_FILE_NAME));
    
    GridFactory.registerGridInterceptor(ServiceConstants.VIEW_MAINTENANCE, new MaintenanceGrid());

    FormFactory.registerFormInterceptor("ServiceObject", new ServiceObjectForm());
  }

  private ServiceKeeper() {
  }
}
