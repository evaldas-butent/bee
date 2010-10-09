package com.butent.bee.egg.client.cli;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ParameterList;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.data.JsData;
import com.butent.bee.egg.client.dom.DomUtils;
import com.butent.bee.egg.client.layout.BeeDirection;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.client.layout.TilePanel;
import com.butent.bee.egg.client.tree.BeeTree;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.client.utils.JreEmulation;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.data.DataUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class CliWorker {

  public static void clearLog() {
    BeeKeeper.getLog().clear();
  }

  public static void digest(String v) {
    String src = null;
    int p = v.indexOf(BeeConst.CHAR_SPACE);

    if (p > 0) {
      String z = v.substring(p + 1).trim();

      if (BeeUtils.isDigit(z)) {
        int x = BeeUtils.toInt(z);
        if (BeeUtils.betweenInclusive(x, 1, BeeUtils.exp10(6))) {
          src = BeeUtils.randomString(x, x, BeeConst.CHAR_SPACE, '\u0800');
        } else {
          src = z;
        }
      } else if (z.length() > 0) {
        src = z;
      }
    }

    if (src == null) {
      src = BeeUtils.randomString(10, 20, BeeConst.CHAR_SPACE, '\u0400');
    }

    if (src.length() > 100) {
      BeeKeeper.getLog().info("Source length", src.length());
    } else {
      BeeKeeper.getLog().info(src);
    }

    BeeKeeper.getLog().info("js", BeeJs.md5(src));
    BeeKeeper.getLog().info(BeeConst.CLIENT, Codec.md5(src));

    BeeKeeper.getRpc().makePostRequest(BeeService.SERVICE_GET_DIGEST,
        BeeService.DATA_TYPE.TEXT, src);
  }

  public static void doLog(String arr[]) {
    if (BeeUtils.length(arr) > 1) {
      String z = arr[1];
      
      if (BeeUtils.inList(z, BeeConst.STRING_ZERO, BeeConst.STRING_MINUS)) {
        BeeKeeper.getLog().hide();
      } else if (BeeUtils.isDigit(z)) {
        BeeKeeper.getLog().resize(BeeUtils.toInt(z));
      } else if (BeeUtils.startsSame(z, "clear")) {
        BeeKeeper.getLog().clear();
      } else {
        BeeKeeper.getLog().show();
        BeeKeeper.getLog().info((Object[]) arr);
      }
      
      return;
    }

    Level[] levels = new Level[] { Level.FINEST, Level.FINER, Level.FINE, Level.CONFIG,
        Level.INFO, Level.WARNING, Level.SEVERE };
    for (Level lvl : levels) {
      BeeKeeper.getLog().log(lvl, lvl.getName().toLowerCase());
    }
  }
  
  public static void doMenu(String[] arr) {
    if (BeeUtils.length(arr) > 1) {
      ParameterList params = BeeKeeper.getRpc().createParameters(
          BeeService.SERVICE_GET_MENU);
      params.addPositionalHeader(arr[1]);
      BeeKeeper.getRpc().makeGetRequest(params);
    } else {
      BeeKeeper.getMenu().showMenu();
    }
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

    BeeDirection dir = DomUtils.getDirection(p1);
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

  public static void getCharsets() {
    ParameterList params = BeeKeeper.getRpc().createParameters(
        BeeService.SERVICE_GET_RESOURCE);
    params.addPositionalHeader("cs");

    BeeKeeper.getRpc().makeGetRequest(params);
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

  public static void showDateFormat() {
    int r = DateTimeFormat.PredefinedFormat.values().length;
    String[][] data = new String[r][2];

    int i = 0;
    for (DateTimeFormat.PredefinedFormat dtf : DateTimeFormat.PredefinedFormat.values()) {
      data[i][0] = dtf.toString();
      data[i][1] = DateTimeFormat.getFormat(dtf).format(new Date());
      i++;
    }

    BeeKeeper.getUi().showGrid(data, "Format", "Value");
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
      return;
    }

    JsData view = (JsData) DataUtils.createView(prp, "property", "type",
        "value");
    view.sort(0, true);

    if (view.getRowCount() <= 20) {
      BeeGlobal.modalGrid(v, view);
    } else {
      BeeKeeper.getUi().showGrid(view);
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
      return;
    }
    if (fnc.length() <= 5) {
      BeeGlobal.showDialog(v, fnc.join());
      return;
    }

    JsData view = (JsData) DataUtils.createView(fnc, "function");
    view.sort(0, true);

    if (BeeUtils.same(arr[0], "f") && view.getRowCount() <= 20) {
      BeeGlobal.modalGrid(v, view);
    } else {
      BeeKeeper.getUi().showGrid(view);
    }
  }

  public static void showGwt() {
    BeeGlobal.modalGrid("GWT", PropUtils.createStringProp("Host Page Base URL",
        GWT.getHostPageBaseURL(), "Module Base URL", GWT.getModuleBaseURL(),
        "Module Name", GWT.getModuleName(), "Permutation Strong Name",
        GWT.getPermutationStrongName(), "Uncaught Exception Handler",
        GWT.getUncaughtExceptionHandler(), "Unique Thread Id",
        GWT.getUniqueThreadId(), "Version", GWT.getVersion(), "Is Client",
        GWT.isClient(), "Is Prod Mode", GWT.isProdMode(), "Is Script",
        GWT.isScript()));
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
      return;
    }

    JsData view = (JsData) DataUtils.createView(prp, "property", "type",
        "value");
    view.sort(0, true);

    if (BeeUtils.same(arr[0], "p") && view.getRowCount() <= 20) {
      BeeGlobal.modalGrid(v, view);
    } else {
      BeeKeeper.getUi().showGrid(view);
    }
  }

  public static void showRpc() {
    if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
      BeeGlobal.showDialog("RpcList empty");
    } else {
      BeeKeeper.getUi().showGrid(
          BeeKeeper.getRpc().getRpcList().getDefaultInfo(),
          RpcList.DEFAULT_INFO_COLUMNS);
    }
  }

  public static void showStack() {
    BeeKeeper.getLog().stack();
  }
  
  public static void showTiles() {
    Widget tiles = BeeKeeper.getUi().getScreenPanel().getCenter();
    if (!(tiles instanceof TilePanel)) {
      BeeGlobal.showDialog("no tiles vailable");
    }
    
    BeeTree tree = new BeeTree();
    tree.addItem(((TilePanel) tiles).getTree(null, true));
    
    BeeGlobal.inform(tree);
  }

  public static void style(String v, String arr[]) {
    if (BeeUtils.length(arr) < 2) {
      NodeList<Element> nodes = Document.get().getElementsByTagName("style");
      if (nodes == null || nodes.getLength() <= 0) {
        BeeGlobal.showDialog("styles not available");
        return;
      }
      int stCnt = nodes.getLength();

      List<SubProp> lst = new ArrayList<SubProp>();
      PropUtils.addSub(lst, "Styles", "count", stCnt);

      for (int i = 0; i < stCnt; i++) {
        String ref = "$doc.getElementsByTagName('style').item(" + i
            + ").sheet.rules";
        int len = BeeJs.evalToInt(ref + ".length");
        PropUtils.addSub(lst, "Style " + BeeUtils.progress(i + 1, stCnt),
            "rules", len);

        for (int j = 0; j < len; j++) {
          JavaScriptObject obj = BeeJs.eval(ref + "[" + j + "]");
          if (obj == null) {
            PropUtils.addSub(lst, "Rule", BeeUtils.progress(j + 1, len),
                "not available");
            break;
          }

          JsArrayString prp = BeeJs.getProperties(obj, null);
          for (int k = 0; k < prp.length() - 2; k += 3) {
            PropUtils.addSub(
                lst,
                BeeUtils.concat(1, "Rule", BeeUtils.progress(j + 1, len),
                    prp.get(k * 3)), prp.get(k * 3 + 1), prp.get(k * 3 + 2));
          }
        }
      }

      BeeKeeper.getUi().showGrid(lst);
      return;
    }

    if (!v.contains("{")) {
      Element elem = Document.get().getElementById(arr[1]);
      if (elem == null) {
        BeeGlobal.showDialog("element id", arr[1], "not found");
        return;
      }

      List<StringProp> lst = DomUtils.getStyleInfo(elem.getStyle());
      if (BeeUtils.isEmpty(lst)) {
        BeeGlobal.showDialog("element id", arr[1], "has no style");
      } else {
        BeeKeeper.getUi().showGrid(lst);
      }
      return;
    }

    boolean start = false;
    boolean end = false;
    boolean immediate = false;

    StringBuilder sb = new StringBuilder();
    boolean tg = false;

    for (int i = 1; i < arr.length; i++) {
      if (!tg) {
        if (arr[i].contains("{")) {
          tg = true;
        } else {
          if (BeeUtils.same(arr[i], "start")) {
            start = true;
            continue;
          }
          if (BeeUtils.same(arr[i], "end")) {
            end = true;
            continue;
          }
          if (arr[i].equals("+")) {
            immediate = true;
            continue;
          }
        }
      }
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(arr[i]);
    }

    String st = sb.toString();
    if (!st.contains("{") || !st.contains("}")) {
      BeeGlobal.showError("Nah pop no style, a strictly roots", v, st);
      return;
    }

    BeeKeeper.getLog().info(st);

    if (start) {
      StyleInjector.injectAtStart(st, immediate);
    } else if (end) {
      StyleInjector.injectAtEnd(st, immediate);
    } else {
      StyleInjector.inject(st, immediate);
    }
  }

  public static void whereAmI() {
    BeeKeeper.getLog().info(BeeConst.whereAmI());
    BeeKeeper.getRpc().dispatchService(BeeService.SERVICE_WHERE_AM_I);
  }
}
