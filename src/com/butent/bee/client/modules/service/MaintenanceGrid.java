package com.butent.bee.client.modules.service;

import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;

public class MaintenanceGrid extends AbstractGridInterceptor {

  MaintenanceGrid() {
  }
  
  @Override
  public GridInterceptor getInstance() {
    return new MaintenanceGrid();
  }
}
