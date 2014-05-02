package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.grid.interceptor.FileGridInterceptor;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.rights.Module;

public final class ServiceKeeper {
  
  public static ParameterList createArgs(String method) {
    ParameterList args = BeeKeeper.getRpc().createParameters(Module.SERVICE.getName());
    args.addQueryItem(AdministrationConstants.METHOD, method);
    return args;
  }
  
  public static void register() {
    GridFactory.registerGridInterceptor(VIEW_OBJECTS, new ServiceObjectsGrid());

    GridFactory.registerGridInterceptor(VIEW_OBJECT_FILES,
        new FileGridInterceptor(COL_SERVICE_OBJECT, AdministrationConstants.COL_FILE,
            AdministrationConstants.COL_FILE_CAPTION, AdministrationConstants.ALS_FILE_NAME));
    
    GridFactory.registerGridInterceptor(VIEW_MAINTENANCE, new MaintenanceGrid());
    GridFactory.registerGridInterceptor("ObjectInvoices", new ObjectInvoicesGrid());

    FormFactory.registerFormInterceptor("ServiceObject", new ServiceObjectForm());
    FormFactory.registerFormInterceptor("ServiceInvoice", new ServiceInvoiceForm());
  }

  private ServiceKeeper() {
  }
}
