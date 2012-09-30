package com.butent.bee.server.jdbc;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Manages JDBC result sets, which are objects for containing results from SQL statements to a JDBC
 * server.
 */

public class BeeResultSet implements Transformable {
  private static final BeeLogger logger = LogUtils.getLogger(BeeResultSet.class);

  public static List<Property> getInfo(ResultSet rs) {
    Assert.notNull(rs);
    List<Property> lst = new ArrayList<Property>();

    int z;
    try {
      z = rs.getType();
      PropertyUtils.addProperty(lst, "Type", BeeUtils.joinWords(z, JdbcUtils.rsTypeAsString(z)));

      z = rs.getConcurrency();
      PropertyUtils.addProperty(lst, "Concurrency",
          BeeUtils.joinWords(z, JdbcUtils.concurrencyAsString(z)));

      PropertyUtils.addProperty(lst, "Holdability", JdbcUtils.getHoldabilityInfo(rs));

      z = rs.getFetchDirection();
      PropertyUtils.addProperty(lst, "Fetch Direction",
          BeeUtils.joinWords(z, JdbcUtils.fetchDirectionAsString(z)));

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
      logger.warning(ex);
    }
    return lst;
  }

  private int concurrency = 0;
  private String cursorName = null;
  private int fetchDirection = 0;
  private int fetchSize = BeeConst.UNDEF;
  private int holdability = 0;

  private int type = 0;
  private int columnCount = 0;

  private BeeColumn[] columns = null;
  private int maxFieldSize = BeeConst.UNDEF;
  private int maxRows = BeeConst.UNDEF;
  private int queryTimeout = BeeConst.UNDEF;

  private boolean poolable = false;
  private final Set<State> states = EnumSet.noneOf(State.class);

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
        "type", BeeUtils.joinWords(getType(), JdbcUtils.rsTypeAsString(getType())),
        "fetch direction", BeeUtils.joinWords(getFetchDirection(),
            JdbcUtils.fetchDirectionAsString(getFetchDirection())),
        "concurrency", BeeUtils.joinWords(getConcurrency(),
            JdbcUtils.concurrencyAsString(getConcurrency())),
        "holdability", BeeUtils.joinWords(getHoldability(),
            JdbcUtils.holdabilityAsString(getHoldability())),
        "cursor name", getCursorName(),
        "fetch size", valueAsString(getFetchSize()),
        "max field size", valueAsString(getMaxFieldSize()),
        "max rows", valueAsString(getMaxRows()),
        "query timeout", valueAsString(getQueryTimeout()),
        "poolable", isPoolable(),
        "column count", valueAsString(getColumnCount()));

    BeeColumn[] arr = getColumns();
    if (arr != null) {
      for (BeeColumn col : arr) {
        PropertyUtils.appendExtended(lst, col.getExtendedInfo());
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

  public Set<State> getStates() {
    return states;
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
      addState(State.INITIALIZED);
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
        setState(State.INITIALIZED);
      }
    } catch (SQLException ex) {
      handleError(ex);
    }
  }

  public void setState(State state) {
    Assert.notNull(state);
    getStates().clear();
    addState(state);
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
      sb.append(BeeUtils.joinWords(el.getName(), el.getSub(), el.getValue()));
    }
    return sb.toString();
  }

  @Override
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
      logger.warning("unrecognized fetch direction", s);
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
        addState(State.CHANGED);
        ok = true;
      } else {
        logger.warning("fetch size not updated:", "expected", size, "getFetchSize", z);
        ok = false;
      }
    } catch (SQLException ex) {
      if (size < 0) {
        logger.warning(ex, "Fetch Size:", size);
      } else {
        handleError(ex);
      }
      ok = false;
    }
    return ok;
  }

  private void addState(State state) {
    if (state != null) {
      getStates().add(state);
    }
  }

  private void handleError(SQLException ex) {
    errors.add(ex);
    logger.error(ex);
    addState(State.ERROR);
  }

  private boolean hasState(State state) {
    if (state == null) {
      return false;
    }
    return getStates().contains(state);
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
      setColumnCount(BeeConst.UNDEF);
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
      addState(State.CHANGED);
      ok = true;
    } catch (SQLException ex) {
      handleError(ex);
      ok = false;
    }
    return ok;
  }

  private boolean validState() {
    return hasState(State.INITIALIZED) && !hasState(State.ERROR);
  }

  private String valueAsString(int v) {
    if (BeeConst.isUndef(v)) {
      return BeeUtils.joinWords(v, BeeConst.UNKNOWN);
    } else {
      return Integer.toString(v);
    }
  }
}
