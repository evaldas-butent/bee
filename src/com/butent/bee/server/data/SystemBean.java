package com.butent.bee.server.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.data.BeeTable.BeeState;
import com.butent.bee.server.utils.FileUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.sql.BeeConstants.DataTypes;
import com.butent.bee.shared.sql.BeeConstants.Keywords;
import com.butent.bee.shared.sql.HasFrom;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.IsExpression;
import com.butent.bee.shared.sql.IsQuery;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlCreate;
import com.butent.bee.shared.sql.SqlDelete;
import com.butent.bee.shared.sql.SqlInsert;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUpdate;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

@Singleton
@Startup
@Lock(LockType.READ)
public class SystemBean {
  public static final String STRUCTURE_SCHEMA = "structure.xsd";
  public static final String VIEW_SCHEMA = "views.xsd";

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  private String dbName;
  private String dbSchema;
  private Map<String, BeeTable> dataCache = Maps.newHashMap();
  private Map<String, BeeView> viewCache = Maps.newHashMap();

  public void activateTable(String tableName) {
    BeeTable table = getTable(tableName);

    if (!table.isActive()) {
      if (!qs.isDbTable(getDbName(), getDbSchema(), tableName)) {
        rebuildTable(table, false);
      } else {
        table.setActive(true);
      }
    }
  }

  public String backupTable(String name) {
    String tmp = null;
    int rc = qs.getSingleRow(new SqlSelect().addCount(name).addFrom(name)).getInt(0);

    if (rc > 0) {
      tmp = SqlUtils.temporaryName();

      qs.updateData(new SqlCreate(tmp)
        .setDataSource(new SqlSelect().addAllFields(name).addFrom(name)));
    }
    return tmp;
  }

  public boolean commitChanges(BeeRowSet changes, ResponseBuffer buff) {
    String err = "";
    int c = 0;
    int idIndex = -1;
    int lockIndex = -1;

    BeeTable table = null;
    String tblName = null;

    BeeView view = getView(changes.getViewName());
    Map<Integer, BeeField> fields = Maps.newHashMap();

    if (!BeeUtils.isEmpty(view)) {
      tblName = view.getSource();
      idIndex = view.getIdIndex();
      lockIndex = view.getLockIndex();
    }
    if (isTable(tblName)) {
      table = getTable(tblName);

      if (idIndex < 0) {
        err = "Cannot update table " + tblName + " (Unknown ID index).";
      } else {
        int i = 0;

        for (String fld : view.getFields().values()) {
          BeeField field = table.getField(fld);

          if (!BeeUtils.isEmpty(field)) {
            fields.put(i, field);
          }
          i++;
        }
      }
    } else {
      err = "Cannot update table (Unknown source " + tblName + ").";
    }
    for (BeeRow row : changes.getRows()) {
      if (!BeeUtils.isEmpty(err)) {
        break;
      }
      List<Object[]> baseUpdate = Lists.newArrayList();
      List<Object[]> extUpdate = Lists.newArrayList();

      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Integer col : row.getShadow().keySet()) {
          BeeField field = fields.get(col);

          if (BeeUtils.isEmpty(field)) {
            err = "Cannot update column " + changes.getColumnLabel(col) + " (Unknown source).";
            break;
          }
          Object[] entry = new Object[]{
              field.getName(), changes.getOriginal(row, col), row.getShadow().get(col)};

          if (field.isExtended()) {
            extUpdate.add(entry);
          } else {
            baseUpdate.add(entry);
          }
        }
        if (!BeeUtils.isEmpty(err)) {
          break;
        }
      }
      long id = row.getLong(idIndex);
      IsCondition idWh = SqlUtils.equal(tblName, table.getIdName(), id);
      IsCondition wh = idWh;

