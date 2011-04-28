package com.butent.bee.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.i18n.client.LocaleInfo;

import com.butent.bee.client.communication.AsyncCallback;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcInfo;
import com.butent.bee.client.communication.RpcList;
import com.butent.bee.client.communication.RpcUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

/**
 * enables to generate and manage remote procedure calls, GET and POST statements.
 * 
 * 
 */

public class RpcFactory implements Module {
  private final String rpcUrl;

  private RpcList rpcList = new RpcList();
  private AsyncCallback reqCallBack = new AsyncCallback();

  public RpcFactory(String url) {
    this.rpcUrl = url;
  }

  public void addUserData(int id, Object... obj) {
    RpcInfo info = getRpcInfo(id);
    if (info != null) {
      info.addUserData(obj);
    }
  }

  public ParameterList createParameters(String svc) {
    Assert.notEmpty(svc);
    return new ParameterList(svc);
  }

  public void end() {
  }

  public String getDsn() {
    return BeeKeeper.getUi().getDsn();
  }

  public String getName() {
    return getClass().getName();
  }

  public String getOptions() {
    if (Global.isDebug()) {
      return CommUtils.OPTION_DEBUG;
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public int getPriority(int p) {
    switch (p) {
      case PRIORITY_INIT:
        return DO_NOT_CALL;
      case PRIORITY_START:
        return DO_NOT_CALL;
      case PRIORITY_END:
        return DO_NOT_CALL;
      default:
        return DO_NOT_CALL;
    }
  }

  public AsyncCallback getReqCallBack() {
    return reqCallBack;
  }

  public RpcInfo getRpcInfo(int id) {
    return rpcList.locateInfo(id);
  }

  public RpcList getRpcList() {
    return rpcList;
  }

  public String getService(int id) {
    RpcInfo info = getRpcInfo(id);
    return (info == null) ? BeeConst.STRING_EMPTY : info.getService();
  }

  public Map<String, String> getUserData(int id) {
    RpcInfo info = getRpcInfo(id);

    if (info == null) {
      return null;
    } else {
      return info.getUserData();
    }
  }

  public void init() {
  }

  public int invoke(String method) {
    return invoke(method, null, null);
  }

  public int invoke(String method, ContentType ctp, String data) {
    Assert.notEmpty(method);

    ParameterList params = createParameters(Service.INVOKE);
    params.addQueryItem(Service.RPC_VAR_METH, method);

    if (data == null) {
      return makeGetRequest(params);
    } else {
      return makePostRequest(params, ctp, data);
    }
  }

  public int invoke(String method, String data) {
    return invoke(method, null, data);
  }

  public int makeGetRequest(ParameterList params) {
    return makeRequest(RequestBuilder.GET, params, null, null, null, BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(ParameterList params, ResponseCallback callback) {
    return makeRequest(RequestBuilder.GET, params, null, null, callback, BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(ParameterList params, ResponseCallback callback, int timeout) {
    return makeRequest(RequestBuilder.GET, params, null, null, callback, timeout);
  }

  public int makeGetRequest(String svc) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null, null,
        BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(String svc, ResponseCallback callback) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null,
        callback, BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(String svc, ResponseCallback callback, int timeout) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null, callback, timeout);
  }

  public int makePostRequest(ParameterList params, ContentType ctp, String data) {
    return makeRequest(RequestBuilder.POST, params, ctp, data, null, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, ContentType ctp,
      String data, ResponseCallback callback) {
    return makeRequest(RequestBuilder.POST, params, ctp, data, callback, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, ContentType ctp,
      String data, ResponseCallback callback, int timeout) {
    return makeRequest(RequestBuilder.POST, params, ctp, data, callback, timeout);
  }

  public int makePostRequest(ParameterList params, ResponseCallback callback) {
    return makeRequest(RequestBuilder.POST, params, null, null, callback, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, String data) {
    return makeRequest(RequestBuilder.POST, params, null, data, null, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, String data, ResponseCallback callback) {
    return makeRequest(RequestBuilder.POST, params, null, data, callback, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, String data, ResponseCallback callback,
      int timeout) {
    return makeRequest(RequestBuilder.POST, params, null, data, callback, timeout);
  }

  public int makePostRequest(String svc, ContentType ctp, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), ctp, data,
        null, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, ContentType ctp, String data, ResponseCallback callback) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), ctp, data,
        callback, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, ContentType ctp, String data,
      ResponseCallback callback, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), ctp, data, callback, timeout);
  }

  public int makePostRequest(String svc, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data,
        null, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, String data, ResponseCallback callback) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data,
        callback, BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, String data, ResponseCallback callback, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data, callback, timeout);
  }

  public int sendText(String svc, String data) {
    return makePostRequest(svc, ContentType.BINARY, data);
  }

  public int sendText(String svc, String data, ResponseCallback callback) {
    return makePostRequest(svc, ContentType.BINARY, data, callback);
  }

  public void setReqCallBack(AsyncCallback reqCallBack) {
    this.reqCallBack = reqCallBack;
  }

  public void setRpcList(RpcList rpcList) {
    this.rpcList = rpcList;
  }

  public void start() {
  }

  private int makeRequest(RequestBuilder.Method meth, ParameterList params,
      ContentType type, String reqData, ResponseCallback callback, int timeout) {
    Assert.notNull(meth);
    Assert.notNull(params);

    String svc = params.getService();
    Assert.notEmpty(svc);

    params.addHeaderItem(Service.RPC_VAR_LOC, LocaleInfo.getCurrentLocale().getLocaleName());

    boolean debug = Global.isDebug();

    ContentType ctp = type;
    String data;
    if (BeeUtils.isEmpty(reqData)) {
      data = params.getData();
    } else {
      data = reqData;
    }

    if (BeeUtils.isEmpty(data)) {
      data = null;
      ctp = null;
    } else if (ctp == null) {
      ctp = CommUtils.normalizeRequest(params.getContentType());
    }

    RpcInfo info = new RpcInfo(meth, svc, params, ctp, data, callback);
    int id = info.getId();

    String qs = params.getQuery();
    String url = RpcUtils.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(meth, url);
    if (timeout > 0) {
      bld.setTimeoutMillis(timeout);
      info.setTimeout(timeout);
    }

    String sid = BeeKeeper.getUser().getSessionId();
    if (!BeeUtils.isEmpty(sid)) {
      bld.setHeader(Service.RPC_VAR_SID, sid);
    }
    bld.setHeader(Service.RPC_VAR_QID, BeeUtils.transform(id));
    String cth = null;

    if (ctp != null) {
      bld.setHeader(Service.RPC_VAR_CTP, ctp.transform());

      String z = params.getParameter(CommUtils.CONTENT_TYPE_HEADER);
      if (BeeUtils.isEmpty(z)) {
        cth = CommUtils.buildContentType(CommUtils.getMediaType(ctp),
            CommUtils.getCharacterEncoding(ctp));
      } else {
        cth = z;
      }
      bld.setHeader(CommUtils.CONTENT_TYPE_HEADER, cth);
    }

    params.getHeadersExcept(bld, Service.RPC_VAR_QID,
        Service.RPC_VAR_CTP, CommUtils.CONTENT_TYPE_HEADER);

    if (debug) {
      BeeKeeper.getLog().info("request", id, meth.toString(), url);
    } else {
      BeeKeeper.getLog().info("request", id, svc);
    }

    String content = null;

    if (!BeeUtils.isEmpty(data)) {
      content = CommUtils.prepareContent(ctp, data);
      int size = content.length();
      info.setReqSize(size);

      BeeKeeper.getLog().info("sending", BeeUtils.transform(ctp), cth, BeeUtils.bracket(size));
      if (debug) {
        BeeKeeper.getLog().info(data);
      }
    }

    try {
      bld.sendRequest(content, reqCallBack);
      info.setState(BeeConst.STATE_OPEN);
    } catch (RequestException ex) {
      info.endError(ex);
      BeeKeeper.getLog().severe("send request error", id, ex);
    }

    rpcList.addInfo(info);
    return id;
  }
}
