package com.butent.bee.client.modules.service;

import com.butent.bee.client.data.Data;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.service.ServiceConstants;

class ServiceObjectWrapper {
  
  private static final String idColumn = Data.getIdColumn(ServiceConstants.VIEW_SERVICE_OBJECTS);
  
  private final Long id;
  
  private final String categoryName;
  private final String address;

  ServiceObjectWrapper(SimpleRow row) {
    this.id = row.getLong(idColumn);
    this.categoryName = row.getValue(ServiceConstants.ALS_SERVICE_CATEGORY_NAME);
    this.address = row.getValue(ServiceConstants.COL_SERVICE_OBJECT_ADDRESS);
  }

  Long getId() {
    return id;
  }

  String getCategoryName() {
    return categoryName;
  }

  String getAddress() {
    return address;
  }
}
