package com.butent.bee.server.jdbc;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages JDBC result sets, which are objects for containing results from SQL statements to a JDBC
 * server.
 */

public class BeeResultSet implements Transformable {
  private static final Logger logger = Logger.getLogger(BeeResultSet.class.getName());

  public static List<Property> getInfo(ResultSet rs) {
    Assert.notNull(rs);
    List<Property> lst = new ArrayList<Property>();

    int z;
    try {
      z = rs.getType();
      PropertyUtils.addProperty(lst, "Type", BeeUtils.concat(1, z, JdbcUtils.rsTypeAsString(z)));

      z = rs.getConcurrency();
      PropertyUtils.addProperty(lst, "Concurrency",
          BeeUtils.concat(1, z, JdbcUtils.concurrencyAsString(z)));

      PropertyUtils.addProperty(lst, "Holdability", JdbcUtils.getHoldabilityInfo(rs));

      z = rs.getFetchDirection();
      PropertyUtils.addProperty(lst, "Fetch Direction",
          BeeUtils.concat(1, z, JdbcUtils.fetchDirectionAsString(z)));

      PropertyUtils.addProperty(lst, "Fetch Size", rs.getFetchSize());
      PropertyUtils.addProperty(lst, "Cursor Name", JdbcUtils.getCursorName(rs));

      SQLWarning warn = rs.getWarnings();
      if (warn != null) {
        List<String> wLst = JdbcUtils.unchain(warn);
        for (String w : wLst) {
          PropertyUtils.addProperty(lst, "Warning", w);
        }
      }
    } catch (SQLException ex) {
      LogUtils.warning(logger, ex);
    }
    return lst;
  }

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
  private int state = BeeConst.STATE_UNKNOWN;

  private List<Exception> errors = new ArrayList<Exception>();

  public BeeResultSet() {
    super();
  }

  public BeeResultSet(ResultSet rs) {
    setRsInfo(rs);
  }

  public int getColumnCount() {
    return columnCount;
  }

  public BeeColumn[] getColumns() {
    return columns;
  }

  public int getConcurrency() {
    return concurrency;
  }

  public String getCursorName() {
    return cursorName;
  }

  public List<Exception> getErrors() {
    return errors;
  }

  public int getFetchDirection() {
    return fetchDirection;
  }

  public int getFetchDirectionQuietly(ResultSet rs) {
    if (rs == null) {
      noResultSet();
      return BeeConst.INT_ERROR;
    }

    int z;
    try {
      z = rs.getFetchDirection();
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }
    return z;
  }

  public int getFetchSize() {
    return fetchSize;
  }

  public int getFetchSizeQuietly(ResultSet rs) {
    if (rs == null) {
      noResultSet();
      return BeeConst.INT_ERROR;
    }

    int z;

    try {
      z = rs.getFetchSize();
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }

    return z;
  }

  public int getHoldability() {
    return holdability;
  }

  public int getMaxFieldSize() {
    return maxFieldSize;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public int getQueryTimeout() {
    return queryTimeout;
  }

  public List<ExtendedProperty> getRsInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.addProperties(lst, false,
        "type", BeeUtils.concat(1, getType(), JdbcUtils.rsTypeAsString(getType())),
        "fetch direction", BeeUtils.concat(1, getFetchDirection(),
            JdbcUtils.fetchDirectionAsString(getFetchDirection())),
        "concurrency", BeeUtils.concat(1, getConcurrency(),
            JdbcUtils.concurrencyAsString(getConcurrency())),
        "holdability", BeeUtils.concat(1, getHoldability(),
            JdbcUtils.holdabilityAsString(getHoldability())),
        "cursor name", getCursorName(),
        "fetch size", valueAsString(getFetchSize()),
        "max field size", valueAsString(getMaxFieldSize()),
        "max rows", valueAsString(getMaxRows()),
        "query timeout", valueAsString(getQueryTimeout()),
        "poolable", isPoolable(),
        "column count", valueAsString(getColumnCount()));

    BeeColumn[] arr = getColumns();
    if (!BeeUtils.isEmpty(arr)) {
      for (BeeColumn col : arr) {
        PropertyUtils.appendChildrenToExtended(lst, col.getName(), col.getInfo());
      }
    }

    List<Exception> err = getErrors();
    if (!BeeUtils.isEmpty(err)) {
      for (Exception ex : err) {
        PropertyUtils.addExtended(lst, "Error", null, ex.getMessage());
      }
    }
    return lst;
  }

  public int getState() {
    return state;
  }

  public int getType() {
    return type;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public boolean isPoolable() {
    return poolable;
  }

  public void setColumnCount(int columnCount) {
    this.columnCount = columnCount;
  }

  public void setColumns(BeeColumn[] columns) {
    this.columns = columns;
  }

  public void setConcurrency(int concurrency) {
    this.concurrency = concurrency;
  }

  public void setCursorName(String cursorName) {
    this.cursorName = cursorName;
  }

  public void setErrors(List<Exception> errors) {
    this.errors = errors;
  }

  public void setFetchDirection(int fetchDirection) {
    this.fetchDirection = fetchDirection;
  }

  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }

