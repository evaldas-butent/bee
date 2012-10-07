package com.butent.bee.server.data;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.primitives.Longs;

import com.butent.bee.server.Config;
import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.data.BeeTable.BeeTrigger;
import com.butent.bee.server.io.FileNameUtils;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.Defaults.DefaultExpression;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SqlConstants;
import com.butent.bee.shared.data.SqlConstants.SqlTriggerEvent;
import com.butent.bee.shared.data.SqlConstants.SqlTriggerScope;
import com.butent.bee.shared.data.SqlConstants.SqlTriggerTiming;
import com.butent.bee.shared.data.SqlConstants.SqlTriggerType;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlKey;
import com.butent.bee.shared.data.XmlTable.XmlTrigger;
import com.butent.bee.shared.data.XmlView;
import com.butent.bee.shared.data.XmlView.XmlColumn;
import com.butent.bee.shared.data.XmlView.XmlSimpleColumn;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Ensures core data management functionality containing: data structures for tables and views,
 * current SQL server configuration, creating data tables only when they are in demand, handles data
 * with exceptions etc.
 */

@Singleton
@Lock(LockType.READ)
public class SystemBean {

  /**
   * Contains a list of system objects, like state or table.
   */

  public enum SysObject {
    TABLE("tables"), VIEW("views");

    private final String path;

    private SysObject(String path) {
      this.path = path;
    }

    public String getFileName(String objName) {
      Assert.notEmpty(objName);
      return BeeUtils.join(".", objName, name().toLowerCase(), XmlUtils.DEFAULT_XML_EXTENSION);
    }

    public String getPath() {
      return path;
    }

    public String getSchemaPath() {
      return Config.getSchemaPath(name().toLowerCase() + ".xsd");
    }
  }

  public static final String AUDIT_PREFIX = "AUDIT";
  public static final String AUDIT_USER = "bee.user";
  public static final String AUDIT_FLD_TIME = "Time";
  public static final String AUDIT_FLD_USER = "UserId";
  public static final String AUDIT_FLD_TX = "TransactionId";
  public static final String AUDIT_FLD_MODE = "Mode";
  public static final String AUDIT_FLD_ID = "RecordId";
  public static final String AUDIT_FLD_FIELD = "Field";
  public static final String AUDIT_FLD_VALUE = "Value";

  private final BeeLogger logger = LogUtils.getLogger(getClass());

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
  private String dbAuditSchema;
  private final Map<String, BeeTable> tableCache = Maps.newHashMap();
  private final Map<String, BeeView> viewCache = Maps.newHashMap();
  private final EventBus viewEventBus = new EventBus();

  @Lock(LockType.WRITE)
  public void activateTable(String tblName) {
    BeeTable table = getTable(tblName);

    if (!table.isActive()) {
      rebuildTable(table);
    }
  }

