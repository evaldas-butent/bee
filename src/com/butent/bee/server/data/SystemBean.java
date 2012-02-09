package com.butent.bee.server.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

import com.butent.bee.server.Config;
import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.data.BeeTable.BeeTrigger;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlConstants;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.XmlState;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlKey;
import com.butent.bee.shared.data.XmlView;
import com.butent.bee.shared.data.XmlView.XmlColumn;
import com.butent.bee.shared.data.XmlView.XmlSimpleColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Ensures core data management functionality containing: data structures for tables and views,
 * current SQL server configuration, creating data tables only when they are in demand, handles data
 * with exceptions etc.
 */

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {

  /**
   * Contains a list of system objects, like state or table.
   */

  public enum SysObject {
    STATE("states"), TABLE("tables"), VIEW("views");

    private String path;

    private SysObject(String path) {
      this.path = path;
    }

    public String getFileName(String objName) {
      Assert.notEmpty(objName);
      return BeeUtils.concat(".", objName, name().toLowerCase(), XmlUtils.defaultXmlExtension);
    }

    public String getPath() {
      return path;
    }

    public String getSchemaPath() {
      return Config.getSchemaPath(name().toLowerCase() + ".xsd");
    }
  }

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  DataSourceBean dsb;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  ModuleHolderBean moduleBean;

  private String dbName;
  private String dbSchema;
  private Map<String, BeeState> stateCache = Maps.newHashMap();
  private Map<String, BeeTable> tableCache = Maps.newHashMap();
  private Map<String, BeeView> viewCache = Maps.newHashMap();

  public void activateTable(String tblName) {
    BeeTable table = getTable(tblName);

    if (!table.isActive()) {
      rebuildTable(table, true);
    }
  }

  public List<Property> checkTables(String... tbls) {
    List<Property> diff = Lists.newArrayList();
    Collection<String> tables;

    if (BeeUtils.isEmpty(tbls)) {
      initTables();
      tables = getTableNames();
    } else {
      tables = Lists.newArrayList(tbls);
    }
    for (String tbl : tables) {
      createTable(getTable(tbl), diff);
    }
    return diff;
  }

  public ResponseObject editStateRoles(String tblName, String stateName) {
    if (!isState(stateName)) {
      return ResponseObject.error("Unknown state:", stateName);
    }
    BeeState state = getState(stateName);

    if (!state.supportsRoles()) {
      return ResponseObject.error("State does not support roles:", stateName);
    }
    boolean allMode = BeeUtils.isEmpty(tblName);
    List<BeeTable> tables = Lists.newArrayList();
    List<BeeTable> tmpTables =
        allMode ? Lists.newArrayList(getTables()) : Lists.newArrayList(getTable(tblName));

    for (BeeTable table : tmpTables) {
      if (table.hasState(state)) {
        tables.add(table);
      }
    }
    SqlSelect union = null;
    Map<Long, String> roles = usr.getRoles();

    for (BeeTable table : tables) {
      String tbl = table.getName();
      SqlSelect ss = null;

      if (allMode) {
        ss = new SqlSelect()
            .addConstant(tbl, "Table")
            .addCount("TotalRows")
            .addFrom(tbl);
      } else {
        ss = getViewQuery(tbl);
      }
      String stateAlias = joinState(ss, tbl, null, state.getName());

      for (long roleId : roles.keySet()) {
        String colName = state.getName() + roleId + roles.get(roleId);

        if (BeeUtils.isEmpty(stateAlias)) {
          if (allMode && state.isChecked()) {
            ss.addCount(colName);
          } else {
            ss.addConstant(BeeUtils.toInt(state.isChecked()), colName);
          }
        } else {
          IsExpression expr =
              SqlUtils.sqlIf(getTable(tbl).checkState(stateAlias, state, roleId), 1, 0);

          if (allMode) {
            ss.addSum(expr, colName);
          } else {
            ss.addExpr(expr, colName);
          }
        }
      }
      if (BeeUtils.isEmpty(union)) {
        union = ss;
      } else {
        union.addUnion(ss);
      }
    }
    if (BeeUtils.isEmpty(union)) {
      return ResponseObject.error("No tables support state: ", stateName);
    }
    return ResponseObject.response(qs.getViewData(union, null));
  }

  public List<DataInfo> getDataInfo() {
    List<DataInfo> lst = Lists.newArrayList();

    Set<String> viewNames = Sets.newHashSet(getViewNames());
    viewNames.addAll(getTableNames());

    for (String viewName : viewNames) {
      lst.add(getDataInfo(viewName));
    }
    return lst;
  }

  public DataInfo getDataInfo(String viewName) {
    BeeView view = getView(viewName);
    BeeTable source = getTable(view.getSourceName());

    List<BeeColumn> columns = null;
    int cnt = BeeConst.UNDEF;

    if (source.isActive()) {
      cnt = getViewSize(viewName, null);
      columns = qs.getViewColumns(view);
    }
    return new DataInfo(viewName, source.getIdName(), source.getVersionName(), columns, cnt);
  }

  public String getDbName() {
    return dbName;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public String getIdName(String tblName) {
    return getTable(tblName).getIdName();
  }

  public String getRelation(String tblName, String fldName) {
    return getTableField(tblName, fldName).getRelation();
  }

  public int getScale(String tblName, String fldName) {
    return getTableField(tblName, fldName).getScale();
  }

  public BeeState getState(String stateName) {
    Assert.state(isState(stateName), "Not a state: " + stateName);
    return stateCache.get(BeeUtils.normalize(stateName));
  }

  public Collection<String> getStateNames() {
    Collection<String> states = Lists.newArrayList();

    for (BeeState state : getStates()) {
      states.add(state.getName());
    }
    return states;
  }

  public BeeTable getTable(String tblName) {
    Assert.state(isTable(tblName), "Not a base table: " + tblName);
    return tableCache.get(BeeUtils.normalize(tblName));
  }

  public BeeField getTableField(String tblName, String fldName) {
    return getTable(tblName).getField(fldName);
  }

  public Collection<BeeField> getTableFields(String tblName) {
    return getTable(tblName).getFields();
  }

  public List<ExtendedProperty> getTableInfo(String tblName) {
    return getTable(tblName).getInfo();
  }

  public Collection<String> getTableNames() {
    Collection<String> tables = Lists.newArrayList();

    for (BeeTable table : getTables()) {
      tables.add(table.getName());
    }
    return tables;
  }

  public Collection<String> getTableStates(String tblName) {
    Collection<String> states = Lists.newArrayList();

    for (BeeState state : getTable(tblName).getStates()) {
      states.add(state.getName());
    }
    return states;
  }

  public String getVersionName(String tblName) {
    return getTable(tblName).getVersionName();
  }

  public BeeView getView(String viewName) {
    Assert.state(isView(viewName), "Not a view: " + viewName);
    BeeView view = viewCache.get(BeeUtils.normalize(viewName));

    if (BeeUtils.isEmpty(view)) {
      view = getDefaultView(viewName);
      register(view, viewCache);
    }
    return view;
  }

  public IsCondition getViewCondition(String viewName, Filter filter) {
    return getView(viewName).getCondition(filter);
  }

  public BeeRowSet getViewData(String viewName, Filter filter, Order order, int limit, int offset,
      String... columns) {

    BeeView view = getView(viewName);
    SqlSelect ss = view.getQuery(filter, order, columns);

    if (limit > 0) {
      ss.setLimit(limit);
    }
    if (offset > 0) {
      ss.setOffset(offset);
    }
    return qs.getViewData(ss, view);
  }

  public Collection<String> getViewNames() {
    Collection<String> views = Lists.newArrayList();

    for (BeeView view : viewCache.values()) {
      views.add(view.getName());
    }
    return views;
  }

  public SqlSelect getViewQuery(String viewName) {
    return getView(viewName).getQuery();
  }

  public int getViewSize(String viewName, Filter filter) {
    return qs.dbRowCount(getView(viewName).getQuery(filter, null));
  }

  public XmlState getXmlState(String moduleName, String stateName) {
    Assert.notEmpty(stateName);

    XmlState xmlState = getXmlState(moduleName, stateName, false);
    XmlState userState = getXmlState(moduleName, stateName, true);

    if (xmlState == null) {
      xmlState = userState;
    } else {
      xmlState.protect().merge(userState);
    }
    return xmlState;
  }

  public XmlState getXmlState(String moduleName, String stateName, boolean userMode) {
    Assert.notEmpty(stateName);
    String resource = moduleBean.getResourcePath(moduleName,
        SysObject.STATE.getPath(), SysObject.STATE.getFileName(stateName));

    if (userMode) {
      resource = Config.getUserPath(resource);
    } else {
      resource = Config.getConfigPath(resource);
    }
    return loadXmlState(resource);
  }

  public XmlTable getXmlTable(String moduleName, String tableName) {
    Assert.notEmpty(tableName);

    XmlTable xmlTable = getXmlTable(moduleName, tableName, false);
    XmlTable userTable = getXmlTable(moduleName, tableName, true);

    if (xmlTable == null) {
      xmlTable = userTable;
    } else {
      xmlTable.protect().merge(userTable);
    }
    return xmlTable;
  }

  public XmlTable getXmlTable(String moduleName, String tableName, boolean userMode) {
    Assert.notEmpty(tableName);
    String resource = moduleBean.getResourcePath(moduleName,
        SysObject.TABLE.getPath(), SysObject.TABLE.getFileName(tableName));

    if (userMode) {
      resource = Config.getUserPath(resource);
    } else {
      resource = Config.getConfigPath(resource);
    }
    return loadXmlTable(resource);
  }

  public XmlView getXmlView(String moduleName, String viewName) {
    Assert.notEmpty(viewName);

    XmlView xmlView = getXmlView(moduleName, viewName, false);
    XmlView userView = getXmlView(moduleName, viewName, true);

    if (userView != null) {
      xmlView = userView;
    }
    return xmlView;
  }

  public XmlView getXmlView(String moduleName, String viewName, boolean userMode) {
    Assert.notEmpty(viewName);
    String resource = moduleBean.getResourcePath(moduleName,
        SysObject.VIEW.getPath(), SysObject.VIEW.getFileName(viewName));

    if (userMode) {
      resource = Config.getUserPath(resource);
    } else {
      resource = Config.getConfigPath(resource);
    }
    return loadXmlView(resource);
  }

  public boolean hasField(String tblName, String fldName) {
    return getTable(tblName).hasField(fldName);
  }

  @Lock(LockType.WRITE)
  public void initDatabase(String dsn) {
    String[] dbTables = new String[0];

    if (SqlBuilderFactory.setDefaultBuilder(qs.dbEngine(dsn), dsn)) {
      dbName = qs.dbName();
      dbSchema = qs.dbSchema();
      dbTables = qs.dbTables(dbName, dbSchema, null);
    }
    for (BeeTable table : getTables()) {
      String tblName = table.getName();
      table.setActive(BeeUtils.inListSame(tblName, dbTables));

      Map<String, String[]> tableFields = Maps.newHashMap();

      for (BeeState state : table.getStates()) {
        tblName = table.getStateTable(state);

        if (BeeUtils.inListSame(tblName, dbTables) && !tableFields.containsKey(tblName)) {
          tableFields.put(tblName,
              qs.dbFields(getDbName(), getDbSchema(), tblName).getColumn(SqlConstants.FLD_NAME));
        }
        table.setStateActive(state, tableFields.get(tblName));
      }
    }
    usr.invalidateCache();
  }

  @Lock(LockType.WRITE)
  public void initStates() {
    initObjects(SysObject.STATE);
    initTables();
  }

  @Lock(LockType.WRITE)
  public void initTables() {
    initObjects(SysObject.TABLE);

    for (BeeTable table : getTables()) {
      for (BeeForeignKey fKey : table.getForeignKeys()) {
        Assert.state(isTable(fKey.getRefTable()), BeeUtils.concat(1,
            "Unknown field", BeeUtils.bracket(table.getName() + "." + fKey.getKeyField()),
            "relation:", BeeUtils.bracket(fKey.getRefTable())));
      }
    }
    initDbTriggers();
    initDatabase(BeeUtils.ifString(SqlBuilderFactory.getDsn(), dsb.getDefaultDsn()));
    initViews();
  }

  @Lock(LockType.WRITE)
  public void initViews() {
    initObjects(SysObject.VIEW);
  }

  public boolean isExtField(String tblName, String fldName) {
    return getTable(tblName).getField(fldName).isExtended();
  }

  public boolean isState(String stateName) {
    return !BeeUtils.isEmpty(stateName) && stateCache.containsKey(BeeUtils.normalize(stateName));
  }

  public boolean isTable(String tblName) {
    return !BeeUtils.isEmpty(tblName) && tableCache.containsKey(BeeUtils.normalize(tblName));
  }

  public boolean isView(String viewName) {
    return !BeeUtils.isEmpty(viewName)
        && (viewCache.containsKey(BeeUtils.normalize(viewName)) || isTable(viewName));
  }

  public String joinExtField(HasFrom<?> query, String tblName, String tblAlias, String fldName) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeField field = table.getField(fldName);

    if (!field.isExtended()) {
      LogUtils.warning(logger, "Field is not extended:", tblName, fldName);
      return null;
    }
    return table.joinExtField(query, tblAlias, field);
  }

  public String joinState(HasFrom<?> query, String tblName, String tblAlias, String stateName) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeState state = getState(stateName);

    if (!table.hasState(state)) {
      LogUtils.warning(logger, "State not registered:", tblName, stateName);
      return null;
    }
    return table.joinState(query, tblAlias, state);
  }

  public String joinTranslationField(HasFrom<?> query, String tblName, String tblAlias,
      String fldName, String locale) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeField field = table.getField(fldName);

    return table.joinTranslationField(query, tblAlias, field, locale);
  }

  public XmlState loadXmlState(String resource) {
    return XmlUtils.unmarshal(XmlState.class, resource, SysObject.STATE.getSchemaPath());
  }

  public XmlTable loadXmlTable(String resource) {
    return XmlUtils.unmarshal(XmlTable.class, resource, SysObject.TABLE.getSchemaPath());
  }

  public XmlView loadXmlView(String resource) {
    return XmlUtils.unmarshal(XmlView.class, resource, SysObject.VIEW.getSchemaPath());
  }

  public void rebuildActiveTables() {
    initTables();

    for (BeeTable table : getTables()) {
      if (table.isActive()) {
        rebuildTable(table, true);
      }
    }
  }

  public void rebuildTable(String tblName, boolean ref) {
    rebuildTable(getTable(tblName), ref);
  }

  public SqlSelect verifyStates(SqlSelect query, String tblName, String tblAlias, String... states) {
    Assert.notNull(query);

    long userId = usr.getCurrentUserId();
    long[] userRoles = usr.getUserRoles(userId);

    if (BeeUtils.isEmpty(userRoles)) {
      return query.setWhere(SqlUtils.sqlFalse());
    }
    long[] bits = Longs.concat(new long[] {-userId}, userRoles);

    for (String stateName : states) {
      BeeTable table = getTable(tblName);
      BeeState state = getState(stateName);

      if (!table.hasState(state)) {
        LogUtils.warning(logger, "State not registered:", tblName, stateName);
      } else {
        table.verifyState(query, tblAlias, state, bits);
      }
    }
    return query;
  }

  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    for (BeeForeignKey fKey : fKeys) {
      String refTblName = fKey.getRefTable();
      makeStructureChanges(SqlUtils.createForeignKey(fKey.getTable(), fKey.getName(),
          fKey.getKeyField(), refTblName, getIdName(refTblName), fKey.getCascade()));
    }
  }

  private void createKeys(Collection<BeeKey> keys) {
    for (BeeKey key : keys) {
      String tblName = key.getTable();

      IsQuery index;
      String keyName = key.getName();
      String[] keyFields = key.getKeyFields();

      if (key.isPrimary()) {
        index = SqlUtils.createPrimaryKey(tblName, keyName, keyFields);
      } else {
        index = SqlUtils.createIndex(key.isUnique(), tblName, keyName, keyFields);
      }
      makeStructureChanges(index);
    }
  }

  private Map<String, String> createTable(BeeTable table, List<Property> diff) {
    String tblName = table.getName();
    Map<String, SqlCreate> newTables = Maps.newHashMap();

    newTables.put(tblName, new SqlCreate(tblName, false)
        .addLong(table.getIdName(), true)
        .addLong(table.getVersionName(), true));

    for (BeeField field : table.getFields()) {
      tblName = field.getTable();

      if (field.isExtended()) {
        SqlCreate sc = table.createExtTable(newTables.get(tblName), field);

        if (!BeeUtils.isEmpty(sc)) {
          newTables.put(tblName, sc);
        }
      } else {
        newTables.get(tblName)
            .addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
                field.isNotNull());
      }
      if (field.isTranslatable()) {
        tblName = table.getTranslationTable(field);
        SqlCreate sc = table.createTranslationTable(newTables.get(tblName), field);

        if (!BeeUtils.isEmpty(sc)) {
          newTables.put(tblName, sc);
        }
      }
    }
    for (BeeState state : table.getStates()) {
      tblName = table.getStateTable(state);
      SqlCreate sc = table.createStateTable(newTables.get(tblName), state);

      if (!BeeUtils.isEmpty(sc)) {
        newTables.put(tblName, sc);
      }
    }
    Map<String, String> rebuilds = Maps.newHashMap();

    for (SqlCreate sc : newTables.values()) {
      tblName = (String) sc.getTarget().getSource();
      String tblBackup = null;
      boolean update = !qs.isDbTable(getDbName(), getDbSchema(), tblName);

      if (update) {
        if (diff != null) {
          PropertyUtils.addProperty(diff, tblName, "DOES NOT EXIST");
          return null;
        } else {
          makeStructureChanges(sc);
        }
      } else {
        tblBackup = tblName + "_BAK";
        LogUtils.info(logger, "Checking indexes...");
        int c = 0;
        String[] keys = qs.dbIndexes(getDbName(), getDbSchema(), tblName)
            .getColumn(SqlConstants.KEY_NAME);

        for (BeeKey key : table.getKeys()) {
          if (BeeUtils.same(key.getTable(), tblName)) {
            if (ArrayUtils.contains(key.getName(), keys)) {
              c++;
            } else {
              String msg = BeeUtils.concat(1, "INDEX", key.getName(), "NOT IN", keys);
              LogUtils.warning(logger, msg);

              if (diff != null) {
                PropertyUtils.addProperty(diff, tblName, msg);
              } else {
                update = true;
                break;
              }
            }
          }
        }
        if (!update && keys.length > c) {
          String msg = "TOO MANY INDEXES";
          LogUtils.warning(logger, msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!update) {
        LogUtils.info(logger, "Checking foreign keys...");
        int c = 0;
        String[] fKeys = qs.dbForeignKeys(getDbName(), getDbSchema(), tblName, null)
            .getColumn(SqlConstants.KEY_NAME);

        for (BeeForeignKey fKey : table.getForeignKeys()) {
          if (BeeUtils.same(fKey.getTable(), tblName)
              && (BeeUtils.same(fKey.getRefTable(), table.getName())
              || getTable(fKey.getRefTable()).isActive())) {

            if (ArrayUtils.contains(fKey.getName(), fKeys)) {
              c++;
            } else {
              String msg = BeeUtils.concat(1, "FOREIGN KEY", fKey.getName(), "NOT IN", fKeys);
              LogUtils.warning(logger, msg);

              if (diff != null) {
                PropertyUtils.addProperty(diff, tblName, msg);
              } else {
                update = true;
                break;
              }
            }
          }
        }
        if (!update && fKeys.length > c) {
          String msg = "TOO MANY FOREIGN KEYS";
          LogUtils.warning(logger, msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!update) {
        LogUtils.info(logger, "Checking triggers...");
        int c = 0;
        String[] triggers = qs.dbTriggers(getDbName(), getDbSchema(), tblName)
            .getColumn(SqlConstants.TRIGGER_NAME);

        for (BeeTrigger trigger : table.getTriggers()) {
          if (BeeUtils.same(trigger.getTable(), tblName)) {
            if (ArrayUtils.contains(trigger.getName(), triggers)) {
              c++;
            } else {
              String msg = BeeUtils.concat(1, "TRIGGER", trigger.getName(), "NOT IN", triggers);
              LogUtils.warning(logger, msg);

              if (diff != null) {
                PropertyUtils.addProperty(diff, tblName, msg);
              } else {
                update = true;
                break;
              }
            }
          }
        }
        if (!update && triggers.length > c) {
          String msg = "TOO MANY TRIGGERS";
          LogUtils.warning(logger, msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!BeeUtils.isEmpty(tblBackup)) {
        if (qs.isDbTable(getDbName(), getDbSchema(), tblBackup)) {
          makeStructureChanges(SqlUtils.dropTable(tblBackup));
        }
        makeStructureChanges(sc.setTarget(tblBackup));

        SimpleRowSet oldFields = qs.dbFields(getDbName(), getDbSchema(), tblName);
        SimpleRowSet newFields = qs.dbFields(getDbName(), getDbSchema(), tblBackup);

        if (!update && oldFields.getNumberOfRows() != newFields.getNumberOfRows()) {
          String msg = "FIELD COUNT DOESN'T MATCH";
          LogUtils.warning(logger, msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
        if (!update) {
          LogUtils.info(logger, "Checking fields...");
          int i = 0;
          for (Map<String, String> oldFieldInfo : oldFields) {
            Map<String, String> newFieldInfo = newFields.getRow(i++);

            for (String info : oldFieldInfo.keySet()) {
              if (!BeeUtils.same(info, SqlConstants.TBL_NAME)
                  && !BeeUtils.equals(oldFieldInfo.get(info), newFieldInfo.get(info))) {

                String msg = BeeUtils.concat(1, "FIELD",
                    oldFieldInfo.get(SqlConstants.FLD_NAME) + ":",
                    info, oldFieldInfo.get(info), "!=", newFieldInfo.get(info));
                LogUtils.warning(logger, msg);

                if (diff != null) {
                  PropertyUtils.addProperty(diff, tblName, msg);
                } else {
                  update = true;
                  break;
                }
              }
            }
            if (update) {
              break;
            }
          }
        }
        if (update) {
          Map<String, String> updFlds = Maps.newLinkedHashMap();
          String[] oldList = oldFields.getColumn(SqlConstants.FLD_NAME);

          for (String newFld : newFields.getColumn(SqlConstants.FLD_NAME)) {
            for (String oldFld : oldList) {
              if (BeeUtils.same(newFld, oldFld)) {
                updFlds.put(newFld, oldFld);
              }
            }
          }
          if (!BeeUtils.isEmpty(updFlds)) {
            Object res = qs.doSql(new SqlInsert(tblBackup)
                .addFields(updFlds.keySet().toArray(new String[0]))
                .setDataSource(new SqlSelect()
                    .addFields(tblName, updFlds.values().toArray(new String[0]))
                    .addFrom(tblName))
                .getQuery());

            Assert.state(res instanceof Number, "Error inserting data");
          }
        } else {
          makeStructureChanges(SqlUtils.dropTable(tblBackup));
        }
      }
      if (update) {
        rebuilds.put(tblName, tblBackup);
      }
    }
    return rebuilds;
  }

  private void createTriggers(Collection<BeeTrigger> triggers) {
    for (BeeTrigger trigger : triggers) {
      makeStructureChanges(SqlUtils.createTrigger(trigger.getTable(), trigger.getName(),
          trigger.getContent(), trigger.getTiming(), trigger.getEvent(), trigger.getScope()));
    }
  }

  private BeeView getDefaultView(String tblName) {
    List<XmlColumn> columns = Lists.newArrayList();

    for (BeeField field : getTableFields(tblName)) {
      XmlColumn column = new XmlSimpleColumn();
      column.expression = field.getName();
      columns.add(column);
    }
    XmlView xmlView = new XmlView();
    xmlView.name = tblName;
    xmlView.source = tblName;
    xmlView.columns = columns;

    return new BeeView(getTable(tblName).getModuleName(), xmlView, tableCache);
  }

  private Collection<BeeState> getStates() {
    return ImmutableList.copyOf(stateCache.values());
  }

  private Collection<BeeTable> getTables() {
    return ImmutableList.copyOf(tableCache.values());
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initStates();
  }

  private void initDbTriggers() {
    for (BeeTable table : getTables()) {
      Map<String, List<String[]>> tr = Maps.newHashMap();

      for (BeeField field : table.getFields()) {
        if (field.isUnique()) {
          String relTable = field.getRelation();

          if (!BeeUtils.isEmpty(relTable) && BeeUtils.isEmpty(field.getCascade())) {
            String tblName = field.getTable();
            String fldName = field.getName();
            String relField = getIdName(relTable);

            List<String[]> entry = tr.get(tblName);

            if (BeeUtils.isEmpty(entry)) {
              entry = Lists.newArrayList();
              tr.put(tblName, entry);
            }
            entry.add(new String[] {fldName, relTable, relField});
            /*
             * <declare name="aa" value="<OLD/>.<name value="fldName"/>" /> <if> <condition> <var
             * value="aa"/> IS NOT NULL </condition> <ifTrue> <delete target="relTable"> <where>
             * <equal source="relTable" field="relField" value="" /> </where> </delete> </ifTrue>
             * </if>
             */
          }
        }
      }
      for (String tblName : tr.keySet()) {
        table.addTrigger(tblName, tr.get(tblName), "AFTER", "DELETE", "FOR EACH ROW");
      }
    }
  }

  private void initObjects(SysObject obj) {
    Assert.notEmpty(obj);

    switch (obj) {
      case STATE:
        stateCache.clear();
        break;
      case TABLE:
        tableCache.clear();
        break;
      case VIEW:
        viewCache.clear();
        break;
    }
    int cnt = 0;
    Collection<File> roots = Lists.newArrayList();

    for (String moduleName : moduleBean.getModules()) {
      roots.clear();
      String modulePath = moduleBean.getResourcePath(moduleName, obj.getPath());

      File root = new File(Config.CONFIG_DIR, modulePath);
      if (FileUtils.isDirectory(root)) {
        roots.add(root);
      }
      root = new File(Config.USER_DIR, modulePath);
      if (FileUtils.isDirectory(root)) {
        roots.add(root);
      }
      List<File> resources =
          FileUtils.findFiles(obj.getFileName("*"), roots, null, null, false, true);

      if (!BeeUtils.isEmpty(resources)) {
        Set<String> objects = Sets.newHashSet();

        for (File resource : resources) {
          String resourcePath = resource.getPath();
          String objectName = NameUtils.getBaseName(resourcePath);
          objectName = objectName.substring(0, objectName.length() - obj.name().length() - 1);
          objects.add(BeeUtils.normalize(objectName));
        }
        for (String objectName : objects) {
          boolean isOk = false;

          switch (obj) {
            case STATE:
              isOk = initState(moduleName, objectName);
              break;
            case TABLE:
              isOk = initTable(moduleName, objectName);
              break;
            case VIEW:
              isOk = initView(moduleName, objectName);
              break;
          }
          if (isOk) {
            cnt++;
          }
        }
      }
    }
    if (BeeUtils.isEmpty(cnt)) {
      LogUtils.severe(logger, "No", obj.name(), "descriptions found");
    } else {
      LogUtils.infoNow(logger, "Loaded", cnt, obj.name(), "descriptions");
    }
  }

  private boolean initState(String moduleName, String stateName) {
    Assert.notEmpty(stateName);
    BeeState state = null;
    XmlState xmlState = getXmlState(moduleName, stateName);

    if (xmlState != null) {
      if (!BeeUtils.same(xmlState.name, stateName)) {
        LogUtils.warning(logger, "State name doesn't match resource name:", xmlState.name);
      } else {
        state = new BeeState(moduleName, xmlState.name, xmlState.userMode, xmlState.roleMode,
            xmlState.checked);
      }
    }
    if (state != null) {
      register(state, stateCache);
    } else {
      unregister(stateName, stateCache);
    }
    return state != null;
  }

  private boolean initTable(String moduleName, String tableName) {
    Assert.notEmpty(tableName);
    BeeTable table = null;
    XmlTable xmlTable = getXmlTable(moduleName, tableName);

    if (xmlTable != null) {
      if (!BeeUtils.same(xmlTable.name, tableName)) {
        LogUtils.warning(logger, "Table name doesn't match resource name:", xmlTable.name);
      } else {
        table = new BeeTable(moduleName, xmlTable.name, xmlTable.idName, xmlTable.versionName);
        String tbl = table.getName();

        for (int i = 0; i < 2; i++) {
          boolean extMode = (i > 0);
          Collection<XmlField> fields = extMode ? xmlTable.extFields : xmlTable.fields;

          if (!BeeUtils.isEmpty(fields)) {
            for (XmlField field : fields) {
              String fldName = field.name;
              boolean notNull = field.notNull;

              if (table.hasField(fldName)) {
                LogUtils.warning(logger, "Dublicate field name:", tbl, fldName);
              } else {
                if (notNull && extMode) {
                  LogUtils.warning(logger, "Extendend fields must bee nullable:", tbl, fldName);
                  notNull = false;
                }
                BeeField fld = table.addField(fldName, SqlDataType.valueOf(field.type),
                    field.precision, field.scale, notNull,
                    field.unique, field.relation,
                    field.cascade == null ? null : SqlKeyword.valueOf(field.cascade))
                    .setTranslatable(field.translatable)
                    .setExtended(extMode);

                String tblName = fld.getTable();

                if (!BeeUtils.isEmpty(fld.getRelation())) {
                  table.addForeignKey(tblName, fldName, fld.getRelation(), fld.getCascade());
                }
                if (fld.isUnique()) {
                  table.addKey(true, tblName, fldName);
                }
              }
            }
          }
        }
        if (!BeeUtils.isEmpty(xmlTable.states)) {
          for (String state : xmlTable.states) {
            if (!isState(state)) {
              LogUtils.warning(logger, "Unrecognized state:", tbl, state);
            } else {
              table.addState(getState(state));
            }
          }
        }
        if (!BeeUtils.isEmpty(xmlTable.keys)) {
          for (XmlKey key : xmlTable.keys) {
            String firstTbl = null;
            String firstFld = null;
            boolean ok = !BeeUtils.isEmpty(key.fields);

            if (ok) {
              for (String fld : key.fields) {
                if (table.hasField(fld)) {
                  String keyTbl = table.getField(fld).getTable();
                  String keyFld = table.getField(fld).getName();

                  if (BeeUtils.isEmpty(firstTbl)) {
                    firstTbl = keyTbl;
                    firstFld = keyFld;

                  } else if (!BeeUtils.same(firstTbl, keyTbl)) {
                    LogUtils.warning(logger,
                        "Key expression contains fields from different sources:",
                        firstTbl + "." + firstFld, "and", keyTbl + "." + keyFld);
                    ok = false;
                    break;
                  }
                } else {
                  LogUtils.warning(logger, "Unrecognized key field:", tbl, fld);
                  ok = false;
                  break;
                }
              }
            }
            if (ok) {
              table.addKey(key.unique, firstTbl, key.fields.toArray(new String[0]));
            }
          }
        }
        // TODO: xmlTable.triggers
        if (table.isEmpty()) {
          LogUtils.warning(logger, "Table has no fields defined:", tbl);
          table = null;
        }
      }
    }
    if (table != null) {
      register(table, tableCache);
    } else {
      unregister(tableName, tableCache);
    }
    return table != null;
  }

  private boolean initView(String moduleName, String viewName) {
    Assert.notEmpty(viewName);
    BeeView view = null;
    XmlView xmlView = getXmlView(moduleName, viewName);

    if (xmlView != null) {
      if (!BeeUtils.same(xmlView.name, viewName)) {
        LogUtils.warning(logger, "View name doesn't match resource name:", xmlView.name);
      } else {
        String src = xmlView.source;

        if (!isTable(src)) {
          LogUtils.warning(logger, "Unrecognized view source:", xmlView.name, src);
        } else {
          view = new BeeView(moduleName, xmlView, tableCache);

          if (view.isEmpty()) {
            LogUtils.warning(logger, "View has no columns defined:", view.getName());
            view = null;
          }
        }
      }
    }
    if (view != null) {
      register(view, viewCache);
    } else {
      unregister(viewName, viewCache);
    }
    return view != null;
  }

  private void makeStructureChanges(IsQuery... queries) {
    Assert.notNull(queries);

    for (IsQuery query : queries) {
      if (qs.updateData(query) < 0) {
        Assert.untouchable();
      }
    }
  }

  @Lock(LockType.WRITE)
  private void rebuildTable(BeeTable table, boolean ref) {
    table.setActive(false);
    String tblMain = table.getName();
    Map<String, String> rebuilds = createTable(table, null);

    if (rebuilds.containsKey(tblMain)) {
      Collection<BeeKey> keys = Lists.newArrayList();

      for (BeeKey key : table.getKeys()) {
        if (BeeUtils.same(key.getTable(), tblMain)) {
          keys.add(key);
        }
      }
      Collection<BeeForeignKey> foreignKeys = Lists.newArrayList();

      for (BeeForeignKey fKey : table.getForeignKeys()) {
        String refTable = fKey.getRefTable();

        if ((BeeUtils.same(refTable, tblMain) && !rebuilds.containsKey(fKey.getTable()))
            || (BeeUtils.same(fKey.getTable(), tblMain)
            && (BeeUtils.same(refTable, tblMain) || getTable(refTable).isActive()))) {

          foreignKeys.add(fKey);
        }
      }
      if (ref) {
        for (BeeTable other : getTables()) {
          if (!BeeUtils.same(other.getName(), tblMain) && other.isActive()) {
            for (BeeForeignKey fKey : other.getForeignKeys()) {
              if (BeeUtils.same(fKey.getRefTable(), tblMain)) {
                foreignKeys.add(fKey);
              }
            }
          }
        }
      }
      Collection<BeeTrigger> triggers = Lists.newArrayList();

      for (BeeTrigger trigger : table.getTriggers()) {
        if (BeeUtils.same(trigger.getTable(), tblMain)) {
          triggers.add(trigger);
        }
      }
      String tblBackup = rebuilds.get(tblMain);

      if (!BeeUtils.isEmpty(tblBackup)) {
        for (Map<String, String> fKeys : qs
            .dbForeignKeys(getDbName(), getDbSchema(), null, tblMain)) {
          String fk = fKeys.get(SqlConstants.KEY_NAME);
          String tbl = fKeys.get(SqlConstants.TBL_NAME);
          makeStructureChanges(SqlUtils.dropForeignKey(tbl, fk));
        }
        makeStructureChanges(SqlUtils.dropTable(tblMain));
        makeStructureChanges(SqlUtils.renameTable(tblBackup, tblMain));
      }
      createKeys(keys);
      createForeignKeys(foreignKeys);
      createTriggers(triggers);
    }

    for (String tbl : rebuilds.keySet()) {
      if (!BeeUtils.same(tbl, tblMain)) {
        Collection<BeeKey> keys = Lists.newArrayList();

        for (BeeKey key : table.getKeys()) {
          if (BeeUtils.same(key.getTable(), tbl)) {
            keys.add(key);
          }
        }
        Collection<BeeForeignKey> foreignKeys = Lists.newArrayList();

        for (BeeForeignKey fKey : table.getForeignKeys()) {
          String refTable = fKey.getRefTable();

          if (BeeUtils.same(fKey.getTable(), tbl)
              && (BeeUtils.same(refTable, tblMain) || getTable(refTable).isActive())) {

            foreignKeys.add(fKey);
          }
        }
        Collection<BeeTrigger> triggers = Lists.newArrayList();

        for (BeeTrigger trigger : table.getTriggers()) {
          if (BeeUtils.same(trigger.getTable(), tbl)) {
            triggers.add(trigger);
          }
        }
        String tblBackup = rebuilds.get(tbl);

        if (!BeeUtils.isEmpty(tblBackup)) {
          makeStructureChanges(SqlUtils.dropTable(tbl));
          makeStructureChanges(SqlUtils.renameTable(tblBackup, tbl));
        }
        createKeys(keys);
        createForeignKeys(foreignKeys);
        createTriggers(triggers);
      }
    }
    table.setActive(true);
  }

  private <T extends BeeObject> void register(T object, Map<String, T> cache) {
    if (!BeeUtils.isEmpty(object)) {
      String name = BeeUtils.getClassName(object.getClass());
      String objectName = object.getName();
      String moduleName = BeeUtils.parenthesize(object.getModuleName());
      T existingObject = cache.get(BeeUtils.normalize(objectName));

      if (existingObject != null) {
        LogUtils.warning(logger, moduleName, "Dublicate", name, "name:",
            BeeUtils.bracket(objectName), BeeUtils.parenthesize(existingObject.getModuleName()));
      } else {
        cache.put(BeeUtils.normalize(objectName), object);
        LogUtils.info(logger, moduleName, "Registered", name, BeeUtils.bracket(objectName));
      }
    }
  }

  private void unregister(String objectName, Map<String, ? extends BeeObject> cache) {
    if (!BeeUtils.isEmpty(objectName)) {
      cache.remove(BeeUtils.normalize(objectName));
    }
  }
}
