package com.butent.bee.server.jdbc;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Contains information and operational methods (mostly getting and changing parameters) for a JDBC
 * connection to a database.
 */

public class BeeConnection {
  private static final Logger logger = Logger.getLogger(BeeConnection.class.getName());

  public static List<Property> getInfo(Connection conn) {
    Assert.notNull(conn);
    List<Property> lst = new ArrayList<Property>();

    int z;

    try {
      PropertyUtils.addProperties(lst, "Auto Commit", conn.getAutoCommit(),
          "Catalog", conn.getCatalog());

      z = conn.getHoldability();
      PropertyUtils.addProperty(lst, "Holdability",
          BeeUtils.concat(1, z, JdbcUtils.holdabilityAsString(z)));

      z = conn.getTransactionIsolation();
      PropertyUtils.addProperty(lst, "Transaction Isolation",
          BeeUtils.concat(1, z, JdbcUtils.transactionIsolationAsString(z)));

      PropertyUtils.addProperty(lst, "Read Only", conn.isReadOnly());

      Properties prp = conn.getClientInfo();
      if (!BeeUtils.isEmpty(prp)) {
        for (String p : prp.stringPropertyNames()) {
          PropertyUtils.addProperty(lst, "Client Info", BeeUtils.addName(p, prp.getProperty(p)));
        }
      }

      Map<String, Class<?>> tm = conn.getTypeMap();
      if (!BeeUtils.isEmpty(tm)) {
        for (Map.Entry<String, Class<?>> me : tm.entrySet()) {
          PropertyUtils.addProperty(lst, "Type Map",
              BeeUtils.addName(me.getKey(), me.getValue().toString()));
        }
      }

      SQLWarning warn = conn.getWarnings();
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

  private boolean autoCommit;
  private String catalog;
  private Properties clientInfo;
  private int holdability;
  private int transactionIsolation;
  private Map<String, Class<?>> typeMap;

  private boolean readOnly;
  private final Set<State> states = EnumSet.noneOf(State.class);

  private List<SQLException> errors = new ArrayList<SQLException>();

  public BeeConnection(Connection conn) {
    setConnectionInfo(conn);
  }

  protected BeeConnection() {
    super();
  }

  public Statement createStatement(Connection conn, Object... opt) {
    Assert.notNull(conn);
    Statement stmt = null;

    int n = (opt == null) ? 0 : opt.length;
    if (n <= 0) {
      try {
        stmt = conn.createStatement();
      } catch (SQLException ex) {
        handleError(ex);
      }
      return stmt;
    }

    int type = JdbcConst.UNKNOWN_RESULT_SET_TYPE;
    int concur = JdbcConst.UNKNOWN_CONCURRENCY;
    int hold = JdbcConst.UNKNOWN_HOLDABILITY;

    int z;
    String s;

    for (int i = 0; i < n; i++) {
      if (opt[i] instanceof Number) {
        z = ((Number) opt[i]).intValue();

        if (JdbcUtils.validRsType(z)) {
          type = z;
        } else if (JdbcUtils.validConcurrency(z)) {
          concur = z;
        } else if (JdbcUtils.validHoldability(z)) {
          hold = z;
        }
      } else if (opt[i] instanceof String) {
        s = ((String) opt[i]).trim();
        if (BeeUtils.isEmpty(s) || BeeConst.isDefault(s)) {
          continue;
        }

        if (JdbcUtils.validRsType(s)) {
          type = JdbcUtils.rsTypeFromString(s);
        } else if (JdbcUtils.validConcurrency(s)) {
          concur = JdbcUtils.concurrencyFromString(s);
        } else if (JdbcUtils.validHoldability(s)) {
          hold = JdbcUtils.holdabilityFromString(s);
        }
      }
    }

    try {
      if (type == JdbcConst.UNKNOWN_RESULT_SET_TYPE
          && concur == JdbcConst.UNKNOWN_CONCURRENCY
          && hold == JdbcConst.UNKNOWN_HOLDABILITY) {
        stmt = conn.createStatement();
      } else {
        if (type == JdbcConst.UNKNOWN_RESULT_SET_TYPE) {
          type = JdbcConst.DEFAULT_RESULT_SET_TYPE;
        }
        if (concur == JdbcConst.UNKNOWN_CONCURRENCY) {
          concur = JdbcConst.DEFAULT_CONCURRENCY;
        }

        if (hold == JdbcConst.UNKNOWN_HOLDABILITY) {
          stmt = conn.createStatement(type, concur);
        } else {
          stmt = conn.createStatement(type, concur, hold);
        }
      }
    } catch (SQLFeatureNotSupportedException ex) {
      LogUtils.warning(logger, ex, "type:", type,
          JdbcUtils.rsTypeAsString(type), "concurrency:",
          JdbcUtils.concurrencyAsString(concur), "holdability:", hold,
          JdbcUtils.holdabilityAsString(hold));
    } catch (SQLException ex) {
      handleError(ex);
    }

    return stmt;
  }

  public int getAutoCommitQuietly(Connection conn) {
    if (conn == null) {
      noConnection();
      return BeeConst.INT_ERROR;
    }

    int z;

    try {
      z = BeeUtils.toInt(conn.getAutoCommit());
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }

    return z;
  }

  public String getCatalog() {
    return catalog;
  }

  public Properties getClientInfo() {
    return clientInfo;
  }

  public List<SQLException> getErrors() {
    return errors;
  }

  public int getHoldability() {
    return holdability;
  }

  public int getHoldabilityQuietly(Connection conn) {
    if (conn == null) {
      noConnection();
      return BeeConst.INT_ERROR;
    }

    int z;

    try {
      z = conn.getHoldability();
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }

    return z;
  }

  public int getReadOnlyQuietly(Connection conn) {
    if (conn == null) {
      noConnection();
      return BeeConst.INT_ERROR;
    }

    int z;

    try {
      z = BeeUtils.toInt(conn.isReadOnly());
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }

    return z;
  }

  public Set<State> getStates() {
    return states;
  }

  public int getTransactionIsolation() {
    return transactionIsolation;
  }

  public int getTransactionIsolationQuietly(Connection conn) {
    if (conn == null) {
      noConnection();
      return BeeConst.INT_ERROR;
    }

    int z;

    try {
      z = conn.getTransactionIsolation();
    } catch (SQLException ex) {
      handleError(ex);
      z = BeeConst.INT_ERROR;
    }

    return z;
  }

  public Map<String, Class<?>> getTypeMap() {
    return typeMap;
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  public boolean isAutoCommit() {
    return autoCommit;
  }

  public boolean isModified(Connection conn) {
    Assert.notNull(conn);
    Assert.state(validState());

    if (!checkAutoCommit(conn)) {
      return true;
    } else if (!checkHoldability(conn)) {
      return true;
    } else if (!checkReadOnly(conn)) {
      return true;
    } else if (!checkTransactionIsolation(conn)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void revert(Connection conn) {
    Assert.notNull(conn);

    if (!validState()) {
      return;
    }
    if (!hasState(State.CHANGED)) {
      return;
    }

    try {
      if (conn.getAutoCommit() != isAutoCommit()) {
        conn.setAutoCommit(isAutoCommit());
      }
      if (conn.getHoldability() != getHoldability()) {
        conn.setHoldability(getHoldability());
      }
      if (conn.getTransactionIsolation() != getTransactionIsolation()) {
        conn.setTransactionIsolation(getTransactionIsolation());
      }
      if (conn.isReadOnly() != isReadOnly()) {
        conn.setReadOnly(isReadOnly());
      }
    } catch (SQLException ex) {
      handleError(ex);
    }
  }

  public void setAutoCommit(boolean autoCommit) {
    this.autoCommit = autoCommit;
  }

  public void setCatalog(String catalog) {
    this.catalog = catalog;
  }

  public void setClientInfo(Properties clientInfo) {
    this.clientInfo = clientInfo;
  }

  public void setConnectionInfo(Connection conn) {
    try {
      autoCommit = conn.getAutoCommit();
      catalog = conn.getCatalog();
      clientInfo = conn.getClientInfo();
      holdability = conn.getHoldability();
      transactionIsolation = conn.getTransactionIsolation();
      typeMap = conn.getTypeMap();
      readOnly = conn.isReadOnly();

      setState(State.INITIALIZED);
    } catch (SQLException ex) {
      handleError(ex);
    }
  }

  public void setErrors(List<SQLException> errors) {
    this.errors = errors;
  }

  public void setHoldability(int holdability) {
    this.holdability = holdability;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setState(State state) {
    Assert.notNull(state);
    getStates().clear();
    addState(state);
  }

  public void setTransactionIsolation(int transactionIsolation) {
    this.transactionIsolation = transactionIsolation;
  }

  public void setTypeMap(Map<String, Class<?>> typeMap) {
    this.typeMap = typeMap;
  }

  public boolean updateAutoCommit(Connection conn, String s) {
    Assert.notNull(conn);
    Assert.notEmpty(s);
    Assert.state(validState());

    if (BeeConst.isDefault(s)) {
      return true;
    }
    if (!BeeUtils.isBoolean(s)) {
      LogUtils.warning(logger, "unrecognized auto commit value", s);
      return false;
    }

    return updateAutoCommit(conn, BeeUtils.toBoolean(s));
  }

  public boolean updateHoldability(Connection conn, String s) {
    Assert.notNull(conn);
    Assert.notEmpty(s);
    Assert.state(validState());

    if (BeeConst.isDefault(s)) {
      return true;
    }
    int hold = JdbcUtils.holdabilityFromString(s);
    if (!JdbcUtils.validHoldability(hold)) {
      LogUtils.warning(logger, "unrecognized holdability", s);
      return false;
    }

    return updateHoldability(conn, hold);
  }

  public boolean updateReadOnly(Connection conn, String s) {
    Assert.notNull(conn);
    Assert.notEmpty(s);
    Assert.state(validState());

    if (BeeConst.isDefault(s)) {
      return true;
    }
    if (!BeeUtils.isBoolean(s)) {
      LogUtils.warning(logger, "unrecognized read only value", s);
      return false;
    }

    return updateReadOnly(conn, BeeUtils.toBoolean(s));
  }

  public boolean updateTransactionIsolation(Connection conn, String s) {
    Assert.notNull(conn);
    Assert.notEmpty(s);
    Assert.state(validState());

    if (BeeConst.isDefault(s)) {
      return true;
    }
    int ti = JdbcUtils.transactionIsolationFromString(s);
    if (!JdbcUtils.validTransactionIsolation(ti)) {
      LogUtils.warning(logger, "unrecognized transaction isolation", s);
      return false;
    }

    return updateTransactionIsolation(conn, ti);
  }

  private void addState(State state) {
    if (state != null) {
      getStates().add(state);
    }
  }

  private boolean checkAutoCommit(Connection conn) {
    if (!validState()) {
      return true;
    }
    boolean ok;

    try {
      ok = (conn.getAutoCommit() == isAutoCommit());
    } catch (SQLException ex) {
      handleError(ex);
      ok = true;
    }

    return ok;
  }

  private boolean checkHoldability(Connection conn) {
    if (!validState()) {
      return true;
    }
    boolean ok;

    try {
      ok = (conn.getHoldability() == getHoldability());
    } catch (SQLException ex) {
      handleError(ex);
      ok = true;
    }

    return ok;
  }

  private boolean checkReadOnly(Connection conn) {
    if (!validState()) {
      return true;
    }
    boolean ok;

    try {
      ok = (conn.isReadOnly() == isReadOnly());
    } catch (SQLException ex) {
      handleError(ex);
      ok = true;
    }

    return ok;
  }

  private boolean checkTransactionIsolation(Connection conn) {
    if (!validState()) {
      return true;
    }
    boolean ok;

    try {
      ok = (conn.getTransactionIsolation() == getTransactionIsolation());
    } catch (SQLException ex) {
      handleError(ex);
      ok = true;
    }

    return ok;
  }

  private void handleError(SQLException ex) {
    errors.add(ex);
    LogUtils.error(logger, ex);
    addState(State.ERROR);
  }

  private boolean hasState(State state) {
    if (state == null) {
      return false;
    }
    return getStates().contains(state);
  }

  private void noConnection() {
    handleError(new SQLException("connection not available"));
  }

  private boolean updateAutoCommit(Connection conn, boolean ac) {
    if (ac == isAutoCommit()) {
      return true;
    }
    boolean ok;

    try {
      conn.setAutoCommit(ac);
      addState(State.CHANGED);
      ok = true;
    } catch (SQLException ex) {
      handleError(ex);
      ok = false;
    }

    return ok;
  }

  private boolean updateHoldability(Connection conn, int hold) {
    if (hold == getHoldability()) {
      return true;
    }
    boolean ok;

    try {
      conn.setHoldability(hold);
      int z = conn.getHoldability();
      if (z == hold) {
        addState(State.CHANGED);
        ok = true;
      } else {
        LogUtils.warning(logger, "holdability not set:",
            "expected", hold, JdbcUtils.holdabilityAsString(hold),
            "getHoldability", z, JdbcUtils.holdabilityAsString(z));
        ok = false;
      }
    } catch (SQLFeatureNotSupportedException ex) {
      LogUtils.warning(logger, hold, JdbcUtils.holdabilityAsString(hold), ex);
      ok = false;
    } catch (SQLException ex) {
      handleError(ex);
      ok = false;
    }

    return ok;
  }

  private boolean updateReadOnly(Connection conn, boolean ro) {
    if (ro == isReadOnly()) {
      return true;
    }
    boolean ok;

    try {
      conn.setReadOnly(ro);
      boolean z = conn.isReadOnly();
      if (z == ro) {
        addState(State.CHANGED);
        ok = true;
      } else {
        LogUtils.warning(logger, "read only not set:",
            "expected", BeeUtils.toString(ro), "isReadOnly", BeeUtils.toString(z));
        ok = false;
      }
    } catch (SQLFeatureNotSupportedException ex) {
      LogUtils.warning(logger, "ReadOnly", BeeUtils.toString(ro), ex);
      ok = false;
    } catch (SQLException ex) {
      handleError(ex);
      ok = false;
    }

    return ok;
  }

  private boolean updateTransactionIsolation(Connection conn, int ti) {
    if (ti == getTransactionIsolation()) {
      return true;
    }
    boolean ok;

    try {
      conn.setTransactionIsolation(ti);
      int z = conn.getTransactionIsolation();
      if (z == ti) {
        addState(State.CHANGED);
        ok = true;
      } else {
        LogUtils.warning(logger, "Transaction isolation not set:",
            "expected", ti, JdbcUtils.transactionIsolationAsString(ti),
            "getTransactionIsolation", z, JdbcUtils.transactionIsolationAsString(z));
        ok = false;
      }
    } catch (SQLFeatureNotSupportedException ex) {
      LogUtils.warning(logger, ti, JdbcUtils.transactionIsolationAsString(ti), ex);
      ok = false;
    } catch (SQLException ex) {
      if (ti == Connection.TRANSACTION_NONE) {
        LogUtils.warning(logger, "Transaction isolation level NONE not supported");
      } else {
        LogUtils.severe(logger, ex, "Transaction isolation level",
            ti, JdbcUtils.transactionIsolationAsString(ti), "not supported");
      }
      ok = false;
    }

    return ok;
  }

  private boolean validState() {
    return hasState(State.INITIALIZED) && !hasState(State.ERROR);
  }
}
