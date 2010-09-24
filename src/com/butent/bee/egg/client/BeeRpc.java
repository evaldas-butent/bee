package com.butent.bee.egg.client;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

import com.butent.bee.egg.client.communication.BeeCallback;
import com.butent.bee.egg.client.communication.ParameterList;
import com.butent.bee.egg.client.communication.RpcInfo;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.communication.RpcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeRpc implements BeeModule {
  private final String rpcUrl;

  private RpcList rpcList = new RpcList();
  private BeeCallback callBack = new BeeCallback();

  public BeeRpc(String url) {
    this.rpcUrl = url;
  }

  public ParameterList createParameters(String svc) {
    Assert.notEmpty(svc);
    return new ParameterList(svc);
  }

  public boolean dispatchService(String svc) {
    boolean z = BeeProperties.getBooleanProperty(BeeProperties.COMMUNICATION_METHOD);

    if (z) {
      return makePostRequest(svc, "post");
    } else {
      return makeGetRequest(svc);
    }
  }

  public void end() {
  }

  public BeeCallback getCallBack() {
    return callBack;
  }

  public String getDsn() {
    return BeeKeeper.getUi().getDsn();
  }

  public String getName() {
    return getClass().getName();
  }

  public String getOptions() {
    if (BeeGlobal.isDebug()) {
      return BeeService.OPTION_DEBUG;
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

  public void init() {
  }

  public boolean makeGetRequest(ParameterList params) {
    return makeRequest(RequestBuilder.GET, params, null, null,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makeGetRequest(ParameterList params, int timeout) {
    return makeRequest(RequestBuilder.GET, params, null, null, timeout);
  }

  public boolean makeGetRequest(String svc) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makeGetRequest(String svc, int timeout) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null,
        timeout);
  }

  public boolean makePostRequest(ParameterList params,
      BeeService.DATA_TYPE dtp, String data) {
    return makeRequest(RequestBuilder.POST, params, dtp, data,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(ParameterList params,
      BeeService.DATA_TYPE dtp, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, params, dtp, data, timeout);
  }

  public boolean makePostRequest(ParameterList params, String data) {
    return makeRequest(RequestBuilder.POST, params, null, data,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(ParameterList params, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, params, null, data, timeout);
  }

  public boolean makePostRequest(String svc, BeeService.DATA_TYPE dtp,
      String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), dtp, data,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(String svc, BeeService.DATA_TYPE dtp,
      String data, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), dtp, data,
        timeout);
  }

  public boolean makePostRequest(String svc, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data,
        BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(String svc, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data,
        timeout);
  }

  public void setCallBack(BeeCallback callBack) {
    this.callBack = callBack;
  }

  public void setRpcList(RpcList rpcList) {
    this.rpcList = rpcList;
  }

  public void start() {
  }

  private boolean makeRequest(RequestBuilder.Method meth, ParameterList params,
      BeeService.DATA_TYPE dataType, String reqData, int timeout) {
    Assert.notNull(meth);
    Assert.notNull(params);

    String svc = params.getService();
    Assert.notEmpty(svc);

    boolean ok = false;
    boolean debug = BeeGlobal.isDebug();

    BeeService.DATA_TYPE dtp = dataType;
    String data;
    if (BeeUtils.isEmpty(reqData)) {
      data = params.getData();
    } else {
      data = reqData;
    }

    if (BeeUtils.isEmpty(data)) {
      data = null;
      dtp = null;
    } else if (dtp == null) {
      dtp = BeeService.normalizeRequest(params.getDataType());
    }

    RpcInfo info = new RpcInfo(meth, svc, params, dtp, data);
    int id = info.getId();

    String qs = params.getQuery();
    String url = RpcUtils.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(meth, url);
    if (timeout > 0) {
      bld.setTimeoutMillis(timeout);
      info.setTimeout(timeout);
    }

    bld.setHeader(BeeService.RPC_FIELD_QID, BeeUtils.transform(id));
    String ctp = null;

    if (dtp != null) {
      bld.setHeader(BeeService.RPC_FIELD_DTP, BeeService.transform(dtp));

      String z = params.getParameter(BeeService.CONTENT_TYPE_HEADER);
      if (BeeUtils.isEmpty(z)) {
        ctp = BeeService.buildContentType(BeeService.getContentType(dtp),
           BeeService.getCharacterEncoding(dtp));
      } else {
        ctp = z;
      }

      bld.setHeader(BeeService.CONTENT_TYPE_HEADER, ctp);
    }

    params.getHeadersExcept(bld, BeeService.RPC_FIELD_QID, BeeService.RPC_FIELD_DTP,
        BeeService.CONTENT_TYPE_HEADER);

    if (debug) {
      BeeKeeper.getLog().info("request", id, meth.toString(), url);
    } else {
      BeeKeeper.getLog().info("request", id, svc);
    }

    if (!BeeUtils.isEmpty(data)) {
      int size = data.length();
      info.setReqSize(size);

      BeeKeeper.getLog().info("sending data", BeeService.transform(dtp), ctp, BeeUtils.bracket(size));
      if (debug) {
        BeeKeeper.getLog().info(data);
      }
    }

    try {
      bld.sendRequest(data, callBack);
      info.setState(BeeConst.STATE_OPEN);
      ok = true;
    } catch (RequestException ex) {
      info.endError(ex);
      BeeKeeper.getLog().severe("send request error", id, ex);
    }

    rpcList.addInfo(info);

    return ok;
  }

}
