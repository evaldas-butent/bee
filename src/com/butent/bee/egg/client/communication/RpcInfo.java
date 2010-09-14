package com.butent.bee.egg.client.communication;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.Collection;

public class RpcInfo {
  private static int COUNTER = 0;

  public static final String COL_ID = "Id";
  public static final String COL_NAME = "Name";
  public static final String COL_TYPE = "Type";

  public static final String COL_STATE = "State";

  public static final String COL_START = "Start";
  public static final String COL_END = "End";
  public static final String COL_COMPLETED = "Completed";
  public static final String COL_TIMEOUT = "Timeout";
  public static final String COL_EXPIRES = "Expires";

  public static final String COL_REQ_MSG = "Request Msg";
  public static final String COL_REQ_ROWS = "Request Rows";
  public static final String COL_REQ_COLS = "Request Columns";
  public static final String COL_REQ_SIZE = "Request Size";
  public static final String COL_REQ_INFO = "Request Info";

  public static final String COL_RESP_MSG = "Response Msg";
  public static final String COL_RESP_ROWS = "Response Rows";
  public static final String COL_RESP_COLS = "Response Columns";
  public static final String COL_RESP_SIZE = "Response Size";
  public static final String COL_RESP_INFO = "Response Info";

  public static final String COL_ERR_MSG = "Error Msg";

  private int id;
  private String name = null;
  private RequestBuilder.Method type = RequestBuilder.GET;

  private int state = BeeConst.STATE_UNKNOWN;
  private final BeeDuration duration;

  private RequestBuilder reqBuilder = null;
  private Request request = null;
  private Collection<StringProp> reqInfo = null;

  private String reqMsg = null;
  private int reqRows = BeeConst.SIZE_UNKNOWN;
  private int reqCols = BeeConst.SIZE_UNKNOWN;
  private int reqSize = BeeConst.SIZE_UNKNOWN;

  private Response response = null;
  private Collection<SubProp> respInfo = null;

  private String respMsg = null;
  private int respRows = BeeConst.SIZE_UNKNOWN;
  private int respCols = BeeConst.SIZE_UNKNOWN;
  private int respSize = BeeConst.SIZE_UNKNOWN;

  private String errMsg = null;

  public RpcInfo() {
    id = ++COUNTER;
    duration = new BeeDuration();
  }

  public RpcInfo(RequestBuilder.Method type) {
    this();
    this.type = type;
  }

  public RpcInfo(RequestBuilder.Method type, String name) {
    this(name);
    this.type = type;
  }

  public RpcInfo(RequestBuilder.Method type, String name, String reqMsg) {
    this(type, name);
    this.reqMsg = reqMsg;
  }

  public RpcInfo(String name) {
    this();
    this.name = name;
  }

  public RpcInfo(String name, String reqMsg) {
    this(name);
    this.reqMsg = reqMsg;
  }

  public int endColumns(int cols, int size) {
    return end(BeeConst.STATE_CLOSED, null, BeeConst.SIZE_UNKNOWN, cols, size,
        null);
  }

  public int endError(Exception ex) {
    return end(BeeConst.STATE_ERROR, null, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, BeeConst.SIZE_UNKNOWN, ex.toString());
  }

  public int endError(String msg) {
    return end(BeeConst.STATE_ERROR, null, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, BeeConst.SIZE_UNKNOWN, msg);
  }

  public int endMessage(String msg) {
    return end(BeeConst.STATE_CLOSED, msg, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, BeeConst.SIZE_UNKNOWN, null);
  }

  public int endMessage(String msg, int size) {
    return end(BeeConst.STATE_CLOSED, msg, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, size, null);
  }

  public int endResult(int rows, int cols) {
    return end(BeeConst.STATE_CLOSED, null, rows, cols, BeeConst.SIZE_UNKNOWN,
        null);
  }

  public int endResult(int rows, int cols, int size) {
    return end(BeeConst.STATE_CLOSED, null, rows, cols, size, null);
  }

  public int endRows(int rows, int size) {
    return end(BeeConst.STATE_CLOSED, null, rows, BeeConst.SIZE_UNKNOWN, size,
        null);
  }

