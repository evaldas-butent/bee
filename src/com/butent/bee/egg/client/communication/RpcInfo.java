package com.butent.bee.egg.client.communication;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.communication.ResponseMessage;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RpcInfo {
  private static int COUNTER = 0;

  public static int MAX_DATA_LEN = 1024;

  public static final String COL_ID = "Id";
  public static final String COL_SERVICE = "Service";
  public static final String COL_STAGE = "Stage";
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

  public static final String COL_RESP_PART_CNT = "Response Part Cnt";
  public static final String COL_RESP_PART_SIZES = "Response Part Sizes";

  public static final String COL_RESP_INFO = "Response Info";

  public static final String COL_ERR_MSG = "Error Msg";

  public static final String COL_USR_DATA = "User Data";

  private int id;
  private String service = null;
  private String stage = null;
  private RequestBuilder.Method method = RequestBuilder.GET;

  private int state = BeeConst.STATE_UNKNOWN;
  private BeeDuration duration = new BeeDuration();

  private RequestBuilder reqBuilder = null;
  private Request request = null;
  private ParameterList reqParams = null;

  private String reqData = null;
  private ContentType reqType = null;
  private int reqRows = BeeConst.SIZE_UNKNOWN;
  private int reqCols = BeeConst.SIZE_UNKNOWN;
  private int reqSize = BeeConst.SIZE_UNKNOWN;

  private Response response = null;
  private Collection<SubProp> respInfo = null;

  private ContentType respType = null;
  private String respData = null;
  private int respRows = BeeConst.SIZE_UNKNOWN;
  private int respCols = BeeConst.SIZE_UNKNOWN;
  private int respSize = BeeConst.SIZE_UNKNOWN;

  private int respMsgCnt = BeeConst.SIZE_UNKNOWN;
  private ResponseMessage[] respMessages = null;
  private int respPartCnt = BeeConst.SIZE_UNKNOWN;
  private int[] respPartSize = null;

  private String errMsg = null;
  
  private Map<String, String> userData = null;

  public RpcInfo(RequestBuilder.Method method, String service,
      ParameterList params) {
    this(method, service, params, null, null);
  }

  public RpcInfo(RequestBuilder.Method method, String service,
      ParameterList params, ContentType ctp, String data) {
    id = ++COUNTER;
    duration = new BeeDuration();

    this.method = method;
    this.service = service;
    this.reqParams = params;
    this.reqType = ctp;
    this.reqData = data;
  }

  protected RpcInfo() {
  }
  
  public void addUserData(Object... obj) {
    Assert.parameterCount(obj.length, 2);
    Assert.isEven(obj.length);
    
    if (userData == null) {
      userData = new HashMap<String, String>();
    }
    
    for (int i = 0; i < obj.length; i += 2) {
      if (!(obj[i] instanceof String)) {
        BeeKeeper.getLog().warning("parameter", i, "not a string");
        continue;
      }
      
      userData.put((String) obj[i], BeeUtils.transformNoTrim(obj[i + 1])); 
    }
  }

  public int end(ContentType ctp, String data, int size, int rows,
      int cols, int msgCnt, ResponseMessage[] messages, int partCnt, int[] partSizes) {
    int r = done();
    setState(BeeConst.STATE_CLOSED);

    setRespType(ctp);
    setRespData(data);

    if (size != BeeConst.SIZE_UNKNOWN) {
      setRespSize(size);
    }
    if (rows != BeeConst.SIZE_UNKNOWN) {
      setRespRows(rows);
    }
    if (cols != BeeConst.SIZE_UNKNOWN) {
      setRespCols(cols);
    }

    if (msgCnt != BeeConst.SIZE_UNKNOWN) {
      setRespMsgCnt(msgCnt);
    }
    if (messages != null) {
      setRespMessages(messages);
    }

    if (partCnt != BeeConst.SIZE_UNKNOWN) {
      setRespPartCnt(partCnt);
    }
    if (partSizes != null) {
      setRespPartSize(partSizes);
    }

    return r;
  }

  public void endError(Exception ex) {
    endError(ex.toString());
  }

  public void endError(String msg) {
    done();
    setState(BeeConst.STATE_ERROR);
    setErrMsg(msg);
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

  public int getRespCols() {
    return respCols;
  }

  public String getRespData() {
    return respData;
  }

  public Collection<SubProp> getRespInfo() {
    return respInfo;
  }

  public String getRespInfoString() {
    return BeeUtils.transformCollection(getRespInfo(),
        BeeConst.DEFAULT_ROW_SEPARATOR);
  }

  public ResponseMessage[] getRespMessages() {
    return respMessages;
  }

  public int getRespMsgCnt() {
    return respMsgCnt;
  }

  public Response getResponse() {
    return response;
  }

  public int getRespPartCnt() {
    return respPartCnt;
  }

  public int[] getRespPartSize() {
    return respPartSize;
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
    if (z != BeeConst.SIZE_UNKNOWN) {
      return BeeUtils.transform(z);
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public String getStage() {
    return stage;
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

  public Map<String, String> getUserData() {
    return userData;
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

  public void setRespCols(int respCols) {
    this.respCols = respCols;
  }

  public void setRespData(String respData) {
    this.respData = BeeUtils.clip(respData, MAX_DATA_LEN);
  }

  public void setRespInfo(Collection<SubProp> respInfo) {
    this.respInfo = respInfo;
  }

  public void setRespMessages(ResponseMessage[] respMessages) {
    this.respMessages = respMessages;
  }

  public void setRespMsgCnt(int respMsgCnt) {
    this.respMsgCnt = respMsgCnt;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public void setRespPartCnt(int respPartCnt) {
    this.respPartCnt = respPartCnt;
  }

  public void setRespPartSize(int[] respPartSize) {
    this.respPartSize = respPartSize;
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

  public void setStage(String stage) {
    this.stage = stage;
  }

  public void setState(int state) {
    this.state = state;
  }

  public void setTimeout(int timeout) {
    duration.setTimeout(timeout);
  }

  public void setUserData(Map<String, String> userData) {
    this.userData = userData;
  }

  private int done() {
    return duration.finish();
  }
  
}
