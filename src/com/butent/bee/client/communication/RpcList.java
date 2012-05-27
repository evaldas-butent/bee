package com.butent.bee.client.communication;

import com.google.common.collect.Lists;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Enables to see all processed requests and their information in one object.
 */
@SuppressWarnings("serial")
public class RpcList extends LinkedHashMap<Integer, RpcInfo> {
  
  public static final String[] DEFAULT_INFO_COLUMNS = new String[] {
      RpcInfo.COL_ID, RpcInfo.COL_SERVICE, RpcInfo.COL_METHOD,
      RpcInfo.COL_STATE, RpcInfo.COL_START, RpcInfo.COL_TIMEOUT,
      RpcInfo.COL_EXPIRES, RpcInfo.COL_END, RpcInfo.COL_COMPLETED,
      RpcInfo.COL_REQ_PARAMS, RpcInfo.COL_REQ_TYPE, RpcInfo.COL_REQ_DATA,
      RpcInfo.COL_REQ_ROWS, RpcInfo.COL_REQ_COLS, RpcInfo.COL_REQ_SIZE,
      RpcInfo.COL_RESP_TYPE, RpcInfo.COL_RESP_DATA, RpcInfo.COL_RESP_ROWS,
      RpcInfo.COL_RESP_COLS, RpcInfo.COL_RESP_SIZE, RpcInfo.COL_RESP_MSG_CNT,
      RpcInfo.COL_RESP_MESSAGES, RpcInfo.COL_RESP_PART_CNT,
      RpcInfo.COL_RESP_PART_SIZES, RpcInfo.COL_RESP_INFO, RpcInfo.COL_ERR_MSG,
      RpcInfo.COL_USR_DATA};

  private static final int DEFAULT_MAX_SIZE = 100;

  private int maxSize = DEFAULT_MAX_SIZE;

  public RpcList() {
    super();
  }

  @Override
  public RpcInfo get(Object key) {
    RpcInfo info = super.get(key);
    if (info == null) {
      BeeKeeper.getLog().warning("rpc id", key, "not found");
    }
    return info;
  }

  public List<String[]> getDefaultInfo() {
    return getInfo(null, DEFAULT_INFO_COLUMNS);
  }

  public List<String[]> getDefaultInfo(Collection<State> states) {
    return getInfo(states, DEFAULT_INFO_COLUMNS);
  }

  public List<String[]> getInfo(Collection<State> states, String... cols) {
    Assert.notNull(cols);
    int c = cols.length;
    Assert.parameterCount(c + 1, 2);
    
    List<String[]> result = Lists.newArrayList();
    if (isEmpty()) {
      return result;
    }
    String s;

    for (RpcInfo el : values()) {
      if (!el.filterStates(states)) {
        continue;
      }
      String[] arr = new String[c];

      for (int i = 0; i < c; i++) {
        if (BeeUtils.isEmpty(cols[i])) {
          arr[i] = BeeConst.STRING_EMPTY;
          continue;
        }

        if (BeeUtils.same(cols[i], RpcInfo.COL_ID)) {
          s = BeeUtils.transform(el.getId());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_SERVICE)) {
          s = el.getService();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_STAGE)) {
          s = el.getStage();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_METHOD)) {
          s = el.getMethodString();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_STATE)) {
          s = el.getStateString();

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_START)) {
          s = el.getStartTime();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_TIMEOUT)) {
          s = el.getTimeoutString();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_EXPIRES)) {
          s = el.getExpireTime();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_END)) {
          s = el.getEndTime();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_COMPLETED)) {
          s = el.getCompletedTime();

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_PARAMS)) {
          s = Codec.escapeUnicode(el.getReqParams().transform());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_TYPE)) {
          s = BeeUtils.transform(el.getReqType());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_DATA)) {
          s = Codec.escapeUnicode(Codec.escapeHtml(el.getReqData()));

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_ROWS)) {
          s = el.getSizeString(el.getReqRows());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_COLS)) {
          s = el.getSizeString(el.getReqCols());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_REQ_SIZE)) {
          s = el.getSizeString(el.getReqSize());

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_TYPE)) {
          s = BeeUtils.transform(el.getRespType());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_DATA)) {
          s = Codec.escapeUnicode(Codec.escapeHtml(el.getRespData()));

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_ROWS)) {
          s = el.getSizeString(el.getRespRows());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_COLS)) {
          s = el.getSizeString(el.getRespCols());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_SIZE)) {
          s = el.getSizeString(el.getRespSize());

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_MSG_CNT)) {
          s = el.getSizeString(el.getRespMsgCnt());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_MESSAGES)) {
          s = Codec.escapeUnicode(ArrayUtils.transform(el.getRespMessages()));

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_PART_CNT)) {
          s = el.getSizeString(el.getRespPartCnt());
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_PART_SIZES)) {
          s = ArrayUtils.transform(el.getRespPartSize());

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_RESP_INFO)) {
          s = el.getRespInfoString();

        } else if (BeeUtils.same(cols[i], RpcInfo.COL_ERR_MSG)) {
          s = el.getErrMsg();
        } else if (BeeUtils.same(cols[i], RpcInfo.COL_USR_DATA)) {
          s = Codec.escapeUnicode(BeeUtils.transformMap(el.getUserData()));

        } else {
          s = BeeConst.STRING_EMPTY;
        }

        if (BeeUtils.isEmpty(s)) {
          arr[i] = BeeConst.STRING_EMPTY;
        } else {
          arr[i] = s;
        }
      }

      result.add(arr);
    }
    return result;
  }
  
  public int getMaxSize() {
    return maxSize;
  }

  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

  public boolean updateRequestInfo(int id, int rows, int cols, int size) {
    RpcInfo el = get(id);

    if (el == null) {
      return false;
    } else {
      if (!BeeConst.isUndef(rows)) {
        el.setReqRows(rows);
      }
      if (!BeeConst.isUndef(cols)) {
        el.setReqCols(cols);
      }
      if (!BeeConst.isUndef(size)) {
        el.setReqSize(size);
      }
    }
    return true;
  }

  @Override
  protected boolean removeEldestEntry(Entry<Integer, RpcInfo> eldest) {
    return getMaxSize() > 0 && size() > getMaxSize();
  }
}
