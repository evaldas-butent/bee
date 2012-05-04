package com.butent.bee.shared.modules.transport;

import com.butent.bee.shared.Service;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public class TransportConstants {

  public static enum OrderStatus implements HasCaption {
    CREATED, ACTIVATED, CONFIRMED, CANCELED, COMPLETED;

    public String getCaption() {
      return BeeUtils.proper(this.name(), null);
    }
  }

  public static final String TRANSPORT_MODULE = "Transport";
  public static final String TRANSPORT_METHOD = TRANSPORT_MODULE + "Method";

  public static final String SVC_UPDATE_KM = "UpdateKilometers";
  public static final String SVC_GET_PROFIT = "GetProfit";

  public static final String VAR_TRIP_ID = Service.RPC_VAR_PREFIX + "trip_id";
  public static final String COL_STATUS = "Status";

  private TransportConstants() {
  }
}
