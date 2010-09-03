package com.butent.bee.egg.client;

import com.butent.bee.egg.client.communication.BeeCallback;
import com.butent.bee.egg.client.communication.RpcInfo;
import com.butent.bee.egg.client.communication.RpcList;
import com.butent.bee.egg.client.communication.RpcUtil;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;

public class BeeRpc implements BeeModule {
  private final String rpcUrl;

  private RpcList rpcList = new RpcList();
  private BeeCallback back = new BeeCallback();

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

  public RpcInfo getRpcInfo(int id) {
    return rpcList.locateInfo(id);
  }

  private boolean makeRequest(RequestBuilder.Method type, String svc,
      String data, int timeout) {
    boolean ok = false;
    if (type == null || BeeUtils.isEmpty(svc))
      return ok;

    RpcInfo info = new RpcInfo(type, svc);
    int id = info.getId();

    String qs = buildQuery(svc, getDsn());
    String url = RpcUtil.addQueryString(rpcUrl, qs);

    RequestBuilder bld = new RequestBuilder(type, url);
    if (timeout > 0)
      bld.setTimeoutMillis(timeout);
    buildHeaders(bld, id);

    info.setReqMsg(qs);

    Request req = null;

    BeeKeeper.getLog().log("sending request", id, type.toString(), url);
    if (!BeeUtils.isEmpty(data))
      BeeKeeper.getLog().log("sending data", BeeUtils.bracket(data.length()),
          data);

    try {
      req = bld.sendRequest(data, back);

      info.setState(BeeConst.STATE_OPEN);
      ok = true;
    } catch (RequestException ex) {
      info.endError(ex);

      BeeKeeper.getLog().log("send request error", id, ex);
    }

    info.setReqInfo(RpcUtil.requestInfo(bld, req));
    rpcList.addInfo(info);

    return ok;
  }

  private String buildQuery(String svc, String dsn) {
    return RpcUtil.buildQueryString(BeeService.RPC_FIELD_QNM, svc,
        BeeService.RPC_FIELD_DSN, dsn);
  }

  private void buildHeaders(RequestBuilder bld, int id) {
    if (id > 0)
      bld.setHeader(BeeService.RPC_FIELD_QID, BeeUtils.transform(id));

    String opt = buildOptions();
    if (!BeeUtils.isEmpty(opt))
      bld.setHeader(BeeService.RPC_FIELD_OPT, opt);
  }

  private String buildOptions() {
    if (BeeGlobal.isDebug())
      return BeeService.OPTION_DEBUG;
    else
      return BeeConst.STRING_EMPTY;
  }

  private String getDsn() {
    return BeeKeeper.getUi().getDsn();
  }

}
