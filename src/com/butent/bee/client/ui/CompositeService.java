package com.butent.bee.client.ui;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Is an abstract class for menu, grid, form and row set service classes, registers and creates
 * services with comp_ui prefix.
 */

public abstract class CompositeService {

  public static final String PREFIX = "comp_ui_";

  private static Map<String, CompositeService> registeredServices = Maps.newHashMap();
  private static Map<String, CompositeService> pendingServices = Maps.newHashMap();

  static {
    registerService(new FormService());
    registerService(new GridService());
    registerService(new MenuService());
    registerService(new RowSetService());
  }

  public static boolean doService(String svc, String stg, Object... parameters) {
    Assert.notEmpty(svc);
    Assert.notEmpty(stg);

    CompositeService service = getService(svc);
    return service.doStage(stg, parameters);
  }

  public static boolean isRegistered(String svc) {
    String svcId = extractId(svc);

    if (!BeeUtils.isEmpty(svcId)) {
      return pendingServices.containsKey(svcId);
    } else {
      return registeredServices.containsKey(svc);
    }
  }

  public static void registerService(CompositeService service) {
    String svc = service.getName();
    Assert.state(!isRegistered(svc), "Service already registered: " + svc);
    registeredServices.put(svc, service);
  }

  private static CompositeService createService(String svc) {
    Assert.contains(registeredServices, svc);

    String svcId = BeeUtils.createUniqueName("svc");
    CompositeService service = registeredServices.get(svc).create(svcId);

    pendingServices.put(svcId, service);

    return service;
  }

  private static String extractId(String svc) {
    if (svc.indexOf(":") > 0) {
      String[] arr = svc.split(":");
      return arr[arr.length - 1];
    }
    return "";
  }

  private static CompositeService getService(String svc) {
    CompositeService service;
    String svcId = extractId(svc);

    if (BeeUtils.isEmpty(svcId)) {
      service = createService(svc);
    } else {
      Assert.contains(pendingServices, svcId);
      service = pendingServices.get(svcId);
    }
    return service;
  }

  private String serviceId;

  protected CompositeService(String... svcId) {
    serviceId = BeeUtils.concat(":", svcId);
  }

  protected abstract CompositeService create(String svcId);

  protected void destroy() {
    pendingServices.remove(self());
  }

  protected abstract boolean doStage(String stg, Object... params);

  protected abstract String getName();

  protected String self() {
    return serviceId;
  }
}
