package com.butent.bee.egg.client.ui;

public abstract class CompositeService {

  public static String extractService(String svc) {
    if (svc.indexOf(":") > 0) {
      String[] arr = svc.split(":");
      return arr[0];
    }
    return svc;
  }

  public static String extractServiceId(String svc) {
    if (svc.indexOf(":") > 0) {
      String[] arr = svc.split(":");
      return arr[1];
    }
    return "";
  }

  protected String serviceId;

  public CompositeService() {
  }

  public CompositeService(String serviceId) {
    this.serviceId = serviceId;
  }

  public abstract CompositeService createInstance(String serviceId);

  public abstract boolean doService(Object... params);

  protected String appendId(String svc) {
    return svc + ":" + serviceId;
  }
}
