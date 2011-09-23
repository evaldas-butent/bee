package com.butent.bee.client.ui;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * Is an abstract class for composite service handling.
 */

public abstract class CompositeService {

  private static Map<String, CompositeService> registeredServices = Maps.newHashMap();
  private static Map<String, CompositeService> pendingServices = Maps.newHashMap();

  static {
    registerService(new FormService());
    registerService(new GridService());
    registerService(new MenuService());
    registerService(new DsnService());
    registerService(new StateService());
  }

  public static boolean doService(String svc, String stg, Object... parameters) {
    Assert.notEmpty(svc);
    Assert.notEmpty(stg);

    CompositeService service = getService(svc);
    return service.doStage(stg, parameters);
  }

  public static boolean isRegistered(String svc) {
    if (!registeredServices.containsKey(svc)) {
      return pendingServices.containsKey(svc);
    }
    return true;
  }

  public static <T extends CompositeService> String name(Class<T> clazz) {
    Assert.notNull(clazz);
    Assert.state(!clazz.equals(CompositeService.class));
    return Service.COMPOSITE_SERVICE_PREFIX + BeeUtils.getClassName(clazz);
  }

  private static CompositeService createService(String svc) {
    Assert.contains(registeredServices, svc);

    CompositeService service = registeredServices.get(svc).getInstance();
    Assert.notNull(service);
    service.serviceId = svc + "_" + BeeUtils.createUniqueName();
    pendingServices.put(service.getId(), service);
    return service;
  }

  private static CompositeService getService(String svc) {
    CompositeService service;

    if (pendingServices.containsKey(svc)) {
      service = pendingServices.get(svc);
    } else {
      service = createService(svc);
    }
    return service;
  }

  private static void registerService(CompositeService service) {
    String svc = name(service.getClass());
    Assert.state(!isRegistered(svc), "Service already registered: " + svc);
    registeredServices.put(svc, service);
  }

  private String serviceId;

  protected abstract CompositeService getInstance();

  protected void destroy() {
    pendingServices.remove(getId());
  }

  protected abstract boolean doStage(String stg, Object... params);

  protected String getId() {
    return serviceId;
  }

  protected Stage getStage(String stg) {
    Assert.notEmpty(stg);
    return new Stage(getId(), stg);
  }
}
