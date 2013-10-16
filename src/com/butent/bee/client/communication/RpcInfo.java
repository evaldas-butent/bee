package com.butent.bee.client.communication;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

import com.butent.bee.client.utils.Duration;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * Contains all relevant RPC related information, both request and response, and methods for
 * operating with that information.
 */

public class RpcInfo {
  private static int counter;

  public static final int MAX_DATA_LEN = 1024;

  public static final String COL_ID = "Id";
  public static final String COL_SERVICE = "Service";
  public static final String COL_METHOD = "Method";

  public static final String COL_STATE = "State";

  public static final String COL_START = "Start";
  public static final String COL_END = "End";
  public static final String COL_COMPLETED = "Completed";
  public static final String COL_TIMEOUT = "Timeout";
  public static final String COL_EXPIRES = "Expires";

  public static final String COL_REQ_PARAMS = "Request Params";
  public static final String COL_REQ_TYPE = "Request Type";
  public static final String COL_REQ_DATA = "Request Data";
  public static final String COL_REQ_ROWS = "Request Rows";
  public static final String COL_REQ_COLS = "Request Columns";
  public static final String COL_REQ_SIZE = "Request Size";

  public static final String COL_RESP_TYPE = "Response Type";
  public static final String COL_RESP_DATA = "Response Data";
  public static final String COL_RESP_ROWS = "Response Rows";
  public static final String COL_RESP_COLS = "Response Columns";
  public static final String COL_RESP_SIZE = "Response Size";

  public static final String COL_RESP_MSG_CNT = "Response Msg Cnt";
  public static final String COL_RESP_MESSAGES = "Response Messages";

  public static final String COL_RESP_INFO = "Response Info";

  public static final String COL_ERR_MSG = "Error Msg";

  public static final String COL_USR_DATA = "User Data";

  private int id;
  private String service;
  private RequestBuilder.Method method = RequestBuilder.GET;

  private final Set<State> states = EnumSet.noneOf(State.class);
  private final Duration duration;

  private RequestBuilder reqBuilder;
  private Request request;
  private ParameterList reqParams;

  private String reqData;
  private ContentType reqType;
  private int reqRows = BeeConst.UNDEF;
  private int reqCols = BeeConst.UNDEF;
  private int reqSize = BeeConst.UNDEF;

  private Response response;
  private Collection<ExtendedProperty> respInfo;

  private ContentType respType;
  private String respData;
  private int respRows = BeeConst.UNDEF;
  private int respCols = BeeConst.UNDEF;
  private int respSize = BeeConst.UNDEF;

  private int respMsgCnt = BeeConst.UNDEF;
  private Collection<ResponseMessage> respMessages;

  private String errMsg;

  private Object userData;
  private ResponseCallback respCallback;

  public RpcInfo(RequestBuilder.Method method, String service,
      ParameterList params, ContentType ctp, String data, ResponseCallback callback) {
    this.id = ++counter;
    this.duration = new Duration();

    this.method = method;
    this.service = service;
    this.reqParams = params;
    this.reqType = ctp;
    this.reqData = data;
    this.respCallback = callback;
  }
 
  public void addState(State state) {
    if (state != null) {
      getStates().add(state);
    }
  }

  public boolean cancel() {
    boolean wasPending = false;

    if (getRequest() != null) {
      wasPending = getRequest().isPending();
      getRequest().cancel();
    }
    addState(State.CANCELED);
    
    return wasPending;
  }

  public int done() {
    return duration.finish();
  }

  public int end(ContentType ctp, String data, int size, int rows,
      int cols, int msgCnt, Collection<ResponseMessage> messages) {
    int r = done();
    setState(State.CLOSED);

    setRespType(ctp);
    setRespData(data);

    if (!BeeConst.isUndef(size)) {
      setRespSize(size);
    }
    if (!BeeConst.isUndef(rows)) {
      setRespRows(rows);
    }
    if (!BeeConst.isUndef(cols)) {
      setRespCols(cols);
    }

    if (!BeeConst.isUndef(msgCnt)) {
      setRespMsgCnt(msgCnt);
    }
    if (messages != null) {
      setRespMessages(messages);
    }

    return r;
  }

  public void endError(Exception ex) {
    endError(ex.toString());
  }

  public void endError(String msg) {
    done();
    setState(State.ERROR);
    setErrMsg(msg);
  }

  public boolean filterStates(Collection<State> filter) {
    if (filter == null) {
      return true;
    }
    if (getStates().isEmpty()) {
      return false;
    }
    return BeeUtils.intersects(getStates(), filter);
  }

  public String getCompletedTime() {
    return duration.getCompletedTime();
  }

