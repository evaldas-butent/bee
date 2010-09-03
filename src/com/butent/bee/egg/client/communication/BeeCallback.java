package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.ui.GwtUiCreator;
import com.butent.bee.egg.client.ui.GwtUiLoader;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.BeeStage;
import com.butent.bee.egg.shared.BeeType;
import com.butent.bee.egg.shared.BeeWidget;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Panel;

public class BeeCallback implements RequestCallback {

  @Override
  public void onError(Request req, Throwable ex) {
    if (ex instanceof RequestTimeoutException)
      BeeKeeper.getLog().log("request timeout", ex);
    else
      BeeKeeper.getLog().log("request failure", ex);
  }

  @Override
  public void onResponseReceived(Request req, Response resp) {
    if (resp.getStatusCode() == Response.SC_OK) {
      BeeDuration dur = new BeeDuration("response get");

      String txt = resp.getText();
      int len = txt.length();

      String hSep = resp.getHeader(BeeService.RPC_FIELD_SEP);
      String sep;

      if (BeeUtils.isHexString(hSep)) {
        sep = new String(BeeUtils.fromHex(hSep));
      } else {
        sep = Character.toString(BeeService.DEFAULT_INFORMATION_SEPARATOR);
      }

      boolean hasSep = txt.indexOf(sep) > 0;

      int cnt = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_CNT));
      int cc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_COLS));
      int mc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_MSG_CNT));

      boolean debug = BeeGlobal.isDebug();

      BeeKeeper.getLog().finish(dur,
          BeeUtils.addName(BeeService.RPC_FIELD_COLS, cc),
          BeeUtils.addName(BeeService.RPC_FIELD_MSG_CNT, mc),
          BeeUtils.addName(BeeService.RPC_FIELD_CNT, cnt),
          BeeUtils.addName("len", len),
          hasSep ? BeeConst.STRING_EMPTY : BeeUtils.addName("text", txt));

      if (debug) {
        BeeKeeper.getLog().log("response headers", resp.getHeadersAsString());
      }

      if (mc > 0) {
        for (int i = 0; i < mc; i++) {
          BeeKeeper.getLog().log(resp.getHeader(BeeService.rpcMessageName(i)));
        }
      }

      if (hasSep) {
        if (debug) {
          dur.restart("split");
        }

        JsArrayString arr = BeeJs.split(txt, sep);
        if (cnt > 0 && arr.length() > cnt) {
          arr.setLength(cnt);
        }

        if (debug) {
          BeeKeeper.getLog().finish(dur,
              BeeUtils.addName("arr size", arr.length()));
        }

        String svc = null;
        int id = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_QID));
        RpcInfo rpc = BeeKeeper.getRpc().getRpcInfo(id);
        if (!BeeUtils.isEmpty(rpc)) {
          svc = rpc.getName();
        }

        if ("rpc_ui_form_list".equals(svc)) {
          String[] lst = new String[arr.length() - cc];
          for (int i = cc; i < arr.length(); i++) {
            lst[i - 1] = arr.get(i);
          }
          BeeGlobal.createField("form_name", "Form name", BeeType.TYPE_STRING,
              lst[0], BeeWidget.LIST, lst);

          BeeGlobal.inputFields(new BeeStage("comp_ui_form",
              BeeStage.STAGE_CONFIRM), "Load form", "form_name");
        }

        else if ("rpc_ui_form".equals(svc) && !debug) {
          UiComponent c = new GwtUiLoader().getFormContent("testForm", cc, arr);
          BeeKeeper.getUi().updateActivePanel(
              (Panel) c.createInstance(new GwtUiCreator()));
        }

        else if (cc > 0) {
          BeeKeeper.getUi().updateActivePanel(BeeGlobal.createGrid(cc, arr));
        } else {
          for (int i = 0; i < arr.length(); i++) {
            if (!BeeUtils.isEmpty(arr.get(i))) {
              BeeKeeper.getLog().log(arr.get(i));
            }
          }
        }
      }

      BeeKeeper.getLog().addSeparator();

    } else {
      BeeKeeper.getLog().log("response status", resp.getStatusCode(),
          resp.getStatusText());
    }
  }

}