      if (lockIndex >= 0) {
        wh = SqlUtils.and(wh, SqlUtils.equal(tblName, table.getLockName(), row.getLong(lockIndex)));
      }
      if (row.markedForDelete()) { // DELETE
        int res = qs.updateData(new SqlDelete(tblName).setWhere(wh));

        if (res > 0) {
          c += res;
        } else {
          err = (res < 0) ? "Error deleting data" : "Optimistic lock exception";
          break;
        }

      } else if (row.markedForInsert()) { // INSERT
        SqlInsert si = new SqlInsert(tblName);

        for (Object[] entry : baseUpdate) {
          si.addConstant((String) entry[0], entry[1]);
        }
        if (lockIndex >= 0) {
          long lock = System.currentTimeMillis();
          si.addConstant(table.getLockName(), lock);
          row.setValue(lockIndex, BeeUtils.transform(lock));
        }
        id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
          break;
        }
        c++;
        row.setValue(idIndex, BeeUtils.transform(id));

        if (!BeeUtils.isEmpty(extUpdate)) {
          int res = commitExtChanges(table, id, extUpdate, false);

          if (res < 0) {
            err = "Error inserting extended fields";
            break;
          }
          c += res;
        }

      } else { // UPDATE
        if (!BeeUtils.isEmpty(baseUpdate)) {
          SqlUpdate su = new SqlUpdate(tblName);

          for (Object[] entry : baseUpdate) {
            su.addConstant((String) entry[0], entry[1]);
          }
          if (lockIndex >= 0) {
            long lock = System.currentTimeMillis();
            su.addConstant(table.getLockName(), lock);
            row.setValue(lockIndex, BeeUtils.transform(lock));
          }
          int res = qs.updateData(su.setWhere(wh));

          if (res == 0) { // Optimistic lock exception
            BeeRowSet rs = getViewData(view, idWh, 0, 0);

            if (!rs.isEmpty()) {
              BeeRow r = rs.getRow(0);
              boolean collision = false;

              for (Object[] entry : baseUpdate) {
                String colName = (String) entry[0];

                if (!BeeUtils.equals(
                    BeeUtils.transformNoTrim(rs.getOriginal(r, colName)),
                    BeeUtils.transformNoTrim(entry[2]))) {
                  collision = true;
                  break;
                }
                rs.setValue(r, colName, changes.getString(row, colName));
              }
              if (!collision) {
                if (lockIndex >= 0) {
                  r.setValue(lockIndex, row.getValue(lockIndex));
                }
                row.setValues(r.getValues());

                res = qs.updateData(su.setWhere(idWh));
              }
            }
          }
          if (res > 0) {
            c += res;
          } else {
            err = (res < 0) ? "Error updating data" : "Optimistic lock exception";
            break;
          }
        }
        if (!BeeUtils.isEmpty(extUpdate)) {
          int res = commitExtChanges(table, id, extUpdate, true);

          if (res < 0) {
            err = "Error updating extended fields";
            break;
          }
          c += res;
        }
      }
    }
    if (!BeeUtils.isEmpty(err)) {
      buff.add(-1);
      buff.add(err);
      return false;
    }
    buff.add(c);
    buff.add(changes.serialize());

    if (BeeUtils.inList(tblName, UserServiceBean.USER_TABLE, UserServiceBean.ROLE_TABLE,
        UserServiceBean.USER_ROLES_TABLE)) {
      usr.invalidateCache();
    }
    return true;
  }

  public int commitExtChanges(BeeTable table, long id, List<Object[]> extUpdate, boolean updateMode) {
    int c = 0;
    Map<String, List<IsQuery[]>> queryMap = Maps.newHashMap();

    for (Object[] entry : extUpdate) {
      BeeField field = table.getField((String) entry[0]);
      String extName = field.getTable();

      if (!queryMap.containsKey(extName)) {
        List<IsQuery[]> queries = Lists.newArrayList();
        queryMap.put(extName, queries);
      }
      List<IsQuery[]> queries = queryMap.get(extName);
      IsQuery[] arr = new IsQuery[2];
      IsQuery mainQuery = null;

      if (!BeeUtils.isEmpty(queries)) {
        mainQuery = queries.get(0)[0];
        arr[1] = queries.get(0)[1];
      }
      arr[0] = table.insertExtField((SqlInsert) mainQuery, id, field, entry[1]);

      if (arr[0] != mainQuery) {
        queries.add(arr);
      }
      if (updateMode) {
        arr[1] = table.updateExtField((SqlUpdate) arr[1], id, field, entry[1]);
      }
    }
    for (List<IsQuery[]> queries : queryMap.values()) {
      for (IsQuery[] query : queries) {
        int res = 0;
        IsQuery insQuery = query[0];
        IsQuery updQuery = query[1];

        if (!BeeUtils.isEmpty(updQuery)) {
          res = qs.updateData(updQuery);
        }
        if (res == 0) {
          res = qs.updateData(insQuery);
        }
        if (res < 0) {
          return -1;
        }
        c += res;
      }
    }
    return c;
  }

  public BeeRowSet editStateRoles(String tblName, String stateName) {
    boolean allMode = BeeUtils.isEmpty(tblName);
    Collection<BeeState> states = Lists.newArrayList();

    if (allMode) {
      for (BeeTable table : getTables()) {
        BeeState state = table.getState(stateName);

        if (!BeeUtils.isEmpty(state) && state.supportsRoles()) {
          states.add(state);
        }
      }
    } else {
      BeeState state = getTable(tblName).getState(stateName);
      if (!BeeUtils.isEmpty(state) && state.supportsRoles()) {
        states.add(state);
      }
    }
    SqlSelect union = null;
    Map<Integer, String> roles = usr.getRoles();

    for (BeeState state : states) {
      String tbl = state.getOwner();
      SqlSelect ss = null;

      if (allMode) {
        ss = new SqlSelect()
          .addConstant(tbl, "Table")
          .addCount("TotalRows")
          .addFrom(tbl);
      } else {
        ss = getViewQuery(getView(tbl), null);
      }
      String stateAlias = joinState(ss, tbl, null, state.getName());

      for (Entry<Integer, String> role : roles.entrySet()) {
        int roleId = role.getKey();
        String colName = state.getName() + roleId + role.getValue();

        if (BeeUtils.isEmpty(stateAlias)) {
          if (allMode && state.isChecked()) {
            ss.addCount(colName);
          } else {
            ss.addConstant(BeeUtils.toInt(state.isChecked()), colName);
          }
        } else {
          IsExpression expr = SqlUtils.sqlIf(
              getTable(tbl).checkState(stateAlias, state, true, roleId), 1, 0);

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
      union = new SqlSelect()
        .addMax(SqlUtils.constant("State \"" + stateName + "\" does not support roles"), "Error")
        .addFrom("Roles");
    }
    return qs.getData(union);
  }

  public String getDbName() {
    return dbName;
  }

  public String getDbSchema() {
    return dbSchema;
  }

  public String getIdName(String table) {
    return getTable(table).getIdName();
  }

  public String getLockName(String table) {
    return getTable(table).getLockName();
  }

  public Collection<String> getTableFields(String table) {
    Collection<String> fields = Lists.newArrayList();

    for (BeeField field : getTable(table).getFields()) {
      fields.add(field.getName());
    }
    return fields;
  }

  public Collection<String> getTableNames() {
    return ImmutableSet.copyOf(dataCache.keySet());
  }

  public Collection<String> getTableStates(String table) {
    Collection<String> states = Lists.newArrayList();

    for (BeeState state : getTable(table).getStates()) {
      states.add(state.getName());
    }
    return states;
  }

  public BeeRowSet getViewData(String viewName, int limit, int offset, String states) {
    return getViewData(getView(viewName), null, limit, offset,
        BeeUtils.isEmpty(states) ? null : states.split(" "));
  }

  @PostConstruct
  public void init() {
    initTables();
    initExtensions();
    initViews();
    initDatabase(BeeUtils.ifString(Config.getProperty("DefaultEngine"), BeeConst.MYSQL));
  }

  public void initDatabase(String engine) {
    SqlBuilderFactory.setDefaultEngine(BeeConst.getDsType(engine));
    dbName = qs.dbName();
    dbSchema = qs.dbSchema();
    String[] dbTables = qs.dbTables(dbName, dbSchema, null);

    for (BeeTable table : getTables()) {
      table.setActive(BeeUtils.inListSame(table.getName(), dbTables));

      Map<String, String[]> stateTables = new HashMap<String, String[]>();

      for (BeeState state : table.getStates()) {
        String tblName = state.getTable();
        boolean active = false;

        if (BeeUtils.inListSame(tblName, dbTables)) {
          if (!stateTables.containsKey(tblName)) {
            stateTables.put(tblName, qs.dbFields(tblName));
          }
          String[] dbFields = stateTables.get(tblName);

          for (String fld : dbFields) {
            if (BeeUtils.startsSame(fld, table.getStateField(state.getName()))) {
              active = true;
              break;
            }
          }
        }
        state.setActive(active);
      }
    }
    usr.invalidateCache();
  }

  @Lock(LockType.WRITE)
  public void initExtensions() {
    String resource = Config.getPath("extensions.xml");

    Collection<BeeTable> extensions = loadTables(resource, Config.getPath(STRUCTURE_SCHEMA));

    if (BeeUtils.isEmpty(extensions)) {
      return;
    }
    int cNew = 0;
    int cUpd = 0;

    for (BeeTable extension : extensions) {
      String tblName = extension.getName();

      if (!isTable(tblName) || getTable(tblName).isCustom()) {
        if (extension.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", tblName, "has no fields defined");
          continue;
        }
        extension.setCustom();
        registerTable(extension);
        cNew++;
      } else {
        if (getTable(tblName).applyChanges(extension) > 0) {
          cUpd++;
        }
      }
    }
    LogUtils.infoNow(logger, "Loaded", cNew, "new tables, updated", cUpd,
        "existing tables descriptions from", resource);
  }

  @Lock(LockType.WRITE)
  public void initViews() {
    String resource = Config.getPath("views.xml");

    Collection<BeeView> views = loadViews(resource, Config.getPath(VIEW_SCHEMA));

    if (BeeUtils.isEmpty(views)) {
      LogUtils.warning(logger, resource, "No views defined");
      return;
    }
    viewCache.clear();

    for (BeeView view : views) {
      String viewName = view.getName();

      if (view.isEmpty()) {
        LogUtils.warning(logger, resource, "View", viewName, "has no columns defined");
      } else if (isView(viewName)) {
        LogUtils.warning(logger, resource, "Dublicate view name:", viewName);
      } else {
        registerView(view);
      }
    }
    LogUtils.infoNow(logger, "Loaded", viewCache.size(), "views descriptions from", resource);
  }

  public boolean isTable(String tableName) {
    return dataCache.containsKey(tableName);
  }

  public boolean isView(String viewName) {
    return viewCache.containsKey(viewName);
  }

  public String joinExtField(HasFrom<?> query, String tbl, String tblAlias, String fld) {
    Assert.notNull(query);
    BeeTable table = getTable(tbl);
    BeeField field = table.getField(fld);

    if (BeeUtils.isEmpty(field)) {
      LogUtils.warning(logger, "Not an extended field:", tbl, fld);
      return null;
    }
    if (field.isExtended()) {
      return table.joinExtField(query, tblAlias, field);
    }
    return BeeUtils.ifString(tblAlias, tbl);
  }

  public String joinState(HasFrom<?> query, String tbl, String tblAlias, String stateName) {
    Assert.notNull(query);
    BeeTable table = getTable(tbl);
    BeeState state = table.getState(stateName);

    if (BeeUtils.isEmpty(state)) {
      LogUtils.warning(logger, "State not registered:", tbl, stateName);
      return null;
    }
    return table.joinState(query, tblAlias, state);
  }

  public void rebuildTable(String tableName) {
    rebuildTable(getTable(tableName), qs.isDbTable(getDbName(), getDbSchema(), tableName));
  }

  @Lock(LockType.WRITE)
  public void rebuildTables(ResponseBuffer buff) {
    for (String tbl : getTableNames()) {
      rebuildTable(tbl);
    }
    buff.add("Recreate structure OK");
  }

  public int restoreTable(String tbl, String tmp) {
    int rc = 0;

    if (!BeeUtils.isEmpty(tmp)) {
      String[] tmpFields = qs.dbFields(tmp);
      Collection<String> fldList = Lists.newArrayList();

      for (String fld : qs.dbFields(tbl)) {
        if (BeeUtils.inListSame(fld, tmpFields)) {
          fldList.add(fld);
        }
      }
      if (!BeeUtils.isEmpty(fldList)) {
        String[] flds = fldList.toArray(new String[0]);

        rc = qs.updateData(new SqlInsert(tbl)
          .addFields(flds)
          .setDataSource(new SqlSelect().addFields(tmp, flds).addFrom(tmp)));
      }
      qs.updateData(SqlUtils.dropTable(tmp));
    }
    return rc;
  }

  public void setState(String tbl, long id, String stateName, boolean mdRole, int... bits) {
    BeeTable table = getTable(tbl);
    BeeState state = table.getState(stateName);

    if (BeeUtils.isEmpty(state)) {
      LogUtils.warning(logger, "State not registered:", tbl, stateName);
    } else if (mdRole ? !state.supportsRoles() : !state.supportsUsers()) {
      LogUtils.warning(logger, "State does not support " + (mdRole ? "Roles" : "Users") + " mode:",
          tbl, stateName);
    } else {
      if (!state.isActive()) {
        state.setActive(true);
        rebuildTable(table, true);
      }
      Collection<Integer> bitSet = null;

      if (mdRole) {
        bitSet = usr.getRoles().keySet();
      } else {
        bitSet = usr.getUsers().keySet();
      }
      Map<Integer, Boolean> bitMap = new HashMap<Integer, Boolean>();

      for (int bit : bitSet) {
        bitMap.put(bit, ArrayUtils.contains(bit, bits));
      }
      SqlUpdate su = table.updateState(id, state, mdRole, bitMap);

      if (!BeeUtils.isEmpty(su) && qs.updateData(su) == 0) {
        SqlInsert si = table.insertState(id, state, mdRole, bitMap);
        qs.updateData(si);
      }
    }
  }

  public void verifyStates(SqlSelect query, String tbl, String tblAlias,
      int userId, String... states) {
    Assert.notNull(query);
    Assert.notEmpty(userId);

    for (String stateName : states) {
      BeeTable table = getTable(tbl);
      BeeState state = table.getState(stateName);

      if (BeeUtils.isEmpty(state)) {
        LogUtils.warning(logger, "State not registered:", tbl, stateName);
      } else {
        table.verifyState(query, tblAlias, state, userId, usr.getUserRoles(userId));
      }
    }
  }

  @Lock(LockType.WRITE)
  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    for (BeeForeignKey fKey : fKeys) {
      String refTblName = fKey.getRefTable();

      if (getTable(refTblName).isActive()) {
        qs.updateData(SqlUtils.createForeignKey(fKey.getTable(), fKey.getName(),
            fKey.getKeyField(), refTblName, getIdName(refTblName), fKey.getAction()));
      }
    }
  }

  @Lock(LockType.WRITE)
  private void createKeys(Collection<BeeKey> keys) {
    for (BeeKey key : keys) {
      String tblName = key.getTable();

      IsQuery index;
      String keyName = key.getName();
      String[] keyFields = key.getKeyFields();

      if (key.isPrimary()) {
        index = SqlUtils.createPrimaryKey(tblName, keyName, keyFields);
      } else if (key.isUnique()) {
        index = SqlUtils.createUniqueIndex(tblName, keyName, keyFields);
      } else {
        index = SqlUtils.createIndex(tblName, keyName, keyFields);
      }
      qs.updateData(index);
    }
  }

  @Lock(LockType.WRITE)
  private void createTable(BeeTable table, boolean tblExists) {
    if (!BeeUtils.isEmpty(table)) {
      Map<String, SqlCreate> newTables = new HashMap<String, SqlCreate>();
      String tblMain = table.getName();
      newTables.put(tblMain, new SqlCreate(tblMain, false));

      for (BeeField field : table.getFields()) {
        String tblName = field.getTable();

        if (field.isExtended()) {
          SqlCreate sc = table.createExtTable(newTables.get(tblName), field);

          if (!BeeUtils.isEmpty(sc)) {
            newTables.put(tblName, sc);
          }
        } else {
          newTables.get(tblName)
            .addField(field.getName(), field.getType(), field.getPrecision(), field.getScale(),
                field.isNotNull() ? Keywords.NOT_NULL : null);
        }
      }
      for (BeeState state : table.getStates()) {
        if (state.isActive()) {
          String tblName = state.getTable();

          SqlCreate sc = table.createStateTable(newTables.get(tblName), state,
              usr.getUsers().keySet(), usr.getRoles().keySet());

          if (!BeeUtils.isEmpty(sc)) {
            newTables.put(tblName, sc);
          }
        }
      }
      newTables.get(tblMain)
        .addLong(table.getLockName(), Keywords.NOT_NULL)
        .addLong(table.getIdName(), Keywords.NOT_NULL);

      for (SqlCreate sc : newTables.values()) {
        String tblName = (String) sc.getTarget().getSource();
        boolean exists = tblExists;

        if (!BeeUtils.same(tblName, tblMain)) {
          exists = qs.isDbTable(getDbName(), getDbSchema(), tblName);
        }
        String tmp = null;

        if (exists) {
          tmp = backupTable(tblName);
          qs.updateData(SqlUtils.dropTable(tblName));
        }
        qs.updateData(sc);
        restoreTable(tblName, tmp);
      }
      table.setActive(true);
    }
  }

  private BeeView getDefaultView(String tbl, boolean allFields) {
    BeeTable table = getTable(tbl);
    Collection<BeeField> fields = allFields ? table.getFields() : table.getMainFields();

    BeeView view = new BeeView(tbl, tbl, false);

    for (BeeField field : fields) {
      String fld = field.getName();
      view.addField(fld, fld);
      String relTbl = field.getRelation();

      if (!BeeUtils.isEmpty(relTbl) && !BeeUtils.same(relTbl, tbl)) {
        BeeView vw = getDefaultView(relTbl, false);

        for (String xpr : vw.getFields().values()) {
          view.addField(fld + xpr.replaceAll(BeeView.JOIN_MASK, ""), fld + ">" + xpr);
        }
      }
    }
    return view;
  }

  private BeeTable getTable(String tableName) {
    Assert.state(isTable(tableName), "Not a base table: " + tableName);
    return dataCache.get(tableName);
  }

  private Collection<BeeTable> getTables() {
    return ImmutableList.copyOf(dataCache.values());
  }

  private BeeView getView(String viewName) {
    Assert.state(isView(viewName) || isTable(viewName), "Not a view: " + viewName);
    BeeView view = viewCache.get(viewName);

    if (BeeUtils.isEmpty(view)) {
      view = getDefaultView(viewName, true);
      registerView(view);
    }
    return view;
  }

  private BeeRowSet getViewData(BeeView view, IsCondition wh, int limit, int offset,
      String... states) {
    SqlSelect ss = getViewQuery(view, wh)
      .setLimit(limit)
      .setOffset(offset);

    if (!BeeUtils.isEmpty(states)) {
      verifyStates(ss, view.getSource(), null, usr.getCurrentUserId(), states);
    }
    BeeRowSet res = qs.getData(ss);
    res.setViewName(view.getName());

    return res;
  }

  private SqlSelect getViewQuery(BeeView view, IsCondition wh) {
    if (BeeUtils.isEmpty(view) || view.isEmpty()) {
      return null;
    }
    String viewSource = view.getSource();
    if (!isTable(viewSource)) {
      return null;
    }
    Map<String, String> aliases = new HashMap<String, String>();
    aliases.put(viewSource, viewSource);
    Map<String, Map<String, String>> sources = new HashMap<String, Map<String, String>>();
    sources.put("", aliases);

    SqlSelect ss = new SqlSelect().addFrom(viewSource);

    for (Entry<String, String> fldExpr : view.getFields().entrySet()) {
      String xpr = "";
      aliases = sources.get(xpr);
      String fld = null;
      String dst = null;
      String src = viewSource;
      String als = aliases.get(viewSource);
      boolean isError = false;

      for (String ff : fldExpr.getValue().split(BeeView.JOIN_MASK)) {
        if (isTable(dst)) {
          xpr = xpr + fld;
          aliases = sources.get(xpr);

          if (BeeUtils.isEmpty(aliases)) {
            String tmpAls = SqlUtils.uniqueName();
            aliases = new HashMap<String, String>();
            aliases.put(dst, tmpAls);
            sources.put(xpr, aliases);
            ss.addFromLeft(dst, tmpAls, SqlUtils.join(als, fld, tmpAls, getIdName(dst)));
          }
          src = dst;
          als = aliases.get(dst);
        }
        fld = ff;
        BeeField field = getTable(src).getField(fld);

        if (BeeUtils.isEmpty(field)) {
          isError = true;
          break;
        }
        if (field.isExtended()) {
          if (!aliases.containsKey(fld)) {
            String extAls = joinExtField(ss, src, als, fld);
            aliases.put(fld, extAls);
          }
          als = aliases.get(fld);
        }
        dst = field.getRelation();
      }
      if (isError) {
        LogUtils.warning(logger, "Unknown field name:", fld, src, BeeUtils.bracket(xpr));
        continue;
      }
      ss.addField(als, fld, fldExpr.getKey());
    }
    if (!view.isReadOnly()) {
      ss.addFields(viewSource, getLockName(viewSource), getIdName(viewSource));
    }
    ss.setWhere(wh);
    return ss;
  }

  private Document getXmlResource(String resource, String resourceSchema) {
    if (!FileUtils.isInputFile(resource)) {
      return null;
    }
    String error = null;
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    try {
      Schema schema = factory.newSchema(new StreamSource(resourceSchema));
      Validator validator = schema.newValidator();
      validator.validate(new StreamSource(resource));
    } catch (SAXException e) {
      error = e.getMessage();
    } catch (IOException e) {
      error = e.getMessage();
    }
    if (!BeeUtils.isEmpty(error)) {
      LogUtils.severe(logger, resource, error);
      return null;
    }
    return XmlUtils.fromFileName(resource);
  }

  @Lock(LockType.WRITE)
  private void initTables() {
    String resource = Config.getPath("structure.xml");

    Collection<BeeTable> tables = loadTables(resource, Config.getPath(STRUCTURE_SCHEMA));

    if (BeeUtils.isEmpty(tables)) {
      LogUtils.warning(logger, resource, "Nothing to load");
      return;
    }
    dataCache.clear();

    for (BeeTable table : tables) {
      String tblName = table.getName();

      if (table.isEmpty()) {
        LogUtils.warning(logger, resource, "Table", tblName, "has no fields defined");
      } else if (isTable(tblName)) {
        LogUtils.warning(logger, resource, "Dublicate table name:", tblName);
      } else {
        registerTable(table);
      }
    }
    LogUtils.infoNow(logger,
        "Loaded", getTables().size(), "main tables descriptions from", resource);
  }

  @Lock(LockType.WRITE)
  private Collection<BeeTable> loadTables(String resource, String schema) {
    Document xml = getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    Collection<BeeTable> data = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList tables = root.getElementsByTagName("BeeTable");

    for (int i = 0; i < tables.getLength(); i++) {
      Element table = (Element) tables.item(i);

      BeeTable tbl = new BeeTable(table.getAttribute("name")
          , table.getAttribute("idName")
          , table.getAttribute("lockName"));

      for (int x = 0; x < 2; x++) {
        boolean extMode = (x > 0);
        NodeList nodeRoot = table.getElementsByTagName(extMode ? "BeeExtended" : "BeeFields");

        if (nodeRoot.getLength() > 0) {
          NodeList fields = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeField");

          for (int j = 0; j < fields.getLength(); j++) {
            Element field = (Element) fields.item(j);

            String fldName = field.getAttribute("name");
            String tblName = extMode ? tbl.getExtTable(fldName) : tbl.getName();
            boolean notNull = BeeUtils.toBoolean(field.getAttribute("notNull"));
            boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));
            String relation = field.getAttribute("relation");
            boolean cascade = BeeUtils.toBoolean(field.getAttribute("cascade"));

            tbl.addField(tblName, fldName
                , DataTypes.valueOf(field.getAttribute("type"))
                , BeeUtils.toInt(field.getAttribute("precision"))
                , BeeUtils.toInt(field.getAttribute("scale"))
                , notNull, unique, relation, cascade)
              .setExtended(extMode);

            if (!BeeUtils.isEmpty(relation)) {
              tbl.addForeignKey(tblName, fldName, relation,
                  cascade ? (notNull ? Keywords.CASCADE : Keywords.SET_NULL) : null);
            }
            if (unique) {
              tbl.addKey(true, tblName, fldName);
            }
          }
          NodeList keys = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeKey");

          for (int j = 0; j < keys.getLength(); j++) {
            Element key = (Element) keys.item(j);

            String[] keyFields = key.getAttribute("fields").split(",");
            String tblName = extMode ? tbl.getExtTable(keyFields[0]) : tbl.getName();

            tbl.addKey(BeeUtils.toBoolean(key.getAttribute("unique")), tblName, keyFields);
          }
        }
      }
      NodeList nodeRoot = table.getElementsByTagName("BeeStates");

      if (nodeRoot.getLength() > 0) {
        NodeList states = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeState");

        for (int j = 0; j < states.getLength(); j++) {
          Element state = (Element) states.item(j);

          tbl.addState(state.getAttribute("name")
              , BeeUtils.toInt(state.getAttribute("id"))
              , state.getAttribute("mode")
              , BeeUtils.toBoolean(state.getAttribute("checked")));
        }
      }
      data.add(tbl);
    }
    return data;
  }

  @Lock(LockType.WRITE)
  private Collection<BeeView> loadViews(String resource, String schema) {
    Document xml = getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    Collection<BeeView> data = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList views = root.getElementsByTagName("BeeView");

    for (int i = 0; i < views.getLength(); i++) {
      Element view = (Element) views.item(i);

      BeeView vw = new BeeView(view.getAttribute("name")
          , view.getAttribute("source")
          , BeeUtils.toBoolean(view.getAttribute("readOnly")));

      NodeList nodeRoot = view.getElementsByTagName("BeeColumns");

      if (nodeRoot.getLength() > 0) {
        NodeList cols = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeColumn");

        for (int j = 0; j < cols.getLength(); j++) {
          Element col = (Element) cols.item(j);
          vw.addField(col.getAttribute("name"), col.getAttribute("expression"));
        }
      }
      data.add(vw);
    }
    return data;
  }

  @Lock(LockType.WRITE)
  private void rebuildTable(BeeTable table, boolean exists) {
    if (!BeeUtils.isEmpty(table)) {
      String tblName = table.getName();

      if (exists) {
        for (String[] fKeys : qs.dbForeignKeys(getDbName(), getDbSchema(), null, tblName)) {
          String fk = fKeys[0];
          String tbl = fKeys[1];
          qs.updateData(SqlUtils.dropForeignKey(tbl, fk));
        }
      }
      Collection<BeeForeignKey> fKeys = Lists.newArrayList();

      for (BeeTable tbl : getTables()) {
        if (BeeUtils.same(tbl.getName(), tblName)) {
          continue;
        }
        for (BeeForeignKey fKey : tbl.getForeignKeys()) {
          if (BeeUtils.same(fKey.getRefTable(), tblName) && tbl.isActive()) {
            fKeys.add(fKey);
          }
        }
      }
      createTable(table, exists);
      createKeys(table.getKeys());
      createForeignKeys(table.getForeignKeys());
      createForeignKeys(fKeys);
    }
  }

  private void registerTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      dataCache.put(table.getName(), table);
    }
  }

  private void registerView(BeeView view) {
    if (!BeeUtils.isEmpty(view)) {
      viewCache.put(view.getName(), view);
    }
  }
}