  public String getEndTime() {
    return duration.getEndTime();
  }

  public String getErrMsg() {
    return errMsg;
  }

  public String getExpireTime() {
    return duration.getExpireTime();
  }

  public int getId() {
    return id;
  }

  public RequestBuilder.Method getMethod() {
    return method;
  }

  public String getMethodString() {
    if (getMethod() == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return getMethod().toString();
    }
  }

  public String getParameter(String name) {
    Assert.notEmpty(name);

    if (getReqParams() == null) {
      return null;
    } else {
      return getReqParams().getParameter(name);
    }
  }

  public RequestBuilder getReqBuilder() {
    return reqBuilder;
  }

  public int getReqCols() {
    return reqCols;
  }

  public String getReqData() {
    return reqData;
  }

  public ParameterList getReqParams() {
    return reqParams;
  }

  public int getReqRows() {
    return reqRows;
  }

  public int getReqSize() {
    return reqSize;
  }

  public ContentType getReqType() {
    return reqType;
  }

  public Request getRequest() {
    return request;
  }

  public ResponseCallback getRespCallback() {
    return respCallback;
  }

  public int getRespCols() {
    return respCols;
  }

  public String getRespData() {
    return respData;
  }

  public Collection<ExtendedProperty> getRespInfo() {
    return respInfo;
  }

  public String getRespInfoString() {
    return BeeUtils.join(BeeConst.DEFAULT_ROW_SEPARATOR, getRespInfo());
  }

  public Collection<ResponseMessage> getRespMessages() {
    return respMessages;
  }

  public int getRespMsgCnt() {
    return respMsgCnt;
  }

  public Response getResponse() {
    return response;
  }

  public int getRespRows() {
    return respRows;
  }

  public int getRespSize() {
    return respSize;
  }

  public ContentType getRespType() {
    return respType;
  }

  public String getService() {
    return service;
  }

  public String getSizeString(int z) {
    if (!BeeConst.isUndef(z)) {
      return BeeUtils.toString(z);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getStartTime() {
    return duration.getStartTime();
  }

  public Set<State> getStates() {
    return states;
  }

  public String getStateString() {
    if (getStates().isEmpty()) {
      return BeeConst.STRING_EMPTY;
    }

    StringBuilder sb = new StringBuilder();
    for (State state : getStates()) {
      if (sb.length() > 0) {
        sb.append(BeeConst.CHAR_SPACE);
      }
      sb.append(state.name().toLowerCase());
    }
    return sb.toString();
  }

  public int getTimeout() {
    return duration.getTimeout();
  }

  public String getTimeoutString() {
    return duration.getTimeoutAsTime();
  }
  
  public Object getUserData() {
    return userData;
  }

  public boolean isCanceled() {
    return getStates().contains(State.CANCELED);
  }

  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setMethod(RequestBuilder.Method method) {
    this.method = method;
  }

  public void setReqBuilder(RequestBuilder reqBuilder) {
    this.reqBuilder = reqBuilder;
  }

  public void setReqCols(int reqCols) {
    this.reqCols = reqCols;
  }

  public void setReqData(String reqData) {
    this.reqData = reqData;
  }

  public void setReqParams(ParameterList reqParams) {
    this.reqParams = reqParams;
  }

  public void setReqRows(int reqRows) {
    this.reqRows = reqRows;
  }

  public void setReqSize(int reqSize) {
    this.reqSize = reqSize;
  }

  public void setReqType(ContentType reqType) {
    this.reqType = reqType;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public void setRespCallback(ResponseCallback respCallback) {
    this.respCallback = respCallback;
  }

  public void setRespCols(int respCols) {
    this.respCols = respCols;
  }

  public void setRespData(String respData) {
    this.respData = BeeUtils.clip(respData, MAX_DATA_LEN);
  }

  public void setRespInfo(Collection<ExtendedProperty> respInfo) {
    this.respInfo = respInfo;
  }

  public void setRespMessages(Collection<ResponseMessage> respMessages) {
    this.respMessages = respMessages;
  }

  public void setRespMsgCnt(int respMsgCnt) {
    this.respMsgCnt = respMsgCnt;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public void setRespRows(int respRows) {
    this.respRows = respRows;
  }

  public void setRespSize(int respSize) {
    this.respSize = respSize;
  }

  public void setRespType(ContentType respType) {
    this.respType = respType;
  }

  public void setService(String service) {
    this.service = service;
  }

  public void setState(State state) {
    Assert.notNull(state);
    getStates().clear();
    getStates().add(state);
  }

  public void setTimeout(int timeout) {
    duration.setTimeout(timeout);
  }

  public void setUserData(Object userData) {
    this.userData = userData;
  }
}
