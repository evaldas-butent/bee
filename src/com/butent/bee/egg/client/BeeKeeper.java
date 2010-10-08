package com.butent.bee.egg.client;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.egg.shared.utils.RowComparator;

import java.util.Arrays;

public class BeeKeeper {
  private static BeeUi UI;
  private static BeeRpc RPC;
  private static BeeBus BUS;

  private static BeeLog LOG;
  private static BeeStyle STYLE;
  private static BeeScheduler SCHED;
  private static BeeUser USER;
  private static BeeGlobal GLOB;
  private static BeeProperties PROP;
  private static BeeMenu MENU;

  public static BeeBus getBus() {
    return BUS;
  }

  public static BeeLog getLog() {
    return LOG;
  }

  public static BeeMenu getMenu() {
    return MENU;
  }

  public static BeeProperties getProp() {
    return PROP;
  }

  public static BeeRpc getRpc() {
    return RPC;
  }

  public static BeeStyle getStyle() {
    return STYLE;
  }

  public static BeeUi getUi() {
    return UI;
  }

  private BeeModule[] modules;

  public BeeKeeper(HasWidgets root, HandlerManager bus, String url) {
    UI = new BeeUi(root);
    BUS = new BeeBus(bus);
    RPC = new BeeRpc(url);

    LOG = new BeeLog();
    STYLE = new BeeStyle();
    SCHED = new BeeScheduler();
    USER = new BeeUser();
    GLOB = new BeeGlobal();
    PROP = new BeeProperties();
    MENU = new BeeMenu();

    modules = new BeeModule[]{
        UI, BUS, RPC, LOG, STYLE, SCHED, USER, GLOB, PROP, MENU};
  }

  public void end() {
    BeeModule arr[] = orderModules(BeeModule.PRIORITY_END);
    if (arr == null) {
      return;
    }

    for (BeeModule mdl : arr) {
      mdl.end();
    }
  }

  public void init() {
    BeeModule arr[] = orderModules(BeeModule.PRIORITY_INIT);
    if (arr == null) {
      return;
    }

    for (BeeModule mdl : arr) {
      mdl.init();
    }
  }

  public void start() {
    BeeModule arr[] = orderModules(BeeModule.PRIORITY_START);
    if (arr == null) {
      return;
    }

    for (BeeModule mdl : arr) {
      mdl.start();
    }
  }

  private BeeModule[] orderModules(int p) {
    int c = modules.length;
    if (c <= 0) {
      return null;
    }

    int r = 0;
    int z;
    Object[][] arr = new Object[c][2];

    for (int i = 0; i < c; i++) {
      arr[i][0] = modules[i];

      z = modules[i].getPriority(p);
      arr[i][1] = z;

      if (z != BeeModule.DO_NOT_CALL) {
        r++;
      }
    }

    if (r <= 0) {
      return null;
    }

    Arrays.sort(arr, new RowComparator(1));

    BeeModule[] ord = new BeeModule[r];
    r = 0;

    for (int i = 0; i < c; i++) {
      if ((Integer) arr[i][1] != BeeModule.DO_NOT_CALL) {
        ord[r] = (BeeModule) arr[i][0];
        r++;
      }
    }

    return ord;
  }

}