  public List<Property> checkTables(String... tbls) {
    List<Property> diff = Lists.newArrayList();
    Collection<String> tables;

    if (ArrayUtils.isEmpty(tbls)) {
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

    for (BeeTable table : tables) {
      String tbl = table.getName();
      SqlSelect ss = null;

      if (allMode) {
        ss = new SqlSelect()
            .addConstant(tbl, "Table")
            .addCount("TotalRows")
            .addFrom(tbl);
      } else {
        ss = getView(tbl).getQuery();
      }
      String stateAlias = joinState(ss, tbl, null, state.getName());

      for (long roleId : usr.getRoles()) {
        String colName = state.getName() + roleId + usr.getRoleName(roleId);

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
      if (union == null) {
        union = ss;
      } else {
        union.addUnion(ss);
      }
    }
    if (union == null) {
      return ResponseObject.error("No tables support state: ", stateName);
    }
    return ResponseObject.response(qs.getViewData(union, null));
  }

  public List<DataInfo> getDataInfo() {
    SimpleRowSet dbTables = qs.dbTables(dbName, dbSchema, null);
    String[] tables = dbTables.getColumn(SqlConstants.TBL_NAME);

    List<DataInfo> lst = Lists.newArrayList();
    Set<String> viewNames = Sets.newHashSet(getViewNames());
    viewNames.addAll(getTableNames());

    for (String viewName : viewNames) {
      String sourceName = getView(viewName).getSourceName();
      DataInfo dataInfo = getDataInfo(viewName);

      for (int i = 0; i < tables.length; i++) {
        if (BeeUtils.same(tables[i], sourceName)) {
          dataInfo.setRowCount(BeeUtils.unbox(dbTables.getInt(i, SqlConstants.ROW_COUNT)));
          break;
        }
      }
      lst.add(dataInfo);
    }
    return lst;
  }

  public DataInfo getDataInfo(String viewName) {
    BeeView view = getView(viewName);
    BeeTable source = getTable(view.getSourceName());

    List<BeeColumn> columns = null;
    List<ViewColumn> viewColumns = null;

    columns = Lists.newArrayList();

    for (String col : view.getColumnNames()) {
      BeeColumn column = new BeeColumn();
      view.initColumn(col, column);
      columns.add(column);
    }
    viewColumns = view.getViewColumns();

    return new DataInfo(viewName, source.getName(), source.getIdName(), source.getVersionName(),
        view.getCaption(), view.getEditForm(),
        view.getNewRowForm(), view.getNewRowColumns(), view.getNewRowCaption(),
        view.getCacheMaximumSize(), view.getCacheEviction(), columns, viewColumns);
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

  @SuppressWarnings("unused")
  public BeeState getState(String stateName) {
    return null; // TODO
  }

  public BeeTable getTable(String tblName) {
    Assert.state(isTable(tblName), "Not a base table: " + tblName);
    return tableCache.get(BeeUtils.normalize(tblName));
  }

  public Map<String, Pair<DefaultExpression, Object>> getTableDefaults(String tblName) {
    return getTable(tblName).getDefaults();
  }

  public Collection<BeeField> getTableFields(String tblName) {
    return getTable(tblName).getFields();
  }

  public List<ExtendedProperty> getTableInfo(String tblName) {
    return getTable(tblName).getExtendedInfo();
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

    if (view == null) {
      view = getDefaultView(viewName);
      register(view, viewCache);
    }
    return view;
  }

  public Collection<String> getViewNames() {
    Collection<String> views = Lists.newArrayList();

    for (BeeView view : viewCache.values()) {
      views.add(view.getName());
    }
    return views;
  }

  public String getViewSource(String viewName) {
    return getView(viewName).getSourceName();
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
  public void initTables() {
    initTables(BeeUtils.notEmpty(SqlBuilderFactory.getDsn(), dsb.getDefaultDsn()));
  }

  @Lock(LockType.WRITE)
  public void initTables(String dsn) {
    Assert.state(SqlBuilderFactory.setDefaultBuilder(qs.dbEngine(dsn), dsn));
    initObjects(SysObject.TABLE);

    for (BeeTable table : getTables()) {
      for (BeeForeignKey fKey : table.getForeignKeys()) {
        Assert.state(isTable(fKey.getRefTable()),
            BeeUtils.joinWords(
                "Unknown field", BeeUtils.bracket(table.getName() + "." + fKey.getKeyField()),
                "relation:", BeeUtils.bracket(fKey.getRefTable())));
      }
    }
    initDatabase();
    initDbTriggers();
    initViews();
  }

  @Lock(LockType.WRITE)
  public void initViews() {
    initObjects(SysObject.VIEW);
  }

  public boolean isExtField(String tblName, String fldName) {
    return getTable(tblName).getField(fldName).isExtended();
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
      logger.warning("Field is not extended:", tblName, fldName);
      return null;
    }
    return table.joinExtField(query, tblAlias, field);
  }

  public String joinState(HasFrom<?> query, String tblName, String tblAlias, String stateName) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeState state = getState(stateName);

    if (!table.hasState(state)) {
      logger.warning("State not registered:", tblName, stateName);
      return null;
    }
    return table.joinState(query, tblAlias, state);
  }

  public IsCondition joinTables(String dst, String src, String fld) {
    return SqlUtils.join(dst, getIdName(dst), src, fld);
  }

  public String joinTranslationField(HasFrom<?> query, String tblName, String tblAlias,
      String fldName, String locale) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeField field = table.getField(fldName);

    return table.joinTranslationField(query, tblAlias, field, locale);
  }

  public XmlTable loadXmlTable(String resource) {
    return XmlUtils.unmarshal(XmlTable.class, resource, SysObject.TABLE.getSchemaPath());
  }

  public XmlView loadXmlView(String resource) {
    return XmlUtils.unmarshal(XmlView.class, resource, SysObject.VIEW.getSchemaPath());
  }

  public void postViewEvent(ViewEvent viewEvent) {
    viewEventBus.post(viewEvent);
  }

  @Lock(LockType.WRITE)
  public void rebuildActiveTables() {
    initTables();

    for (BeeTable table : getTables()) {
      if (table.isActive()) {
        rebuildTable(table);
      }
    }
  }

  @Lock(LockType.WRITE)
  public void rebuildTable(String tblName) {
    rebuildTable(getTable(tblName));
  }

  @Lock(LockType.WRITE)
  public void registerViewEventHandler(ViewEventHandler eventHandler) {
    viewEventBus.register(eventHandler);
  }

  public SqlSelect verifyStates(SqlSelect query, String tblName, String tblAlias, String... states) {
    Assert.notNull(query);

    long userId = usr.getCurrentUserId();
    long[] userRoles = usr.getUserRoles(userId);

    if (userRoles == null || userRoles.length == 0) {
      return query.setWhere(SqlUtils.sqlFalse());
    }
    long[] bits = Longs.concat(new long[] {-userId}, userRoles);

    for (String stateName : states) {
      BeeTable table = getTable(tblName);
      BeeState state = getState(stateName);

      if (!table.hasState(state)) {
        logger.warning("State not registered:", tblName, stateName);
      } else {
        table.verifyState(query, tblAlias, state, bits);
      }
    }
    return query;
  }

  private void createAuditTables(BeeTable table) {
    if (!table.isAuditable()) {
      return;
    }
    if (!qs.dbSchemaExists(dbName, dbAuditSchema)) {
      makeStructureChanges(SqlUtils.createSchema(dbAuditSchema));
    }
    String auditName = BeeUtils.join("_", table.getName(), AUDIT_PREFIX);
    String auditPath = BeeUtils.join(".", dbAuditSchema, auditName);

    if (!qs.dbTableExists(dbName, dbAuditSchema, auditName)) {
      makeStructureChanges(
          new SqlCreate(auditPath, false)
              .addDateTime(AUDIT_FLD_TIME, true)
              .addLong(AUDIT_FLD_USER, false)
              .addLong(AUDIT_FLD_TX, false)
              .addString(AUDIT_FLD_MODE, 1, true)
              .addLong(AUDIT_FLD_ID, true)
              .addString(AUDIT_FLD_FIELD, 30, false)
              .addText(AUDIT_FLD_VALUE, false),
          SqlUtils.createIndex(false,
              auditPath, "IK_" + Codec.crc32(auditName + AUDIT_FLD_ID), AUDIT_FLD_ID));
    }
  }

  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    HashMultimap<String, String> flds = HashMultimap.create();

    for (Map<String, String> row : qs.dbFields(getDbName(), getDbSchema(), null)) {
      flds.put(row.get(SqlConstants.TBL_NAME), row.get(SqlConstants.FLD_NAME));
    }
    for (BeeForeignKey fKey : fKeys) {
      String tblName = fKey.getTable();
      String fldName = fKey.getKeyField();
      String refTblName = fKey.getRefTable();

      if (flds.containsEntry(tblName, fldName)) {
        makeStructureChanges(SqlUtils.createForeignKey(tblName, fKey.getName(),
            fldName, refTblName, getIdName(refTblName), fKey.getCascade()));
      }
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
      tblName = field.getStorageTable();

      if (field.isExtended()) {
        SqlCreate sc = table.createExtTable(newTables.get(tblName), field);

        if (sc != null) {
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

        if (sc != null) {
          newTables.put(tblName, sc);
        }
      }
    }
    for (BeeState state : table.getStates()) {
      tblName = table.getStateTable(state);
      SqlCreate sc = table.createStateTable(newTables.get(tblName), state);

      if (sc != null) {
        newTables.put(tblName, sc);
      }
    }
    Map<String, String> rebuilds = Maps.newHashMap();

    for (SqlCreate sc : newTables.values()) {
      tblName = sc.getTarget();
      String tblBackup = null;
      boolean update = !qs.dbTableExists(getDbName(), getDbSchema(), tblName);

      if (update) {
        if (diff != null) {
          PropertyUtils.addProperty(diff, tblName, "DOES NOT EXIST");
          return null;
        } else {
          makeStructureChanges(sc);
        }
      } else {
        tblBackup = tblName + "_BAK";
        logger.info("Checking indexes...");
        int c = 0;
        String[] keys = qs.dbIndexes(getDbName(), getDbSchema(), tblName)
            .getColumn(SqlConstants.KEY_NAME);

        for (BeeKey key : table.getKeys()) {
          if (BeeUtils.same(key.getTable(), tblName)) {
            if (ArrayUtils.contains(keys, key.getName())) {
              c++;
            } else {
              String msg = BeeUtils.joinWords("INDEX", key.getName(), key.getKeyFields(), "NOT IN",
                  keys);
              logger.warning(msg);

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
          logger.warning(msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!update) {
        logger.info("Checking foreign keys...");
        int c = 0;
        String[] fKeys = qs.dbForeignKeys(getDbName(), getDbSchema(), tblName, null)
            .getColumn(SqlConstants.KEY_NAME);

        for (BeeForeignKey fKey : table.getForeignKeys()) {
          if (BeeUtils.same(fKey.getTable(), tblName)
              && (BeeUtils.same(fKey.getRefTable(), table.getName())
              || getTable(fKey.getRefTable()).isActive())) {

            if (ArrayUtils.contains(fKeys, fKey.getName())) {
              c++;
            } else {
              String msg = BeeUtils.joinWords("FOREIGN KEY", fKey.getName(),
                  BeeUtils.parenthesize(BeeUtils.join(" ON DELETE ",
                      fKey.getKeyField() + "->" + fKey.getRefTable(), fKey.getCascade())),
                  "NOT IN", fKeys);
              logger.warning(msg);

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
          logger.warning(msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!update) {
        logger.info("Checking triggers...");
        int c = 0;
        Set<String> triggers = Sets.newHashSet(qs.dbTriggers(getDbName(), getDbSchema(), tblName)
            .getColumn(SqlConstants.TRIGGER_NAME));

        for (BeeTrigger trigger : table.getTriggers()) {
          if (BeeUtils.same(trigger.getTable(), tblName)) {
            if (triggers.contains(trigger.getName())) {
              c++;
            } else {
              String msg = BeeUtils.joinWords("TRIGGER", trigger.getName(), "NOT IN",
                  BeeUtils.parenthesize(triggers));
              logger.warning(msg);

              if (diff != null) {
                PropertyUtils.addProperty(diff, tblName, msg);
              } else {
                update = true;
                break;
              }
            }
          }
        }
        if (!update && triggers.size() > c) {
          String msg = "TOO MANY TRIGGERS";
          logger.warning(msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!update && table.isAuditable() && isTable(tblName)) {
        logger.info("Checking audit tables...");
        String auditName = BeeUtils.join("_", tblName, AUDIT_PREFIX);

        if (!qs.dbTableExists(dbName, dbAuditSchema, auditName)) {
          String msg = BeeUtils.joinWords("AUDIT TABLE",
              BeeUtils.join(".", dbAuditSchema, auditName), "DOES NOT EXIST");
          logger.warning(msg);

          if (diff != null) {
            PropertyUtils.addProperty(diff, tblName, msg);
          } else {
            update = true;
          }
        }
      }
      if (!BeeUtils.isEmpty(tblBackup)) {
        if (qs.dbTableExists(getDbName(), getDbSchema(), tblBackup)) {
          makeStructureChanges(SqlUtils.dropTable(tblBackup));
        }
        makeStructureChanges(sc.setTarget(tblBackup));

        SimpleRowSet oldFields = qs.dbFields(getDbName(), getDbSchema(), tblName);
        SimpleRowSet newFields = qs.dbFields(getDbName(), getDbSchema(), tblBackup);

        if (!update) {
          logger.info("Checking fields...");
          int c = 0;

          for (Map<String, String> newFieldInfo : newFields) {
            Map<String, String> oldFieldInfo = null;
            String fldName = newFieldInfo.get(SqlConstants.FLD_NAME);

            for (Map<String, String> oldInfo : oldFields) {
              if (BeeUtils.same(oldInfo.get(SqlConstants.FLD_NAME), fldName)) {
                c++;
                oldFieldInfo = oldInfo;
                break;
              }
            }
            if (oldFieldInfo != null) {
              for (String info : oldFieldInfo.keySet()) {
                if (!BeeUtils.same(info, SqlConstants.TBL_NAME)
                    && !Objects.equal(oldFieldInfo.get(info), newFieldInfo.get(info))) {

                  String msg = BeeUtils.joinWords("FIELD", fldName + ":",
                      info, oldFieldInfo.get(info), "!=", newFieldInfo.get(info));
                  logger.warning(msg);

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
            } else {
              String msg = BeeUtils.joinWords("FIELD", fldName, "DOES NOT EXIST");
              logger.warning(msg);

              if (diff != null) {
                PropertyUtils.addProperty(diff, tblName, msg);
              } else {
                update = true;
                break;
              }
            }
          }
          if (!update && oldFields.getNumberOfRows() > c) {
            String msg = "TOO MANY FIELDS";
            logger.warning(msg);

            if (diff != null) {
              PropertyUtils.addProperty(diff, tblName, msg);
            } else {
              update = true;
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
      makeStructureChanges(SqlUtils.createTrigger(trigger.getName(), trigger.getTable(),
          trigger.getType(), trigger.getParameters(), trigger.getTiming(), trigger.getEvents(),
          trigger.getScope()));
    }
  }

  private BeeView getDefaultView(String tblName) {
    List<XmlColumn> columns = Lists.newArrayList();

    for (BeeField field : getTableFields(tblName)) {
      XmlColumn column = new XmlSimpleColumn();
      column.name = field.getName();
      columns.add(column);
    }
    XmlView xmlView = new XmlView();
    xmlView.name = tblName;
    xmlView.source = tblName;
    xmlView.columns = columns;

    return new BeeView(getTable(tblName).getModuleName(), xmlView, tableCache);
  }

  private Collection<BeeTable> getTables() {
    return ImmutableList.copyOf(tableCache.values());
  }

  @PostConstruct
  private void init() {
    initTables();
  }

  private void initDatabase() {
    dbName = qs.dbName();
    dbSchema = qs.dbSchema();
    dbAuditSchema = BeeUtils.join("_", dbSchema, AUDIT_PREFIX);

    String[] dbTables = qs.dbTables(dbName, dbSchema, null).getColumn(SqlConstants.TBL_NAME);
    Set<String> names = Sets.newHashSet();
    for (String name : dbTables) {
      names.add(BeeUtils.normalize(name));
    }

    for (BeeTable table : getTables()) {
      String tblName = table.getName();
      table.setActive(names.contains(BeeUtils.normalize(tblName)));

      Map<String, String[]> tableFields = Maps.newHashMap();

      for (BeeState state : table.getStates()) {
        tblName = table.getStateTable(state);

        if (names.contains(BeeUtils.normalize(tblName)) && !tableFields.containsKey(tblName)) {
          tableFields.put(tblName,
              qs.dbFields(getDbName(), getDbSchema(), tblName).getColumn(SqlConstants.FLD_NAME));
        }
        table.setStateActive(state, tableFields.get(tblName));
      }
    }
  }

  private void initDbTriggers() {
    for (BeeTable table : getTables()) {
      Map<String, List<Map<String, String>>> tr = Maps.newHashMap();

      for (BeeField field : table.getFields()) {
        if (field instanceof BeeRelation && ((BeeRelation) field).hasEditableRelation()) {
          String tblName = field.getStorageTable();
          String relTable = ((BeeRelation) field).getRelation();

          List<Map<String, String>> entry = tr.get(tblName);

          if (BeeUtils.isEmpty(entry)) {
            entry = Lists.newArrayList();
            tr.put(tblName, entry);
          }
          entry.add(ImmutableMap.of("field", field.getName(),
              "relTable", relTable, "relField", getIdName(relTable)));
        }
      }
      for (String tblName : tr.keySet()) {
        table.addTrigger(tblName, SqlTriggerType.RELATION,
            ImmutableMap.of("fields", tr.get(tblName)),
            SqlTriggerTiming.AFTER, EnumSet.of(SqlTriggerEvent.DELETE), SqlTriggerScope.ROW);
      }

      if (table.isAuditable()) {
        HashMultimap<String, String> fields = HashMultimap.create();

        for (BeeField field : table.getFields()) {
          fields.put(field.getStorageTable(), field.getName());
        }
        for (String tblName : fields.keySet()) {
          table.addTrigger(tblName, SqlTriggerType.AUDIT,
              ImmutableMap.of("auditSchema", dbAuditSchema,
                  "auditTable", BeeUtils.join("_", table.getName(), AUDIT_PREFIX),
                  "idName", table.getIdName(),
                  "fields", fields.get(tblName)),
              SqlTriggerTiming.AFTER,
              EnumSet.of(SqlTriggerEvent.INSERT, SqlTriggerEvent.UPDATE, SqlTriggerEvent.DELETE),
              SqlTriggerScope.ROW);
        }
      }
    }
  }

  private void initObjects(SysObject obj) {
    Assert.notNull(obj);

    switch (obj) {
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
          String objectName = FileNameUtils.getBaseName(resourcePath);
          objectName = objectName.substring(0, objectName.length() - obj.name().length() - 1);
          objects.add(objectName);
        }
        for (String objectName : objects) {
          boolean isOk = false;

          switch (obj) {
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
    if (cnt <= 0) {
      logger.severe("No", obj.name(), "descriptions found");
    } else {
      logger.info("Loaded", cnt, obj.name(), "descriptions");
    }
  }

  private boolean initTable(String moduleName, String tableName) {
    Assert.notEmpty(tableName);
    BeeTable table = null;
    XmlTable xmlTable = getXmlTable(moduleName, tableName);

    if (xmlTable != null) {
      if (!BeeUtils.same(xmlTable.name, tableName)) {
        logger.warning("Table name doesn't match resource name:", xmlTable.name);
      } else {
        table = new BeeTable(moduleName,
            xmlTable.name, xmlTable.idName, xmlTable.versionName, xmlTable.audit);
        String tbl = table.getName();

        for (int i = 0; i < 2; i++) {
          boolean extMode = (i > 0);
          Collection<XmlField> fields = extMode ? xmlTable.extFields : xmlTable.fields;

          if (!BeeUtils.isEmpty(fields)) {
            for (XmlField field : fields) {
              table.addField(field, extMode);
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
                  String keyTbl = table.getField(fld).getStorageTable();
                  String keyFld = table.getField(fld).getName();

                  if (BeeUtils.isEmpty(firstTbl)) {
                    firstTbl = keyTbl;
                    firstFld = keyFld;

                  } else if (!BeeUtils.same(firstTbl, keyTbl)) {
                    logger.warning("Key expression contains fields from different sources:",
                        firstTbl + "." + firstFld, "and", keyTbl + "." + keyFld);
                    ok = false;
                    break;
                  }
                } else {
                  logger.warning("Unrecognized key field:", tbl, fld);
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
        if (!BeeUtils.isEmpty(xmlTable.triggers)) {
          for (XmlTrigger trigger : xmlTable.triggers) {
            String body = null;
            List<SqlTriggerEvent> events = Lists.newArrayList();

            for (String event : trigger.events) {
              events.add(NameUtils.getEnumByName(SqlTriggerEvent.class, event));
            }
            switch (SqlBuilderFactory.getBuilder().getEngine()) {
              case POSTGRESQL:
                body = trigger.postgreSql;
                break;
              case MSSQL:
                body = trigger.msSql;
                break;
              case ORACLE:
                body = trigger.oracle;
                break;
              case GENERIC:
                body = null;
                break;
            }
            if (!BeeUtils.isEmpty(body)) {
              table.addTrigger(tableName, SqlTriggerType.CUSTOM,
                  ImmutableMap.of("body", body),
                  NameUtils.getEnumByName(SqlTriggerTiming.class, trigger.timing),
                  EnumSet.copyOf(events),
                  NameUtils.getEnumByName(SqlTriggerScope.class, trigger.scope));
            }
          }
        }
        if (table.isEmpty()) {
          logger.warning("Table has no fields defined:", tbl);
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
        logger.warning("View name doesn't match resource name:", xmlView.name);
      } else {
        String src = xmlView.source;

        if (!isTable(src)) {
          logger.warning("Unrecognized view source:", xmlView.name, src);
        } else {
          view = new BeeView(moduleName, xmlView, tableCache);

          if (view.isEmpty()) {
            logger.warning("View has no columns defined:", view.getName());
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

  private void rebuildTable(BeeTable table) {
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
      for (BeeTable other : getTables()) {
        if (!BeeUtils.same(other.getName(), tblMain) && other.isActive()) {
          for (BeeForeignKey fKey : other.getForeignKeys()) {
            if (BeeUtils.same(fKey.getRefTable(), tblMain)) {
              foreignKeys.add(fKey);
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
      createTriggers(triggers);
      createForeignKeys(foreignKeys);
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
          makeStructureChanges(SqlUtils.dropTable(tbl), SqlUtils.renameTable(tblBackup, tbl));
        }
        createKeys(keys);
        createTriggers(triggers);
        createForeignKeys(foreignKeys);
      }
    }
    createAuditTables(table);

    table.setActive(true);
  }

  private <T extends BeeObject> void register(T object, Map<String, T> cache) {
    if (object != null) {
      String name = NameUtils.getClassName(object.getClass());
      String objectName = object.getName();
      String moduleName = BeeUtils.parenthesize(object.getModuleName());
      T existingObject = cache.get(BeeUtils.normalize(objectName));

      if (existingObject != null) {
        logger.warning(moduleName, "Dublicate", name, "name:",
            BeeUtils.bracket(objectName), BeeUtils.parenthesize(existingObject.getModuleName()));
      } else {
        cache.put(BeeUtils.normalize(objectName), object);
        logger.debug(moduleName, "Registered", name, BeeUtils.bracket(objectName));
      }
    }
  }

  private void unregister(String objectName, Map<String, ? extends BeeObject> cache) {
    if (!BeeUtils.isEmpty(objectName)) {
      cache.remove(BeeUtils.normalize(objectName));
    }
  }
}
