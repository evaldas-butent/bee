package com.butent.bee.egg.client.ui;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class CompositeService {

  private static Map<String, CompositeService> registeredServices;
  private static Map<String, CompositeService> pendingServices;

  static {
    registeredServices = new HashMap<String, CompositeService>();
    pendingServices = new HashMap<String, CompositeService>();

    registeredServices.put("comp_ui_form", new FormService());
    registeredServices.put("comp_ui_grid", new GridService());
    registeredServices.put("comp_ui_menu", new MenuService());
  }

  public static boolean doService(String svc, Object... parameters) {
    Assert.notEmpty(svc);

    CompositeService service = getService(svc);
    return service.doStage(parameters);
  }

  public static boolean isRegistered(String svc) {
    String serviceId = extractId(svc);

    if (!BeeUtils.isEmpty(serviceId)) {
      return pendingServices.containsKey(serviceId);
    } else {
      String service = normalizeService(svc);
      return !BeeUtils.isEmpty(service)
          && registeredServices.containsKey(service);
    }
  }

  public static String normalizeService(String svc) {
    if (!BeeUtils.isEmpty(svc)) {
      if (svc.indexOf(":") > 0) {
        String[] arr = svc.split(":");
        return arr[0];
      }
    }
    return svc;
  }

  private static CompositeService createService(String svc) {
    Assert.contains(registeredServices, svc);

    String serviceId = BeeUtils.createUniqueName("svc");
    CompositeService service = registeredServices.get(svc).create(serviceId);

    pendingServices.put(serviceId, service);

    return service;
  }

  private static String extractId(String svc) {
    if (svc.indexOf(":") > 0) {
      String[] arr = svc.split(":", 2);
      return arr[1];
    }
    return "";
  }

  private static CompositeService getService(String svc) {
    CompositeService service;
    String svcId = extractId(svc);

    if (BeeUtils.isEmpty(svcId)) {
      service = createService(normalizeService(svc));
    } else {
      Assert.contains(pendingServices, svcId);
      service = pendingServices.get(svcId);
    }
    return service;
  }

  private String serviceId;

  protected CompositeService() {
  }

  protected CompositeService(String serviceId) {
    Assert.notEmpty(serviceId);

    this.serviceId = serviceId;
  }

  protected String adoptService(String svc) {
    return svc + ":" + serviceId;
  }

  protected abstract CompositeService create(String svcId);

  protected abstract boolean doStage(Object... params);

  protected void unregister() {
    pendingServices.remove(serviceId);
  }
}
