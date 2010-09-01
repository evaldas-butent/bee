package com.butent.bee.egg.server.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.server.jdbc.JdbcException;
import com.butent.bee.egg.server.jdbc.JdbcUtils;
import com.butent.bee.egg.shared.BeeColumn;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

public class BeeResultSet implements Transformable {
  private int concurrency = 0;
  private String cursorName = null;
  private int fetchDirection = 0;
  private int fetchSize = BeeConst.SIZE_UNKNOWN;
  private int holdability = 0;
  private int type = 0;

  private int columnCount = 0;
  private BeeColumn[] columns = null;

  private int maxFieldSize = BeeConst.SIZE_UNKNOWN;
  private int maxRows = BeeConst.SIZE_UNKNOWN;
  private int queryTimeout = BeeConst.TIME_UNKNOWN;
  private boolean poolable = false;

  private List<Exception> errors = new ArrayList<Exception>();

  public BeeResultSet() {
    super();
  }

  public BeeResultSet(ResultSet rs) {
    this();
    setRsInfo(rs);
  }

  public int getConcurrency() {
    return concurrency;
  }

  public void setConcurrency(int concurrency) {
    this.concurrency = concurrency;
  }

  public String getCursorName() {
    return cursorName;
  }

  public void setCursorName(String cursorName) {
    this.cursorName = cursorName;
  }

  public int getFetchDirection() {
    return fetchDirection;
  }

  public void setFetchDirection(int fetchDirection) {
    this.fetchDirection = fetchDirection;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }

  public int getHoldability() {
    return holdability;
  }

  public void setHoldability(int holdability) {
    this.holdability = holdability;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public BeeColumn[] getColumns() {
    return columns;
  }

  public void setColumns(BeeColumn[] columns) {
    this.columns = columns;
  }

  public int getMaxFieldSize() {
    return maxFieldSize;
  }

  public void setMaxFieldSize(int maxFieldSize) {
    this.maxFieldSize = maxFieldSize;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public void setMaxRows(int maxRows) {
    this.maxRows = maxRows;
  }

  public int getQueryTimeout() {
    return queryTimeout;
  }

  public void setQueryTimeout(int queryTimeout) {
    this.queryTimeout = queryTimeout;
  }

  public boolean isPoolable() {
    return poolable;
  }

  public void setPoolable(boolean poolable) {
    this.poolable = poolable;
  }

  public List<Exception> getErrors() {
    return errors;
  }

  public void setErrors(List<Exception> errors) {
    this.errors = errors;
  }

  public void setRsInfo(ResultSet rs) {
    if (rs == null)
      return;

    try {
      setConcurrency(rs.getConcurrency());
      // setCursorName(rs.getCursorName());
      setFetchDirection(rs.getFetchDirection());
      setFetchSize(rs.getFetchSize());
      setHoldability(rs.getHoldability());
      setType(rs.getType());

      setMdInfo(rs.getMetaData());
      setStmtInfo(rs.getStatement());
    } catch (SQLException ex) {
      addError(ex);
    } catch (JdbcException ex) {
      addError(ex);
    }
  }

  private void setMdInfo(ResultSetMetaData md) throws JdbcException {
    if (md == null)
      return;

    try {
      setColumnCount(md.getColumnCount());
    } catch (SQLException ex) {
      addError(ex);
      setColumnCount(BeeConst.SIZE_UNKNOWN);

      return;
    }

    int c = getColumnCount();

    if (c > 0) {
      BeeColumn[] arr = new BeeColumn[c];

      for (int i = 0; i < c; i++) {
        arr[i] = new BeeColumn();
        JdbcUtils.setColumnInfo(md, i + 1, arr[i]);
      }

      setColumns(arr);
    }
  }

  private void setStmtInfo(Statement stmt) {
    if (stmt == null)
      return;

    try {
      setMaxFieldSize(stmt.getMaxFieldSize());
      setMaxRows(stmt.getMaxRows());
      setQueryTimeout(stmt.getQueryTimeout());

      setPoolable(stmt.isPoolable());
    } catch (SQLException ex) {
      addError(ex);
    }
  }

  private void addError(Exception ex) {
    if (ex != null)
      errors.add(ex);
  }

  private String valueAsString(int v) {
    if (v == BeeConst.INDEX_UNKNOWN || v == BeeConst.SIZE_UNKNOWN
        || v == BeeConst.TIME_UNKNOWN)
      return BeeUtils.concat(1, v, BeeConst.UNKNOWN);
    else
      return Integer.toString(v);
  }

  public List<SubProp> getRsInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.addPropSub(
        lst,
        false,
        "type",
        BeeUtils.concat(1, getType(), JdbcUtils.rsTypeAsString(getType())),
        "fetch direction",
        BeeUtils.concat(1, getFetchDirection(),
            JdbcUtils.fetchDirectionAsString(getFetchDirection())),
        "concurrency",
        BeeUtils.concat(1, getConcurrency(),
            JdbcUtils.concurrencyAsString(getConcurrency())),
        "holdability",
        BeeUtils.concat(1, getHoldability(),
            JdbcUtils.holdabilityAsString(getHoldability())), "cursor name",
        getCursorName(), "fetch size", valueAsString(getFetchSize()),
        "max field size", valueAsString(getMaxFieldSize()), "max rows",
        valueAsString(getMaxRows()), "query timeout",
        valueAsString(getQueryTimeout()), "poolable",
        isPoolable() ? Boolean.toString(isPoolable()) : null, "column count",
        valueAsString(getColumnCount()));

    BeeColumn[] arr = getColumns();

    if (!BeeUtils.isEmpty(arr))
      for (BeeColumn col : arr)
        PropUtils.appendString(lst, col.getName(), col.getColumnInfo());

    List<Exception> err = getErrors();

    if (!BeeUtils.isEmpty(err))
      for (Exception ex : err)
        PropUtils.addSub(lst, "Error", null, ex.getMessage());

    return lst;
  }

  @Override
  public String toString() {
    return "BeeRs []";
  }

  @Override
  public String transform() {
    return null;
  }

}
