package com.butent.bee.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.LayoutPanel;

import com.butent.bee.shared.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BeeKeeper {

  private static Screen screen;
  private static RpcFactory rpc;
  private static EventManager bus;

  private static UserInfo user;
  private static Global glob;
  private static Storage stor;
  private static MenuManager menu;

  public static EventManager getBus() {
    return bus;
  }

  public static MenuManager getMenu() {
    return menu;
  }

  public static RpcFactory getRpc() {
    return rpc;
  }

  public static Screen getScreen() {
    return screen;
  }

  public static Storage getStorage() {
    return stor;
  }

  public static UserInfo getUser() {
    return user;
  }

  private Module[] modules;

  BeeKeeper(LayoutPanel root, String url) {
    screen = GWT.create(Screen.class);
    screen.setRootPanel(root);

    bus = new EventManager();
    rpc = new RpcFactory(url);
    user = new UserInfo();
    glob = new Global();
    stor = new Storage();
    menu = new MenuManager();

    modules = new Module[] {screen, bus, rpc, user, glob, stor, menu};
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
