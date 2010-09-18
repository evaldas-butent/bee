package com.butent.bee.egg.client.cli;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockLayoutPanel;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ParameterList;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.client.utils.JreEmulation;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;

public class Worker {

  public static void clearLog() {
    BeeKeeper.getLog().clear();
  }

  public static void doScreen(String arr[]) {
    BeeSplit screen = BeeKeeper.getUi().getScreenPanel();
    Assert.notNull(screen);
    
    String p1 = BeeUtils.arrayGetQuietly(arr, 0);
    String p2 = BeeUtils.arrayGetQuietly(arr, 1);
    
    if (BeeUtils.same(p1, "screen")) {
      BeeKeeper.getUi().showGrid(screen.getInfo());
      return;
    }
    
    DockLayoutPanel.Direction dir = DomUtils.getDirection(p1);
    if (dir == null) {
      BeeGlobal.sayHuh(p1, p2);
      return;
    }
    
    if (BeeUtils.isEmpty(p2)) {
      BeeKeeper.getUi().showGrid(screen.getDirectionInfo(dir));
      return;
    }
    
    double size = BeeUtils.toDouble(p2);
    if (Double.isNaN(size)) {
      BeeGlobal.showError(p1, p2, "NaN");
      return;
    }
    
    screen.setDirectionSize(dir, size);
  }

  public static void eval(String v, String[] arr) {
    String xpr = v.substring(arr[0].length()).trim();

    if (BeeUtils.isEmpty(xpr)) {
      BeeGlobal.sayHuh(v);
    } else {
      BeeGlobal.showDialog(xpr, BeeJs.evalToString(xpr));
    }
  }

  public static void getFs() {
    ParameterList params = BeeKeeper.getRpc().createParameters(
        BeeService.SERVICE_GET_RESOURCE);
    params.addPositionalHeader("fs");

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  public static void getResource(String arr[]) {
    if (BeeUtils.length(arr) < 2) {
      BeeGlobal.sayHuh(BeeUtils.transform(arr));
      return;
    }

    ParameterList params = BeeKeeper.getRpc().createParameters(
        BeeService.SERVICE_GET_RESOURCE);
    params.addPositionalHeader(arr);

    BeeKeeper.getRpc().makeGetRequest(params);
  }

  public static void showElement(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
      BeeGlobal.sayHuh(v);
      return;
    }

    JavaScriptObject obj = DOM.getElementById(arr[1]);
    if (obj == null) {
      BeeGlobal.showError(arr[1], "element id not found");
      return;
    }

    String patt = BeeUtils.arrayGetQuietly(arr, 2);
    JsArrayString prp = BeeJs.getProperties(obj, patt);

    if (BeeJs.isEmpty(prp)) {
      BeeGlobal.showError(v, "properties not found");
    } else if (prp.length() <= 20) {
      BeeGlobal.modalGrid(v, prp, "property", "type", "value");
    } else {
      BeeKeeper.getUi().showGrid(prp, "property", "type", "value");
    }
  }

  public static void showFields(String[] arr) {
    if (BeeUtils.length(arr) > 1) {
      BeeGlobal.showFields(JreEmulation.copyOfRange(arr, 1, arr.length));
    } else {
      BeeGlobal.showFields();
    }
  }

  public static void showFunctions(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
      BeeGlobal.sayHuh(v);
      return;
    }

    JavaScriptObject obj = BeeJs.eval(arr[1]);
    if (obj == null) {
      BeeGlobal.showError(arr[1], "not a js object");
      return;
    }

    String patt = BeeUtils.arrayGetQuietly(arr, 2);
    JsArrayString fnc = BeeJs.getFunctions(obj, patt);

    if (BeeJs.isEmpty(fnc)) {
      BeeGlobal.showError(v, "functions not found");
    } else if (fnc.length() <= 5) {
      BeeGlobal.showDialog(v, fnc.join());
    } else if (BeeUtils.same(arr[0], "f") && fnc.length() < 30) {
      BeeGlobal.modalGrid(v, fnc, "function");
    } else {
      BeeKeeper.getUi().showGrid(fnc, "function");
    }
  }

  public static void showGwt() {
    BeeGlobal.modalGrid("GWT", PropUtils.createStringProp(
        "Host Page Base URL", GWT.getHostPageBaseURL(), "Module Base URL",
        GWT.getModuleBaseURL(), "Module Name", GWT.getModuleName(),
        "Permutation Strong Name", GWT.getPermutationStrongName(),
        "Uncaught Exception Handler", GWT.getUncaughtExceptionHandler(),
        "Unique Thread Id", GWT.getUniqueThreadId(), "Version",
        GWT.getVersion(), "Is Client", GWT.isClient(), "Is Prod Mode",
        GWT.isProdMode(), "Is Script", GWT.isScript()));
  }

  public static void showMenu() {
    BeeKeeper.getMenu().showMenu();
  }

  public static void showProperties(String v, String[] arr) {
    if (BeeUtils.length(arr) < 2) {
      BeeGlobal.sayHuh(v);
      return;
    }

    JavaScriptObject obj = BeeJs.eval(arr[1]);
    if (obj == null) {
      BeeGlobal.showError(arr[1], "not a js object");
      return;
    }

    String patt = BeeUtils.arrayGetQuietly(arr, 2);
    JsArrayString prp = BeeJs.getProperties(obj, patt);

    if (BeeJs.isEmpty(prp)) {
      BeeGlobal.showError(v, "properties not found");
    } else if (BeeUtils.same(arr[0], "p") && prp.length() < 30) {
      BeeGlobal.modalGrid(v, prp, "property", "type", "value");
    } else {
      BeeKeeper.getUi().showGrid(prp, "property", "type", "value");
    }
  }

  public static void showRpc() {
    if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
      BeeGlobal.showDialog("RpcList empty");
    } else {
      BeeKeeper.getUi().updateActivePanel(
          BeeGlobal.simpleGrid(BeeKeeper.getRpc().getRpcList().getDefaultInfo(),
          RpcList.DEFAULT_INFO_COLUMNS));
    }
  }

  public static void showStack() {
    BeeKeeper.getLog().stack();
  }
  
  public static void whereAmI() {
    BeeKeeper.getLog().info(BeeConst.whereAmI());
    BeeKeeper.getRpc().dispatchService(BeeService.SERVICE_WHERE_AM_I);
  }
}
