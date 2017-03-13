package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.modules.orders.OrderItemsPicker;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;

class ServiceItemsPicker extends OrderItemsPicker {

  @Override
  public Long getWarehouseFrom(IsRow row) {
    int warehouseIdx = Data
        .getColumnIndex(TBL_SERVICE_MAINTENANCE, ClassifierConstants.COL_WAREHOUSE);
    if (row == null || BeeConst.isUndef(warehouseIdx)) {
      return null;
    }

    return row.getLong(warehouseIdx);
  }

  @Override
  public boolean setIsOrder(IsRow row) {
    return true;
  }
}