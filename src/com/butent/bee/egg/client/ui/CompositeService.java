package com.butent.bee.egg.client.ui;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

public abstract class CompositeService {

  public static CompositeService createService(String service, String serviceId) {
    CompositeService svc = null;

    if (BeeUtils.same(service, "comp_ui_form")) {
      svc = new FormService(serviceId);
    } else if (BeeUtils.same(service, "comp_ui_grid")) {
      svc = new GridService(serviceId);
    } else if (BeeUtils.same(service, "comp_ui_menu")) {
      svc = new MenuService(serviceId);
    } else {
      BeeGlobal.showError("Unknown composite service", svc);
    }

    return svc;
  }

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

  public CompositeService(String serviceId) {
    Assert.notEmpty(serviceId);

    this.serviceId = serviceId;
  }

  public abstract boolean doService(Object... params);

  protected String appendId(String svc) {
    return svc + ":" + serviceId;
  }
}
