package com.butent.bee.egg.client.communication;

import java.util.LinkedList;

import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.shared.BeeConst;

@SuppressWarnings("serial")
public class RpcList extends LinkedList<RpcInfo> {
  private static final int DEFAULT_CAPACITY = 1000;
  private int capacity = DEFAULT_CAPACITY;

  public RpcList() {
    super();
  }

  public RpcList(int capacity) {
    this();
    this.capacity = capacity;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
  }

  private void checkCapacity() {
    if (capacity > 0)
      while (size() > capacity)
        remove();
  }

  public void addInfo(RpcInfo el) {
    if (el != null) {
      checkCapacity();
      add(el);
    }
  }

  public int createInfo(String name, String msg) {
    RpcInfo el = new RpcInfo(name, msg);
    addInfo(el);

    return el.getId();
  }

  public RpcInfo locateInfo(int id) {
    RpcInfo el = null;
    if (isEmpty())
      return el;

    for (int i = 0; i < size(); i++)
      if (get(i).getId() == id) {
        el = get(i);
        break;
      }

    return el;
  }

  public boolean updateRequestInfo(int id, int rows, int cols, int size) {
    RpcInfo el = locateInfo(id);

    if (el == null)
      return false;
    else {
      if (rows != BeeConst.SIZE_UNKNOWN)
        el.setReqRows(rows);
      if (cols != BeeConst.SIZE_UNKNOWN)
        el.setReqCols(cols);
      if (size != BeeConst.SIZE_UNKNOWN)
        el.setReqSize(size);
    }

    return true;
  }

  public int endMessage(int id, String msg) {
    RpcInfo el = locateInfo(id);

    if (el == null)
      return BeeConst.TIME_UNKNOWN;
    else
      return el.endMessage(msg);
  }

  public int endResult(int id, int rows, int cols) {
    RpcInfo el = locateInfo(id);

    if (el == null)
      return BeeConst.TIME_UNKNOWN;
    else
      return el.endResult(rows, cols);
  }

  public int endError(int id, Exception ex) {
    RpcInfo el = locateInfo(id);

    if (el == null)
      return BeeConst.TIME_UNKNOWN;
    else
      return el.endError(ex);
  }

  public String[][] getInfo(int status, String... cols) {
    int r = size();
    if (r <= 0)
      return null;

    int c = cols.length;
    if (c <= 0)
      return null;

    boolean filterMd = status > 0;
    int i, j;
    String s;

    RpcList src;
    RpcInfo el;

    if (filterMd) {
      src = new RpcList();

      for (i = 0; i < r; i++) {
        el = get(i);
        if (el.filterStatus(status))
          src.add(el);
      }

      if (src.isEmpty())
        return null;
      r = src.size();
    } else
      src = this;

    String[][] arr = new String[r][c];

    for (i = 0; i < r; i++) {
      el = get(i);

      for (j = 0; j < c; j++) {
        if (BeeJs.isEmpty(cols[j])) {
          arr[i][j] = BeeConst.STRING_EMPTY;
          continue;
        }

        if (cols[j].equals(RpcInfo.COL_ID))
          s = BeeJs.transform(el.getId());
        else if (cols[j].equals(RpcInfo.COL_NAME))
          s = el.getName();
        else if (cols[j].equals(RpcInfo.COL_STATUS))
          s = el.getStatusString();
        else if (cols[j].equals(RpcInfo.COL_START))
          s = el.getStartTime();
        else if (cols[j].equals(RpcInfo.COL_END))
          s = el.getEndTime();
        else if (cols[j].equals(RpcInfo.COL_COMPLETED))
          s = el.getCompletedTime();
        else if (cols[j].equals(RpcInfo.COL_REQ_MSG))
          s = el.getReqMsg();
        else if (cols[j].equals(RpcInfo.COL_REQ_ROWS))
          s = el.getSizeString(el.getReqRows());
        else if (cols[j].equals(RpcInfo.COL_REQ_COLS))
          s = el.getSizeString(el.getReqCols());
        else if (cols[j].equals(RpcInfo.COL_REQ_SIZE))
          s = el.getSizeString(el.getReqSize());
        else if (cols[j].equals(RpcInfo.COL_RESP_MSG))
          s = el.getRespMsg();
        else if (cols[j].equals(RpcInfo.COL_RESP_ROWS))
          s = el.getSizeString(el.getRespRows());
        else if (cols[j].equals(RpcInfo.COL_RESP_COLS))
          s = el.getSizeString(el.getRespCols());
        else if (cols[j].equals(RpcInfo.COL_RESP_SIZE))
          s = el.getSizeString(el.getRespSize());
        else if (cols[j].equals(RpcInfo.COL_ERR_MSG))
          s = el.getErrMsg();
        else
          s = BeeConst.STRING_EMPTY;

        if (BeeJs.isEmpty(s))
          arr[i][j] = BeeConst.STRING_EMPTY;
        else
          arr[i][j] = s;
      }
    }

    return arr;
  }

}
