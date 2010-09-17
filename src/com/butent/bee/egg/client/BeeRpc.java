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
    return (info == null) ? BeeConst.STRING_EMPTY : info.getName();
  }

  public void init() {
  }

  public boolean makeGetRequest(String svc) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, BeeConst.TIME_UNKNOWN);
  }

  public boolean makeGetRequest(ParameterList params) {
    return makeRequest(RequestBuilder.GET, params, null, BeeConst.TIME_UNKNOWN);
  }

  public boolean makeGetRequest(String svc, int timeout) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, timeout);
  }

  public boolean makeGetRequest(ParameterList params, int timeout) {
    return makeRequest(RequestBuilder.GET, params, null, timeout);
  }
  
  public boolean makePostRequest(String svc, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), data, BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(ParameterList params, String data) {
    return makeRequest(RequestBuilder.POST, params, data, BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(String svc, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), data, timeout);
  }

  public boolean makePostRequest(ParameterList params, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, params, data, timeout);
  }

  public void setCallBack(BeeCallback callBack) {
    this.callBack = callBack;
  }

  public void setRpcList(RpcList rpcList) {
    this.rpcList = rpcList;
  }

  public void start() {
  }

  private boolean makeRequest(RequestBuilder.Method type, 
      ParameterList params, String data, int timeout) {
    Assert.notNull(type);
    Assert.notNull(params);
    
    String svc = params.getService();
    Assert.notEmpty(svc);

    boolean ok = false;
    boolean debug = BeeGlobal.isDebug();

    RpcInfo info = new RpcInfo(type, svc);
    int id = info.getId();

    String qs = params.getQuery();
    String url = RpcUtils.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(type, url);
    if (timeout > 0) {
      bld.setTimeoutMillis(timeout);
      info.setTimeout(timeout);
    }

    bld.setHeader(BeeService.RPC_FIELD_QID, BeeUtils.transform(id));
    params.getHeaders(bld);
    
    String z = BeeUtils.ifString(data, params.getData());
    
    info.setReqMsg(qs);
    BeeKeeper.getLog().info("sending request", id, type.toString(), url);

    if (!BeeUtils.isEmpty(z)) {
      int size = z.length();
      info.setReqSize(size);

      if (debug) {
        BeeKeeper.getLog().info("sending data", BeeUtils.bracket(size), z);
      }
    }

    try {
      bld.sendRequest(z, callBack);
      info.setState(BeeConst.STATE_OPEN);
      ok = true;
    } catch (RequestException ex) {
      info.endError(ex);
      BeeKeeper.getLog().severe("send request error", id, ex);
    }

    if (debug) {
      info.setReqInfo(RpcUtils.requestInfo(bld));
    }
    rpcList.addInfo(info);

    return ok;
  }

}
