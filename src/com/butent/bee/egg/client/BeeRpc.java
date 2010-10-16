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
import com.butent.bee.egg.shared.communication.CommUtils;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;

public class BeeRpc implements BeeModule {
  private final String rpcUrl;

  private RpcList rpcList = new RpcList();
  private BeeCallback callBack = new BeeCallback();

  public BeeRpc(String url) {
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

  public int dispatchService(String svc) {
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

  public int invoke(String method, String data) {
    return invoke(method, null, data);
  }
  
  public int invoke(String method, ContentType ctp, String data) {
    Assert.notEmpty(method);

    ParameterList params = createParameters(BeeService.SERVICE_INVOKE);
    params.addQueryItem(BeeService.RPC_FIELD_METH, method);
    
    if (data == null) {
      return makeGetRequest(params);
    } else {
      return makePostRequest(params, ctp, data);
    }
  }
  
  public int makeGetRequest(ParameterList params) {
    return makeRequest(RequestBuilder.GET, params, null, null,
        BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(ParameterList params, int timeout) {
    return makeRequest(RequestBuilder.GET, params, null, null, timeout);
  }

  public int makeGetRequest(String svc) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null,
        BeeConst.TIME_UNKNOWN);
  }

  public int makeGetRequest(String svc, int timeout) {
    return makeRequest(RequestBuilder.GET, createParameters(svc), null, null,
        timeout);
  }

  public int makePostRequest(ParameterList params, ContentType ctp, String data) {
    return makeRequest(RequestBuilder.POST, params, ctp, data,
        BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, ContentType ctp, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, params, ctp, data, timeout);
  }

  public int makePostRequest(ParameterList params, String data) {
    return makeRequest(RequestBuilder.POST, params, null, data,
        BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(ParameterList params, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, params, null, data, timeout);
  }

  public int makePostRequest(String svc, ContentType ctp, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), ctp, data,
        BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, ContentType ctp, String data, int timeout) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), ctp, data,
        timeout);
  }

  public int makePostRequest(String svc, String data) {
    return makeRequest(RequestBuilder.POST, createParameters(svc), null, data,
        BeeConst.TIME_UNKNOWN);
  }

  public int makePostRequest(String svc, String data, int timeout) {
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

  private int makeRequest(RequestBuilder.Method meth, ParameterList params,
      ContentType type, String reqData, int timeout) {
    Assert.notNull(meth);
    Assert.notNull(params);

    String svc = params.getService();
    Assert.notEmpty(svc);

    boolean debug = BeeGlobal.isDebug();

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

    RpcInfo info = new RpcInfo(meth, svc, params, ctp, data);
    int id = info.getId();

    String qs = params.getQuery();
    String url = RpcUtils.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(meth, url);
    if (timeout > 0) {
      bld.setTimeoutMillis(timeout);
      info.setTimeout(timeout);
    }

    bld.setHeader(BeeService.RPC_FIELD_QID, BeeUtils.transform(id));
    String cth = null;

    if (ctp != null) {
      bld.setHeader(BeeService.RPC_FIELD_CTP, ctp.transform());

      String z = params.getParameter(CommUtils.CONTENT_TYPE_HEADER);
      if (BeeUtils.isEmpty(z)) {
        cth = CommUtils.buildContentType(CommUtils.getMediaType(ctp),
            CommUtils.getCharacterEncoding(ctp));
      } else {
        cth = z;
      }

      bld.setHeader(CommUtils.CONTENT_TYPE_HEADER, cth);
    }

    params.getHeadersExcept(bld, BeeService.RPC_FIELD_QID, BeeService.RPC_FIELD_CTP,
        CommUtils.CONTENT_TYPE_HEADER);

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
      bld.sendRequest(content, callBack);
      info.setState(BeeConst.STATE_OPEN);
    } catch (RequestException ex) {
      info.endError(ex);
      BeeKeeper.getLog().severe("send request error", id, ex);
    }

    rpcList.addInfo(info);

    return id;
  }

}
