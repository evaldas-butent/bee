package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.LayoutPanel;

import com.butent.bee.shared.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * starts and stops core system modules.
 * <code>Screen, RpcFactory, EventManager, UserInfo, Global, Storage, MenuManager
 */

public class BeeKeeper {

  private static Screen SCREEN;
  private static RpcFactory RPC;
  private static EventManager BUS;

  private static UserInfo USER;
  private static Global GLOB;
  private static Storage STOR;
  private static MenuManager MENU;

  public static EventManager getBus() {
    return BUS;
  }

  public static MenuManager getMenu() {
    return MENU;
  }

  public static RpcFactory getRpc() {
    return RPC;
  }

  public static Screen getScreen() {
    return SCREEN;
  }

  public static Storage getStorage() {
    return STOR;
  }

  public static UserInfo getUser() {
    return USER;
  }

  private Module[] modules;

  BeeKeeper(LayoutPanel root, String url) {
    SCREEN = GWT.create(Screen.class);
    SCREEN.setRootPanel(root);

    BUS = new EventManager();
    RPC = new RpcFactory(url);
    USER = new UserInfo();
    GLOB = new Global();
    STOR = new Storage();
    MENU = new MenuManager();

    modules = new Module[] {SCREEN, BUS, RPC, USER, GLOB, STOR, MENU};
  }

  void exit() {
    List<Module> list = orderModules(Module.PRIORITY_END);
    for (Module mdl : list) {
      mdl.onExit();
    }
  }

  void init() {
    List<Module> list = orderModules(Module.PRIORITY_INIT);
    for (Module mdl : list) {
      mdl.init();
    }
  }

  void start() {
    List<Module> list = orderModules(Module.PRIORITY_START);
    for (Module mdl : list) {
      mdl.start();
    }
  }

  private List<Module> orderModules(int p) {
    List<Module> result = Lists.newArrayList();

    List<Pair<Module, Integer>> temp = Lists.newArrayList();

    for (Module mdl : modules) {
      int z = mdl.getPriority(p);
      if (z != Module.DO_NOT_CALL) {
        temp.add(Pair.of(mdl, z));
      }
    }

    if (temp.isEmpty()) {
      return result;
    }
    if (temp.size() == 1) {
      result.add(temp.get(0).getA());
      return result;
    }
    
    Collections.sort(temp, new Comparator<Pair<Module, Integer>>() {
      @Override
      public int compare(Pair<Module, Integer> o1, Pair<Module, Integer> o2) {
        return o1.getB().compareTo(o2.getB());
      }
    });
    
    for (Pair<Module, Integer> pair : temp) {
      result.add(pair.getA());
    }
    return result;
  }
}
