package com.butent.bee.egg.client;

import com.butent.bee.egg.client.communication.BeeCallback;
import com.butent.bee.egg.client.communication.RpcInfo;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.communication.RpcUtils;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

public class BeeRpc implements BeeModule {
  private final String rpcUrl;

  private RpcList rpcList = new RpcList();
  private BeeCallback callBack = new BeeCallback();

  public BeeRpc(String url) {
    this.rpcUrl = url;
  }

  public String getName() {
    return getClass().getName();
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

  public void init() {
  }

  public void start() {
  }

  public void end() {
  }

  public boolean dispatchService(String svc) {
    boolean z = BeeProperties
        .getBooleanProperty(BeeProperties.COMMUNICATION_METHOD);

    if (z)
      return makePostRequest(svc, "post");
    else
      return makeGetRequest(svc);
  }

  public boolean makeGetRequest(String svc) {
    return makeRequest(RequestBuilder.GET, svc, null, BeeConst.TIME_UNKNOWN);
  }

  public boolean makeGetRequest(String svc, int timeout) {
    return makeRequest(RequestBuilder.GET, svc, null, timeout);
  }

  public boolean makePostRequest(String svc, String data) {
    return makeRequest(RequestBuilder.POST, svc, data, BeeConst.TIME_UNKNOWN);
  }

  public boolean makePostRequest(String svc, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, svc, data, timeout);
  }
  
  public BeeCallback getCallBack() {
    return callBack;
  }

  public void setCallBack(BeeCallback callBack) {
    this.callBack = callBack;
  }

  public RpcList getRpcList() {
    return rpcList;
  }

  public void setRpcList(RpcList rpcList) {
    this.rpcList = rpcList;
  }

  public RpcInfo getRpcInfo(int id) {
    return rpcList.locateInfo(id);
  }
  
  public String getService(int id) {
    RpcInfo info = getRpcInfo(id);
    return (info == null) ? BeeConst.STRING_EMPTY : info.getName();
  }

  private boolean makeRequest(RequestBuilder.Method type, String svc,
      String data, int timeout) {
    Assert.notNull(type);
    Assert.notEmpty(svc);
    
    boolean ok = false;
    boolean debug = BeeGlobal.isDebug();

    RpcInfo info = new RpcInfo(type, svc);
    int id = info.getId();

    String qs = buildQuery(svc, getDsn());
    String url = RpcUtils.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(type, url);
    if (timeout > 0) {
      bld.setTimeoutMillis(timeout);
      info.setTimeout(timeout);
    }  
    buildHeaders(bld, id);

    info.setReqMsg(qs);

    BeeKeeper.getLog().info("sending request", id, type.toString(), url);
    
    if (!BeeUtils.isEmpty(data)) {
      int size = data.length();
      info.setReqSize(size);
      
      if (debug) {
        BeeKeeper.getLog().info("sending data", BeeUtils.bracket(size), data);
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
    
    if (debug) {
      info.setReqInfo(RpcUtils.requestInfo(bld));
    }
    rpcList.addInfo(info);

    return ok;
  }

  private String buildQuery(String svc, String dsn) {
    return RpcUtils.buildQueryString(BeeService.RPC_FIELD_QNM, svc,
        BeeService.RPC_FIELD_DSN, dsn);
  }

  private void buildHeaders(RequestBuilder bld, int id) {
    if (id > 0) {
      bld.setHeader(BeeService.RPC_FIELD_QID, BeeUtils.transform(id));
    }

    String opt = buildOptions();
    if (!BeeUtils.isEmpty(opt)) {
      bld.setHeader(BeeService.RPC_FIELD_OPT, opt);
    }
  }

  private String buildOptions() {
    if (BeeGlobal.isDebug()) {
      return BeeService.OPTION_DEBUG;
    }  
    else {
      return BeeConst.STRING_EMPTY;
    }
  }

  private String getDsn() {
    return BeeKeeper.getUi().getDsn();
  }

}
