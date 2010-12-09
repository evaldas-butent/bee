package com.butent.bee.egg.client;

import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.egg.shared.utils.RowComparator;

import java.util.Arrays;

public class BeeKeeper {
  private static BeeUi UI;
  private static RpcFactory RPC;
  private static EventManager BUS;

  private static BeeLog LOG;
  private static BeeStyle STYLE;
  private static BeeScheduler SCHED;
  private static BeeUser USER;
  private static Global GLOB;
  private static Storage STOR;
  private static MenuManager MENU;

  public static EventManager getBus() {
    return BUS;
  }

  public static BeeLog getLog() {
    return LOG;
  }

  public static MenuManager getMenu() {
    return MENU;
  }

  public static RpcFactory getRpc() {
    return RPC;
  }

  public static Storage getStorage() {
    return STOR;
  }

  public static BeeStyle getStyle() {
    return STYLE;
  }

  public static BeeUi getUi() {
    return UI;
  }

  private Module[] modules;

  public BeeKeeper(HasWidgets root, String url) {
    UI = new BeeUi(root);
    BUS = new EventManager();
    RPC = new RpcFactory(url);

    LOG = new BeeLog();
    STYLE = new BeeStyle();
    SCHED = new BeeScheduler();
    USER = new BeeUser();
    GLOB = new Global();
    STOR = new Storage();
    MENU = new MenuManager();

    modules = new Module[]{
        UI, BUS, RPC, LOG, STYLE, SCHED, USER, GLOB, STOR, MENU};
  }

  public void end() {
    Module[] arr = orderModules(Module.PRIORITY_END);
    if (arr == null) {
      return;
    }

    for (Module mdl : arr) {
      mdl.end();
    }
  }

  public void init() {
    Module[] arr = orderModules(Module.PRIORITY_INIT);
    if (arr == null) {
      return;
    }

    for (Module mdl : arr) {
      mdl.init();
    }
  }

  public void start() {
    Module[] arr = orderModules(Module.PRIORITY_START);
    if (arr == null) {
      return;
    }

    for (Module mdl : arr) {
      mdl.start();
    }
  }

  private Module[] orderModules(int p) {
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

      if (z != Module.DO_NOT_CALL) {
        r++;
      }
    }

    if (r <= 0) {
      return null;
    }

    Arrays.sort(arr, new RowComparator(1));

    Module[] ord = new Module[r];
    r = 0;

    for (int i = 0; i < c; i++) {
      if ((Integer) arr[i][1] != Module.DO_NOT_CALL) {
        ord[r] = (Module) arr[i][0];
        r++;
      }
    }

    return ord;
  }

}