  public int endSize(int size) {
    return end(BeeConst.STATE_CLOSED, null, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, size, null);
  }

  public boolean filterState(int st) {
    return (state & st) != 0;
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

  public String getName() {
    return name;
  }

  public RequestBuilder getReqBuilder() {
    return reqBuilder;
  }

  public int getReqCols() {
    return reqCols;
  }

  public Collection<StringProp> getReqInfo() {
    return reqInfo;
  }

  public String getReqInfoString() {
    return BeeUtils.transformCollection(getReqInfo(),
        BeeConst.DEFAULT_ROW_SEPARATOR);
  }

  public String getReqMsg() {
    return reqMsg;
  }

  public int getReqRows() {
    return reqRows;
  }

  public int getReqSize() {
    return reqSize;
  }

  public Request getRequest() {
    return request;
  }

  public int getRespCols() {
    return respCols;
  }

  public Collection<SubProp> getRespInfo() {
    return respInfo;
  }

  public String getRespInfoString() {
    return BeeUtils.transformCollection(getRespInfo(),
        BeeConst.DEFAULT_ROW_SEPARATOR);
  }

  public String getRespMsg() {
    return respMsg;
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

  public String getSizeString(int z) {
    if (z != BeeConst.SIZE_UNKNOWN) {
      return BeeUtils.transform(z);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getStartTime() {
    return duration.getStartTime();
  }

  public int getState() {
    return state;
  }

  public String getStateString() {
    String s;

    switch (state) {
      case BeeConst.STATE_OPEN: {
        s = "Open";
        break;
      }
      case BeeConst.STATE_CLOSED: {
        s = "Finished";
        break;
      }
      case BeeConst.STATE_ERROR: {
        s = "Error";
        break;
      }
      case BeeConst.STATE_EXPIRED: {
        s = "Expired";
        break;
      }
      case BeeConst.STATE_CANCELED: {
        s = "Canceled";
        break;
      }
      default:
        s = "Unknown";
    }

    return s;
  }

  public int getTimeout() {
    return duration.getTimeout();
  }

  public String getTimeoutString() {
    return duration.getTimeoutAsTime();
  }

  public RequestBuilder.Method getType() {
    return type;
  }

  public String getTypeString() {
    if (getType() == null) {
      return BeeConst.STRING_EMPTY;
    } else {
      return getType().toString();
    }
  }

  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setReqBuilder(RequestBuilder reqBuilder) {
    this.reqBuilder = reqBuilder;
  }

  public void setReqCols(int reqCols) {
    this.reqCols = reqCols;
  }

  public void setReqInfo(Collection<StringProp> reqInfo) {
    this.reqInfo = reqInfo;
  }

  public void setReqMsg(String reqMsg) {
    this.reqMsg = reqMsg;
  }

  public void setReqRows(int reqRows) {
    this.reqRows = reqRows;
  }

  public void setReqSize(int reqSize) {
    this.reqSize = reqSize;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public void setRespCols(int respCols) {
    this.respCols = respCols;
  }

  public void setRespInfo(Collection<SubProp> respInfo) {
    this.respInfo = respInfo;
  }

  public void setRespMsg(String respMsg) {
    this.respMsg = respMsg;
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

  public void setState(int state) {
    this.state = state;
  }

  public void setTimeout(int timeout) {
    duration.setTimeout(timeout);
  }

  public void setType(RequestBuilder.Method type) {
    this.type = type;
  }

  private int done() {
    return duration.finish();
  }

  private int end(int st, String msg, int rows, int cols, int size, String err) {
    int r = done();

    if (st > 0) {
      setState(st);
    } else if (!BeeUtils.isEmpty(err)) {
      setState(BeeConst.STATE_ERROR);
    } else {
      setState(BeeConst.STATE_CLOSED);
    }

    setRespMsg(msg);
    if (rows != BeeConst.SIZE_UNKNOWN) {
      setRespRows(rows);
    }
    if (cols != BeeConst.SIZE_UNKNOWN) {
      setRespCols(cols);
    }
    if (size != BeeConst.SIZE_UNKNOWN) {
      setRespSize(size);
    }

    if (!BeeUtils.isEmpty(err)) {
      setErrMsg(err);
    }

    return r;
  }

}
