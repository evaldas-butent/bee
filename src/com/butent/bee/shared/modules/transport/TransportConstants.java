package com.butent.bee.shared.modules.transport;

public class TransportConstants {

  public static enum OrderStatus {
    CREATED, ACTIVATED, CONFIRMED, CANCELED, COMPLETED,
  }

  public static final String TRANSPORT_MODULE = "Transport";
  public static final String TRANSPORT_METHOD = TRANSPORT_MODULE + "Method";

  public static final String COL_STATUS = "Status";

  private TransportConstants() {
  }
}
