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
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.communication.CommUtils;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.communication.ResponseMessage;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.util.logging.Level;

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

    ContentType ctp = CommUtils.getContentType(resp.getHeader(BeeService.RPC_FIELD_CTP));

    String txt = CommUtils.getContent(ctp, resp.getText());
    int len = txt.length();

    int cnt = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_CNT));
    int cc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_COLS));
    int mc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_MSG_CNT));
    int pc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_PART_CNT));

    if (debug) {
      BeeKeeper.getLog().finish(dur,
          BeeUtils.addName(BeeService.RPC_FIELD_QID, id),
          BeeUtils.addName(BeeService.RPC_FIELD_SVC, svc));

      BeeKeeper.getLog().info(BeeUtils.addName(BeeService.RPC_FIELD_CTP, ctp),
          BeeUtils.addName("len", len),
          BeeUtils.addName(BeeService.RPC_FIELD_CNT, cnt));
      BeeKeeper.getLog().info(BeeUtils.addName(BeeService.RPC_FIELD_COLS, cc),
          BeeUtils.addName(BeeService.RPC_FIELD_MSG_CNT, mc),
          BeeUtils.addName(BeeService.RPC_FIELD_PART_CNT, pc));
    } else {
      BeeKeeper.getLog().info("response", id, svc, ctp, cnt, cc, mc, pc, len);
    }

    String hSep = resp.getHeader(BeeService.RPC_FIELD_SEP);
    String sep;

    if (BeeUtils.isHexString(hSep)) {
      sep = new String(BeeUtils.fromHex(hSep));
      BeeKeeper.getLog().warning("response separator", BeeUtils.bracket(hSep));
    } else {
      sep = Character.toString(CommUtils.DEFAULT_INFORMATION_SEPARATOR);
      if (!BeeUtils.isEmpty(hSep)) {
        BeeKeeper.getLog().severe("wrong response separator",
            BeeUtils.bracket(hSep));
      }
    }

    if (debug) {
      Header[] headers = resp.getHeaders();
      for (int i = 0; i < headers.length; i++) {
        if (!BeeUtils.isEmpty(headers[i])) {
          BeeKeeper.getLog().info("Header", i + 1, headers[i].getName(),
              headers[i].getValue());
        }
      }

      if (info != null) {
        info.setRespInfo(RpcUtils.responseInfo(resp));
      }
    }

    ResponseMessage[] messages = null;
    if (mc > 0) {
      messages = new ResponseMessage[mc];
      for (int i = 0; i < mc; i++) {
        messages[i] = new ResponseMessage(resp.getHeader(CommUtils.rpcMessageName(i)), true);
      }
      dispatchMessages(mc, messages);
    }

    int[] partSizes = null;
    if (pc > 0) {
      partSizes = new int[pc];
      for (int i = 0; i < pc; i++) {
        partSizes[i] = BeeUtils.toInt(resp.getHeader(CommUtils.rpcPartName(i)));
      }
    }

    if (info != null) {
      info.end(ctp, txt, len, cnt, cc, mc, messages, pc, partSizes);
    }

    if (len == 0) {
      if (mc == 0) {
        BeeKeeper.getLog().warning("response empty");
      }

    } else if (BeeService.isInvocation(svc)) {
      dispatchInvocation(svc, info, txt, mc, messages, cc, cnt, sep);

    } else if (pc > 0) {
      dispatchParts(svc, pc, partSizes, txt);

    } else if (CommUtils.isResource(ctp)) {
      dispatchResource(txt);

    } else if (txt.indexOf(sep) < 0) {
      BeeKeeper.getLog().info("text", txt);

    } else {

      JsArrayString arr = splitResponse(txt, sep, cnt);

      String serviceId = CompositeService.extractServiceId(svc);

      if (!BeeUtils.isEmpty(serviceId) && !debug) {
        CompositeService service = BeeGlobal.getService(serviceId);
        service.doService(arr, cc);
      } else {
        dispatchResponse(svc, cc, arr);
      }
    }

    BeeKeeper.getLog().finish(dur);
    finalizeResponse();
  }

  private void dispatchInvocation(String svc, RpcInfo info, String txt, 
      int mc, ResponseMessage[] messages, int cc, int cnt, String sep) {
    if (info == null) {
      BeeKeeper.getLog().severe("rpc info not available");
      return;
    }
    
    String method = info.getParameter(BeeService.RPC_FIELD_METH);
    if (BeeUtils.isEmpty(method)) {
      BeeKeeper.getLog().severe("rpc parameter [method] not found");
      return;
    }
    
    if (BeeUtils.same(method, "stringInfo")) {
      ResponseHandler.unicodeTest(info, txt, mc, messages);
    } else if (cnt > 0) {
      JsArrayString arr = splitResponse(txt, sep, cnt);
      dispatchResponse(svc, cc, arr);
    } else {
      BeeKeeper.getLog().warning("unknown invocation method", method);
    }
  }

  private void dispatchMessages(int mc, ResponseMessage[] messages) {
    for (int i = 0; i < mc; i++) {
      Level level = messages[i].getLevel();
      if (LogUtils.isOff(level)) {
        continue;
      }
      
      BeeDate date = messages[i].getDate();
      String msg;
      if (date == null) {
        msg = messages[i].getMessage();
      } else {
        msg = BeeUtils.concat(1, date.toLog(), messages[i].getMessage());
      }
      
      if (level == null) {
        BeeKeeper.getLog().info(msg);
      } else {
        BeeKeeper.getLog().log(level, msg);
      }
    }
  }

  private void dispatchParts(String svc, int pc, int[] sizes, String content) {
    if (BeeUtils.same(svc, BeeService.SERVICE_XML_INFO)) {
      ResponseHandler.showXmlInfo(pc, sizes, content);
    } else {
      BeeKeeper.getLog().warning("unknown multipart response", svc);
    }
  }

  private void dispatchResource(String src) {
    BeeKeeper.getUi().showResource(new BeeResource(src));
  }

  private void dispatchResponse(String svc, int cc, JsArrayString arr) {
    if (BeeUtils.same(svc, BeeService.SERVICE_GET_MENU)) {
      BeeKeeper.getMenu().loadCallBack(arr);

    } else if (cc > 0) {
      BeeView view = new ResponseData(arr, cc);
      BeeKeeper.getUi().showGrid(view);

    } else {
      for (int i = 0; i < arr.length(); i++) {
        if (!BeeUtils.isEmpty(arr.get(i))) {
          BeeKeeper.getLog().info(arr.get(i));
        }
      }

      if (BeeUtils.same(svc, BeeService.SERVICE_WHERE_AM_I)) {
        BeeKeeper.getLog().info(BeeConst.whereAmI());
      }
    }
  }

  private void finalizeResponse() {
    BeeKeeper.getLog().addSeparator();
  }
  
  private JsArrayString splitResponse(String txt, String sep, int cnt) {
    JsArrayString arr = BeeJs.split(txt, sep);
    if (cnt > 0 && arr.length() > cnt) {
      arr.setLength(cnt);
    }
    
    return arr;
  }

}
