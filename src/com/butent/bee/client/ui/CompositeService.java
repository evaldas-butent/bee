package com.butent.bee.client.ui;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Stage;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Map;

/**
 * Is an abstract class for composite service handling.
 */

public abstract class CompositeService {

  private static Map<String, CompositeService> registeredServices = Maps.newHashMap();
  private static Map<String, CompositeService> pendingServices = Maps.newHashMap();

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

  private static CompositeService createService(String svc) {
    Assert.contains(registeredServices, svc);

    CompositeService service = registeredServices.get(svc).getInstance();
    Assert.notNull(service);
    service.serviceId = svc + "_" + NameUtils.createUniqueName();
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

  private String serviceId;

  protected CompositeService() {
    String svc = name();

    if (!registeredServices.containsKey(svc)) {
      registeredServices.put(svc, this);
    }
  }

  public String name() {
    return Service.COMPOSITE_SERVICE_PREFIX + NameUtils.getClassName(this.getClass());
  }

  protected void destroy() {
    pendingServices.remove(getId());
  }

  protected abstract boolean doStage(String stg, Object... params);

  protected String getId() {
    return serviceId;
  }

  protected abstract CompositeService getInstance();

  protected Stage getStage(String stg) {
    Assert.notEmpty(stg);
    return new Stage(getId(), stg);
  }
}
