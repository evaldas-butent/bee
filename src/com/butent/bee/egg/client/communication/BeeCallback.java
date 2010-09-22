package com.butent.bee.egg.client.communication;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.data.ResponseData;
import com.butent.bee.egg.client.ui.CompositeService;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeCallback implements RequestCallback {

  @Override
  public void onError(Request req, Throwable ex) {
    String msg = (ex instanceof RequestTimeoutException) ? "request timeout"
        : "request failure";
    BeeKeeper.getLog().severe(msg, ex);
  }

  @Override
  public void onResponseReceived(Request req, Response resp) {
    BeeDuration dur = new BeeDuration("response");

    int statusCode = resp.getStatusCode();
    boolean debug = BeeGlobal.isDebug();

    int id = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_QID));
    RpcInfo info = BeeKeeper.getRpc().getRpcInfo(id);
    String svc = (info == null) ? BeeConst.STRING_EMPTY : info.getService();

    String msg;

    if (info == null) {
      BeeKeeper.getLog().warning("Rpc info not available");
    }
    if (BeeUtils.isEmpty(svc)) {
      BeeKeeper.getLog().warning("Rpc service",
          BeeUtils.bracket(BeeService.RPC_FIELD_SVC), "not available");
    }

    if (statusCode != Response.SC_OK) {
      msg = BeeUtils.concat(1, BeeUtils.addName(BeeService.RPC_FIELD_QID, id),
          BeeUtils.addName(BeeService.RPC_FIELD_SVC, svc));
      if (!BeeUtils.isEmpty(msg)) {
        BeeKeeper.getLog().severe(msg);
      }

      msg = BeeUtils.concat(1, BeeUtils.bracket(statusCode),
          resp.getStatusText());
      BeeKeeper.getLog().severe("response status", msg);

      if (info != null) {
        info.endError(msg);
      }
      finalizeResponse();
      return;
    }

    String txt = resp.getText();
    int len = txt.length();

    BeeService.DATA_TYPE dtp = BeeService.getDataType(resp.getHeader(BeeService.RPC_FIELD_DTP));
    String uri = resp.getHeader(BeeService.RPC_FIELD_URI);

    int cnt = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_CNT));
    int cc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_COLS));
    int mc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_MSG_CNT));

    if (debug) {
      BeeKeeper.getLog().finish(dur,
          BeeUtils.addName(BeeService.RPC_FIELD_QID, id),
          BeeUtils.addName(BeeService.RPC_FIELD_SVC, svc));

      BeeKeeper.getLog().info(BeeUtils.addName(BeeService.RPC_FIELD_DTP, dtp),
          BeeUtils.addName(BeeService.RPC_FIELD_URI, uri),
          BeeUtils.addName("len", len),
          BeeUtils.addName(BeeService.RPC_FIELD_COLS, cc),
          BeeUtils.addName(BeeService.RPC_FIELD_MSG_CNT, mc),
          BeeUtils.addName(BeeService.RPC_FIELD_CNT, cnt));
    } else {
      BeeKeeper.getLog().info("response", id, svc, dtp, cc, cnt, len, mc);
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
      Header[] headers = resp.getHeaders();
      for (int i = 0; i < headers.length; i++) {
        BeeKeeper.getLog().info("Header", i + 1, headers[i].getName(),
            headers[i].getValue());
      }

      if (info != null) {
        info.setRespInfo(RpcUtils.responseInfo(resp));
      }
    }

    String[] messages = null;

    if (mc > 0) {
      messages = new String[mc];
      for (int i = 0; i < mc; i++) {
        messages[i] = resp.getHeader(BeeService.rpcMessageName(i));
      }

      dispatchMessages(mc, messages);
    }

    if (info != null) {
      info.end(dtp, txt, cnt, cc, len, mc, messages);
    }

    if (len == 0) {
      if (mc == 0) {
        msg = "response empty";
        BeeKeeper.getLog().warning(msg);
      }
      finalizeResponse();
      return;
    }

    if (txt.indexOf(sep) < 0 && !BeeService.isResource(dtp)) {
      BeeKeeper.getLog().info("text", txt);
      finalizeResponse();
      return;
    }

    JsArrayString arr = BeeJs.split(txt, sep);
    if (cnt > 0 && arr.length() > cnt) {
      arr.setLength(cnt);
    }

    String serviceId = CompositeService.extractServiceId(svc);

    if (!BeeUtils.isEmpty(serviceId) && !debug) {
      CompositeService service = BeeGlobal.getService(serviceId);
      service.doService(arr, cc);
    } else {
      dispatchResponse(svc, dtp, uri, cc, arr);
    }

    BeeKeeper.getLog().finish(dur);
    finalizeResponse();
  }

  private void dispatchMessages(int mc, String[] messages) {
    for (int i = 0; i < mc; i++) {
      BeeKeeper.getLog().info(messages[i]);
    }
  }

  private void dispatchResponse(String svc, BeeService.DATA_TYPE dtp, String uri, int cc,
      JsArrayString arr) {
    if (BeeService.equals(svc, BeeService.SERVICE_GET_MENU)) {
      BeeKeeper.getMenu().loadCallBack(arr);

    } else if (cc > 0) {
      BeeView view = new ResponseData(arr, cc);
      BeeKeeper.getUi().showGrid(view);

    } else if (BeeService.isResource(dtp)) {
      BeeKeeper.getUi().showResource(new BeeResource(uri, arr.get(0), dtp));

    } else {
      for (int i = 0; i < arr.length(); i++) {
        if (!BeeUtils.isEmpty(arr.get(i))) {
          BeeKeeper.getLog().info(arr.get(i));
        }
      }

      if (BeeService.equals(svc, BeeService.SERVICE_WHERE_AM_I)) {
        BeeKeeper.getLog().info(BeeConst.whereAmI());
      }
    }
  }

  private void finalizeResponse() {
    BeeKeeper.getLog().addSeparator();
  }

}
