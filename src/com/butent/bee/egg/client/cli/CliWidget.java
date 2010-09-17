package com.butent.bee.egg.client.cli;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.DOM;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.communication.ParameterList;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.client.widget.BeeTextBox;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;

public class CliWidget extends BeeTextBox {

  public CliWidget() {
    super();
  }

  public CliWidget(Element element) {
    super(element);
  }

  @Override
  public boolean onBeeKey(KeyPressEvent event) {
    if (BeeUtils.isEmpty(getValue())) {
      return true;
    }

    String v = getValue().trim();
    String[] arr = BeeUtils.split(v, BeeConst.STRING_SPACE);
    int c = (arr == null) ? 0 : arr.length;

    boolean ok = true;

    if (BeeUtils.same(v, "fields")) {
      BeeGlobal.showFields();
    } else if (BeeUtils.same(v, "clear")) {
      BeeKeeper.getLog().clear();
    } else if (BeeUtils.same(v, "stack")) {
      BeeKeeper.getLog().stack();

    } else if (BeeUtils.same(v, "rpc")) {
      if (BeeKeeper.getRpc().getRpcList().isEmpty()) {
        BeeGlobal.showDialog("RpcList empty");
      } else {
        BeeKeeper.getUi().updateActivePanel(
            BeeGlobal.createSimpleGrid(RpcList.DEFAULT_INFO_COLUMNS,
                BeeKeeper.getRpc().getRpcList().getDefaultInfo()));
      }

    } else if (BeeUtils.same(v, "menu")) {
      BeeKeeper.getMenu().showMenu();

    } else if (BeeUtils.same(v, "gwt")) {
      BeeGlobal.showGrid(v, StringProp.HEADERS, PropUtils.createStringArray(
          "Host Page Base URL", GWT.getHostPageBaseURL(), "Module Base URL",
          GWT.getModuleBaseURL(), "Module Name", GWT.getModuleName(),
          "Permutation Strong Name", GWT.getPermutationStrongName(),
          "Uncaught Exception Handler", GWT.getUncaughtExceptionHandler(),
          "Unique Thread Id", GWT.getUniqueThreadId(), "Version",
          GWT.getVersion(), "Is Client", GWT.isClient(), "Is Prod Mode",
          GWT.isProdMode(), "Is Script", GWT.isScript()));
    
    } else if (BeeUtils.same(v, "?")) {
      BeeKeeper.getLog().info(BeeConst.whereAmI());
      BeeKeeper.getRpc().dispatchService(BeeService.SERVICE_WHERE_AM_I);

    } else if (c > 1) {
      if (BeeUtils.same(arr[0], "eval")) {
        String xpr = v.substring("eval".length()).trim();
        BeeGlobal.showDialog(xpr, BeeJs.evalToString(xpr));

      } else if (BeeUtils.inListSame(arr[0], "p", "prop")) {
        JavaScriptObject obj = BeeJs.eval(arr[1]);
        if (obj == null) {
          BeeGlobal.showError(arr[1], "not a js object");
          return ok;
        }

        String patt = (c > 2) ? arr[2] : null;
        JsArrayString prp = BeeJs.getProperties(obj, patt);

        if (BeeJs.isEmpty(prp)) {
          BeeGlobal.showError(v, "properties not found");
        } else if (BeeUtils.same(arr[0], "p")) {
          BeeGlobal.showGrid(v, new String[]{"property", "type", "value"}, prp);
        } else {
          BeeKeeper.getUi().showGrid(new String[]{"property", "type", "value"},
              prp);
        }

      } else if (BeeUtils.inListSame(arr[0], "f", "func")) {
        JavaScriptObject obj = BeeJs.eval(arr[1]);
        if (obj == null) {
          BeeGlobal.showError(arr[1], "not a js object");
          return ok;
        }

        String patt = (c > 2) ? arr[2] : null;
        JsArrayString fnc = BeeJs.getFunctions(obj, patt);

        if (BeeJs.isEmpty(fnc)) {
          BeeGlobal.showError(v, "functions not found");
        } else if (fnc.length() <= 5) {
          BeeGlobal.showDialog(v, fnc.join());
        } else if (BeeUtils.same(arr[0], "f")) {
          BeeGlobal.showGrid(v, new String[]{"function"}, fnc);
        } else {
          BeeKeeper.getUi().showGrid(new String[]{"function"}, fnc);
        }

      } else if (BeeUtils.same(arr[0], "id")) {
        JavaScriptObject obj = DOM.getElementById(arr[1]);
        if (obj == null) {
          BeeGlobal.showError(arr[1], "element id not found");
          return ok;
        }

        String patt = (c > 2) ? arr[2] : null;
        JsArrayString prp = BeeJs.getProperties(obj, patt);

        if (BeeJs.isEmpty(prp)) {
          BeeGlobal.showError(v, "properties not found");
        } else if (prp.length() <= 20) {
          BeeGlobal.showGrid(v, new String[]{"property", "type", "value"}, prp);
        } else {
          BeeKeeper.getUi().showGrid(new String[]{"property", "type", "value"},
              prp);
        }

      } else if (BeeUtils.inListSame(arr[0], "file", "dir", "get")) {
        ParameterList params = BeeKeeper.getRpc().createParameters(BeeService.SERVICE_GET_RESOURCE);
        params.addPositionalHeader(arr);

        BeeKeeper.getRpc().makeGetRequest(params);
      }

    } else {
      BeeGlobal.showDialog(v, c, arr);
    }

    return ok;
  }

}
