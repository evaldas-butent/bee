package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.ui.GwtUiCreator;
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
    String msg = (ex instanceof RequestTimeoutException) ? "request timeout"
        : "request failure";
    BeeKeeper.getLog().severe(msg, ex);
  }

  @Override
  public void onResponseReceived(Request req, Response resp) {
    BeeDuration dur = new BeeDuration("response get");

    int statusCode = resp.getStatusCode();
    boolean debug = BeeGlobal.isDebug();

    int id = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_QID));
    RpcInfo info = BeeKeeper.getRpc().getRpcInfo(id);
    String svc = (info == null) ? BeeConst.STRING_EMPTY : info.getName();

    String msg;

    if (info == null) {
      BeeKeeper.getLog().warning("Rpc info not available");
    }
    if (BeeUtils.isEmpty(svc)) {
      BeeKeeper.getLog().warning("Rpc service",
          BeeUtils.bracket(BeeService.RPC_FIELD_QNM), "not available");
    }

    if (statusCode != Response.SC_OK) {
      msg = BeeUtils.concat(1, BeeUtils.addName(BeeService.RPC_FIELD_QID, id),
          BeeUtils.addName(BeeService.RPC_FIELD_QNM, svc));
      if (!BeeUtils.isEmpty(msg)) {
        BeeKeeper.getLog().severe(msg);
      }

      msg = BeeUtils.concat(1, BeeUtils.bracket(statusCode),
          resp.getStatusText());
      BeeKeeper.getLog().severe("response status", msg);

      if (info != null) {
        info.endError(msg);
      }
      return;
    }

    String txt = resp.getText();
    int len = txt.length();

    int cnt = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_CNT));
    int cc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_COLS));
    int mc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_MSG_CNT));

    BeeKeeper.getLog().finish(dur,
        BeeUtils.addName(BeeService.RPC_FIELD_QID, id),
        BeeUtils.addName(BeeService.RPC_FIELD_QNM, svc));

    if (debug) {
      BeeKeeper.getLog().info(BeeUtils.addName(BeeService.RPC_FIELD_COLS, cc),
          BeeUtils.addName(BeeService.RPC_FIELD_MSG_CNT, mc),
          BeeUtils.addName(BeeService.RPC_FIELD_CNT, cnt),
          BeeUtils.addName("len", len));
    }

    String hSep = resp.getHeader(BeeService.RPC_FIELD_SEP);
    String sep;

    if (BeeUtils.isHexString(hSep)) {
      sep = new String(BeeUtils.fromHex(hSep));
      BeeKeeper.getLog().warning("response separator", BeeUtils.bracket(hSep));
    } else {
      sep = Character.toString(BeeService.DEFAULT_INFORMATION_SEPARATOR);
      if (!BeeUtils.isEmpty(hSep)) {
        BeeKeeper.getLog().severe("wrong response separator",
            BeeUtils.bracket(hSep));
      }
    }

    if (debug) {
      BeeKeeper.getLog().info("response headers", resp.getHeadersAsString());

      if (info != null) {
        info.setRespInfo(RpcUtil.responseInfo(resp,
            (cc > 0) ? BeeConst.STRING_EMPTY : txt));
      }
    }

    if (mc > 0) {
      dispatchMessages(mc, resp);
    }

    if (len == 0) {
      if (mc == 0) {
        msg = "reponse empty";
        BeeKeeper.getLog().warning(msg);
        if (info != null) {
          info.endMessage(msg);
        }
      } else if (info != null) {
        info.endMessage("messages", mc);
      }

      return;
    }

    if (txt.indexOf(sep) < 0) {
      BeeKeeper.getLog().info("response text", txt);
      if (info != null) {
        info.endMessage(txt, len);
      }

      return;
    }

    if (info != null) {
      info.endResult(cnt, cc, len);
    }

    if (debug) {
      dur.restart("split");
    }

    JsArrayString arr = BeeJs.split(txt, sep);
    if (cnt > 0 && arr.length() > cnt) {
      arr.setLength(cnt);
    }

    if (debug) {
      BeeKeeper.getLog()
          .finish(dur, BeeUtils.addName("arr size", arr.length()));
    }

    dispatchResponse(svc, cc, arr, debug);

    BeeKeeper.getLog().addSeparator();
  }

  private void dispatchResponse(String svc, int cc, JsArrayString arr,
      boolean debug) {
    if ("rpc_ui_form_list".equals(svc)) {
      String[] lst = new String[arr.length() - cc];
      for (int i = cc; i < arr.length(); i++) {
        lst[i - 1] = arr.get(i);
      }
      BeeGlobal.createField("form_name", "Form name", BeeType.TYPE_STRING,
          lst[0], BeeWidget.LIST, lst);

      BeeGlobal.inputFields(
          new BeeStage("comp_ui_form", BeeStage.STAGE_CONFIRM), "Load form",
          "form_name");
    }

    else if ("rpc_ui_form".equals(svc) && !debug) {
      UiComponent c = UiComponent.restore(arr.get(0));
      BeeKeeper.getUi().updateActivePanel(
          (Panel) c.createInstance(new GwtUiCreator()));
    }

    else if (BeeService.equals(svc, BeeService.SERVICE_GET_MENU)) {
      BeeKeeper.getMenu().loadCallBack(arr);
    }

    else if (cc > 0) {
      BeeKeeper.getUi().updateActivePanel(BeeGlobal.createGrid(cc, arr));
    } else {
      for (int i = 0; i < arr.length(); i++) {
        if (!BeeUtils.isEmpty(arr.get(i))) {
          BeeKeeper.getLog().info(arr.get(i));
        }
      }
    }

  }

  private void dispatchMessages(int mc, Response resp) {
    for (int i = 0; i < mc; i++) {
      BeeKeeper.getLog().info(resp.getHeader(BeeService.rpcMessageName(i)));
    }
  }

}
