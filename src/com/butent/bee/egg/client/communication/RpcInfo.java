package com.butent.bee.egg.client.communication;

import java.util.Collection;

import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.utils.BeeJs;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

public class RpcInfo {
  private static int COUNTER = 0;

  public final static String COL_ID = "Id";
  public final static String COL_NAME = "Name";
  public final static String COL_TYPE = "Type";

  public final static String COL_STATUS = "Status";

  public final static String COL_START = "Start";
  public final static String COL_END = "End";
  public final static String COL_COMPLETED = "Completed";
  public final static String COL_TIMEOUT = "Timeout";
  public final static String COL_EXPIRES = "Expires";

  public final static String COL_REQ_MSG = "Request Msg";
  public final static String COL_REQ_ROWS = "Request Rows";
  public final static String COL_REQ_COLS = "Request Columns";
  public final static String COL_REQ_SIZE = "Request Size";

  public final static String COL_RESP_MSG = "Response Msg";
  public final static String COL_RESP_ROWS = "Response Rows";
  public final static String COL_RESP_COLS = "Response Columns";
  public final static String COL_RESP_SIZE = "Response Size";

  public final static String COL_ERR_MSG = "Error Msg";

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

  String errMsg = null;

  public RpcInfo() {
    id = ++COUNTER;
    duration = new BeeDuration();
  }

  public RpcInfo(String name) {
    this();
    this.name = name;
  }

  public RpcInfo(String name, String reqMsg) {
    this(name);
    this.reqMsg = reqMsg;
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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public RequestBuilder.Method getType() {
    return type;
  }

  public void setType(RequestBuilder.Method type) {
    this.type = type;
  }

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public RequestBuilder getReqBuilder() {
    return reqBuilder;
  }

  public void setReqBuilder(RequestBuilder reqBuilder) {
    this.reqBuilder = reqBuilder;
  }

  public Request getRequest() {
    return request;
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public Collection<StringProp> getReqInfo() {
    return reqInfo;
  }

  public void setReqInfo(Collection<StringProp> reqInfo) {
    this.reqInfo = reqInfo;
  }

  public String getReqMsg() {
    return reqMsg;
  }

  public void setReqMsg(String reqMsg) {
    this.reqMsg = reqMsg;
  }

  public int getReqRows() {
    return reqRows;
  }

  public void setReqRows(int reqRows) {
    this.reqRows = reqRows;
  }

  public int getReqCols() {
    return reqCols;
  }

  public void setReqCols(int reqCols) {
    this.reqCols = reqCols;
  }

  public int getReqSize() {
    return reqSize;
  }

  public void setReqSize(int reqSize) {
    this.reqSize = reqSize;
  }

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public Collection<SubProp> getRespInfo() {
    return respInfo;
  }

  public void setRespInfo(Collection<SubProp> respInfo) {
    this.respInfo = respInfo;
  }

  public String getRespMsg() {
    return respMsg;
  }

  public void setRespMsg(String respMsg) {
    this.respMsg = respMsg;
  }

  public int getRespRows() {
    return respRows;
  }

  public void setRespRows(int respRows) {
    this.respRows = respRows;
  }

  public int getRespCols() {
    return respCols;
  }

  public void setRespCols(int respCols) {
    this.respCols = respCols;
  }

  public int getRespSize() {
    return respSize;
  }

  public void setRespSize(int respSize) {
    this.respSize = respSize;
  }

  public String getErrMsg() {
    return errMsg;
  }

  public void setErrMsg(String errMsg) {
    this.errMsg = errMsg;
  }

  private int done() {
    return duration.finish();
  }

  private int end(int st, String msg, int rows, int cols, int size, String err) {
    int r = done();

    if (st > 0)
      setState(st);
    else if (!BeeJs.isEmpty(err))
      setState(BeeConst.STATE_ERROR);
    else
      setState(BeeConst.STATE_CLOSED);

    setRespMsg(msg);
    if (rows != BeeConst.SIZE_UNKNOWN)
      setRespRows(rows);
    if (cols != BeeConst.SIZE_UNKNOWN)
      setRespCols(cols);
    if (size != BeeConst.SIZE_UNKNOWN)
      setRespSize(size);

    if (!BeeJs.isEmpty(err))
      setErrMsg(err);

    return r;
  }

  public int endMessage(String msg) {
    return end(BeeConst.STATE_CLOSED, msg, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, BeeConst.SIZE_UNKNOWN, null);
  }

  public int endResult(int rows, int cols) {
    return end(BeeConst.STATE_CLOSED, null, rows, cols, BeeConst.SIZE_UNKNOWN,
        null);
  }

  public int endError(Exception ex) {
    return end(BeeConst.STATE_ERROR, null, BeeConst.SIZE_UNKNOWN,
        BeeConst.SIZE_UNKNOWN, BeeConst.SIZE_UNKNOWN, ex.getMessage());
  }

  public boolean filterStatus(int st) {
    return (state & st) != 0;
  }

  public String getStartTime() {
    return duration.getStartTime();
  }

  public String getEndTime() {
    return duration.getEndTime();
  }

  public String getCompletedTime() {
    return duration.getCompletedTime();
  }

  public String getStatusString() {
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

  public String getSizeString(int z) {
    if (z != BeeConst.SIZE_UNKNOWN)
      return BeeJs.transform(z);
    else
      return BeeConst.STRING_EMPTY;
  }

}