  public void setHoldability(int holdability) {
    this.holdability = holdability;
  }

  public void setInitialized() {
    if (!hasErrors()) {
      addState(BeeConst.STATE_INITIALIZED);
    }
  }

  public void setMaxFieldSize(int maxFieldSize) {
    this.maxFieldSize = maxFieldSize;
  }

  public void setMaxRows(int maxRows) {
    this.maxRows = maxRows;
  }

  public void setPoolable(boolean poolable) {
    this.poolable = poolable;
  }

  public void setQueryTimeout(int queryTimeout) {
    this.queryTimeout = queryTimeout;
  }

  public void setRsInfo(ResultSet rs) {
    Assert.notNull(rs);

    try {
      setConcurrency(rs.getConcurrency());
      setCursorName(JdbcUtils.getCursorName(rs));
      setFetchDirection(rs.getFetchDirection());
      setFetchSize(rs.getFetchSize());
      setHoldability(JdbcUtils.getHoldability(rs));
      setType(rs.getType());

      setMdInfo(rs.getMetaData());
      setStmtInfo(rs.getStatement());

      if (!hasErrors()) {
        state = BeeConst.STATE_INITIALIZED;
      }
    } catch (SQLException ex) {
      handleError(ex);
    }
  }

  public void setState(int state) {
    this.state = state;
  }

  public void setType(int type) {
    this.type = type;
  }

  @Override
  public String toString() {
    List<ExtendedProperty> lst = getRsInfo();
    StringBuilder sb = new StringBuilder();

    for (ExtendedProperty el : lst) {
      if (sb.length() > 0) {
        sb.append(BeeConst.DEFAULT_ROW_SEPARATOR);
      }
      sb.append(BeeUtils.concat(1, el.getName(), el.getSub(), el.getValue()));
    }
    return sb.toString();
  }

  public String transform() {
    return toString();
  }

  public boolean updateFetchDirection(ResultSet rs, String s) {
    Assert.notNull(rs);
    Assert.notEmpty(s);
    Assert.state(validState());

    if (BeeConst.isDefault(s)) {
      return true;
    }
    int direction = JdbcUtils.fetchDirectionFromString(s);
    if (!JdbcUtils.validFetchDirection(direction)) {
      LogUtils.warning(logger, "unrecognized fetch direction", s);
      return false;
    }

    return updateFetchDirection(rs, direction);
  }

  public boolean updateFetchSize(ResultSet rs, int size) {
    Assert.notNull(rs);
    Assert.state(validState());

    if (size == getFetchSize()) {
      return true;
    }
    boolean ok;

    try {
      rs.setFetchSize(size);
      int z = rs.getFetchSize();
      if (z == size) {
        addState(BeeConst.STATE_CHANGED);
        ok = true;
      } else {
        LogUtils.warning(logger, "fetch size not updated:", "expected", size, "getFetchSize", z);
        ok = false;
      }
    } catch (SQLException ex) {
      if (size < 0) {
        LogUtils.warning(logger, ex, "Fetch Size:", size);
      } else {
        handleError(ex);
      }
      ok = false;
    }
    return ok;
  }

  private void addState(int st) {
    this.state |= st;
  }

  private void handleError(SQLException ex) {
    errors.add(ex);
    LogUtils.error(logger, ex);
    addState(BeeConst.STATE_ERROR);
  }

  private boolean hasState(int st) {
    return (state & st) != 0;
  }

  private void noResultSet() {
    handleError(new SQLException("result set not available"));
  }

  private void setMdInfo(ResultSetMetaData md) {
    if (md == null) {
      return;
    }

    try {
      setColumnCount(md.getColumnCount());
    } catch (SQLException ex) {
      handleError(ex);
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
    if (stmt == null) {
      return;
    }

    try {
      setMaxFieldSize(stmt.getMaxFieldSize());
      setMaxRows(stmt.getMaxRows());
      setQueryTimeout(stmt.getQueryTimeout());
      setPoolable(stmt.isPoolable());
    } catch (SQLException ex) {
      handleError(ex);
    }
  }

  private boolean updateFetchDirection(ResultSet rs, int direction) {
    if (direction == getFetchDirection()) {
      return true;
    }
    boolean ok;

    try {
      rs.setFetchDirection(direction);
      addState(BeeConst.STATE_CHANGED);
      ok = true;
    } catch (SQLException ex) {
      handleError(ex);
      ok = false;
    }

    return ok;
  }

  private boolean validState() {
    return hasState(BeeConst.STATE_INITIALIZED) && !hasState(BeeConst.STATE_ERROR);
  }

  private String valueAsString(int v) {
    if (v == BeeConst.INDEX_UNKNOWN || v == BeeConst.SIZE_UNKNOWN || v == BeeConst.TIME_UNKNOWN) {
      return BeeUtils.concat(1, v, BeeConst.UNKNOWN);
    } else {
      return Integer.toString(v);
    }
  }
}
