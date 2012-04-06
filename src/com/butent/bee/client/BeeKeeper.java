package com.butent.bee.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.demo.CalendarPanel;
import com.butent.bee.client.modules.commons.CommonEventHandler;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.CompositeService;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.PasswordService;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.RowComparator;

import java.util.Arrays;

/**
 * starts and stops core system modules.
 * <code>Screen, RpcFactory, EventManager, LogHandler, UserInfo, Global, Storage, MenuManager
 */

public class BeeKeeper {

  private static Screen SCREEN;
  private static RpcFactory RPC;
  private static EventManager BUS;

  private static LogHandler LOG;
  private static UserInfo USER;
  private static Global GLOB;
  private static Storage STOR;
  private static MenuManager MENU;

  public static EventManager getBus() {
    return BUS;
  }

  public static LogHandler getLog() {
    return LOG;
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

  public BeeKeeper(LayoutPanel root, String url) {
    SCREEN = GWT.create(Screen.class);
    SCREEN.setRootPanel(root);

    BUS = new EventManager();
    RPC = new RpcFactory(url);
    LOG = new LogHandler();
    USER = new UserInfo();
    GLOB = new Global();
    STOR = new Storage();
    MENU = new MenuManager();

    modules = new Module[] {SCREEN, BUS, RPC, LOG, USER, GLOB, STOR, MENU};
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

  public void register() {
    FormFactory.registerFormCallback("User", new AbstractFormCallback() {
      @Override
      public void afterCreateWidget(String name, final Widget widget) {
        if (BeeUtils.same(name, "ChangePassword") && widget instanceof HasClickHandlers) {
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              CompositeService.doService(new PasswordService().name(),
                  PasswordService.STG_GET_PASS, UiHelper.getForm(widget));
            }
          });
        }
      }
    });

    TransportHandler.register();
    CommonEventHandler.register();

    getMenu().registerMenuCallback("calendar", new MenuManager.MenuCallback() {
      public void onSelection(String parameters) {
        getScreen().updateActivePanel(new CalendarPanel(14, false));
      }
    });
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
