package com.butent.bee.server.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.data.BeeTable.BeeState;
import com.butent.bee.server.utils.FileUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.TableInfo;
import com.butent.bee.shared.sql.BeeConstants;
import com.butent.bee.shared.sql.BeeConstants.DataType;
import com.butent.bee.shared.sql.BeeConstants.Keyword;
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
import com.butent.bee.shared.utils.TimeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    int rc = qs.dbRowCount(name, null);

    if (rc > 0) {
      tmp = SqlUtils.temporaryName();

      qs.updateData(new SqlCreate(tmp)
          .setDataSource(new SqlSelect().addAllFields(name).addFrom(name)));
    }
    return tmp;
  }

  public ResponseObject commitChanges(BeeRowSet changes) {
    String err = "";
    int c = 0;

    BeeTable table = null;
    String tblName = null;

    BeeView view = getView(changes.getViewName());
    Map<Integer, BeeField> fields = Maps.newHashMap();

    if (!BeeUtils.isEmpty(view)) {
      tblName = view.getSource();
    }
    if (isTable(tblName)) {
      table = getTable(tblName);

      if (view.isReadOnly()) {
        err = "Table " + tblName + " is read only.";
      } else {
        int i = 0;
        for (BeeField fld : view.getFields().values()) {
          if (BeeUtils.equals(fld.getOwner(), tblName)) { // TODO pataisyti
            fields.put(i, fld);
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
      Map<String, Object[]> baseUpdate = Maps.newHashMap();
      Map<String, Object> extUpdate = Maps.newHashMap();

      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Integer col : row.getShadow().keySet()) {
          BeeField field = fields.get(col);

          if (BeeUtils.isEmpty(field)) {
            err = "Cannot update column " + changes.getColumnLabel(col) + " (Unknown source).";
            break;
          }
          String colName = field.getName();
          Object newValue = changes.getOriginal(row, col);
          Object oldValue = row.getShadow().get(col);

          if (field.isExtended()) {
            extUpdate.put(colName, newValue);
          } else {
            baseUpdate.put(colName, new Object[]{newValue, oldValue});
          }
        }
        if (!BeeUtils.isEmpty(err)) {
          break;
        }
      }

      if (row.isMarkedForInsert()) { // INSERT
        SqlInsert si = new SqlInsert(tblName);

        for (String colName : baseUpdate.keySet()) {
          si.addConstant(colName, baseUpdate.get(colName)[0]);
        }
        long lock = System.currentTimeMillis();
        si.addConstant(table.getLockName(), lock);
        row.setVersion(lock);

        long id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
          break;
        }
        c++;
        row.setNewId(id);

        if (!BeeUtils.isEmpty(extUpdate)) {
          int res = commitExtChanges(table, id, extUpdate, false);

          if (res < 0) {
            err = "Error inserting extended fields";
            break;
          }
          c += res;
        }
      } else {
        long id = row.getId();
        IsCondition idWh = SqlUtils.equal(tblName, table.getIdName(), id);
        IsCondition wh = SqlUtils.and(idWh,
            SqlUtils.equal(tblName, table.getLockName(), row.getVersion()));

        if (row.isMarkedForDelete()) { // DELETE
          int res = qs.updateData(new SqlDelete(tblName).setWhere(wh));

          if (res > 0) {
            c += res;
          } else {
            err = (res < 0) ? "Error deleting data" : "Optimistic lock exception";
            break;
          }

        } else { // UPDATE
          if (!BeeUtils.isEmpty(baseUpdate)) {
            SqlUpdate su = new SqlUpdate(tblName);

            for (String colName : baseUpdate.keySet()) {
              su.addConstant(colName, baseUpdate.get(colName)[0]);
            }
            long lock = System.currentTimeMillis();
            su.addConstant(table.getLockName(), lock);
            row.setVersion(lock);

            int res = qs.updateData(su.setWhere(wh));

            if (res == 0) { // Optimistic lock exception
              BeeRowSet rs = getViewData(view.getName(), idWh, 0, 0);

              if (!rs.isEmpty()) {
                BeeRow r = rs.getRow(0);
                boolean collision = false;

                for (String colName : baseUpdate.keySet()) {
                  if (!BeeUtils.equals(
                      BeeUtils.transformNoTrim(rs.getOriginal(r, colName)),
                      BeeUtils.transformNoTrim(baseUpdate.get(colName)[1]))) {
                    collision = true;
                    break;
                  }
                  rs.setValue(r, colName, changes.getString(row, colName));
                }
                if (!collision) {
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
    }
    ResponseObject response = new ResponseObject();

    if (!BeeUtils.isEmpty(err)) {
      response.addError(err);
    } else {
      response.addInfo("Update count:", c);
      response.setResponse(changes);

      if (BeeUtils.inList(tblName, UserServiceBean.USER_TABLE, UserServiceBean.ROLE_TABLE,
          UserServiceBean.USER_ROLES_TABLE)) {
        usr.invalidateCache();
      }
    }
    return response;
  }

  public int commitExtChanges(BeeTable table, long id, Map<String, Object> extUpdate,
      boolean updateMode) {
    int c = 0;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String colName : extUpdate.keySet()) {
      Object value = extUpdate.get(colName);
      BeeField field = table.getField(colName);
      String extName = field.getTable();

      if (!queryMap.containsKey(extName)) {
        queryMap.put(extName, new IsQuery[2]);
      }
      IsQuery[] queries = queryMap.get(extName);
      queries[0] = table.insertExtField((SqlInsert) queries[0], id, field, value);

      if (updateMode) {
        queries[1] = table.updateExtField((SqlUpdate) queries[1], id, field, value);
      }
    }
    for (IsQuery[] queries : queryMap.values()) {
      int res = 0;
      IsQuery insQuery = queries[0];
      IsQuery updQuery = queries[1];

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
    return c;
  }

  public ResponseObject editStateRoles(String tblName, String stateName) {
    boolean allMode = BeeUtils.isEmpty(tblName);
    Collection<BeeState> states = Lists.newArrayList();

    if (allMode) {
      for (BeeTable table : getTables()) {
        if (table.hasState(stateName)) {
          BeeState state = table.getState(stateName);

          if (state.supportsRoles()) {
            states.add(state);
          }
        }
      }
    } else {
      if (getTable(tblName).hasState(stateName)) {
        BeeState state = getTable(tblName).getState(stateName);

        if (state.supportsRoles()) {
          states.add(state);
        }
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
        ss = getView(tbl).getQuery();
      }
      String stateAlias = joinState(ss, tbl, null, state.getName());

      for (int roleId : roles.keySet()) {
        String colName = state.getName() + roleId + roles.get(roleId);

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
      return ResponseObject.error("State \"", stateName, "\" does not support roles");
    }
    return ResponseObject.response(qs.getViewData(union, null));
  }

  public ResponseObject generateData(String tblName, int rowCount) {
    Assert.isTrue(isTable(tblName), "Not a base table: " + tblName);
    Assert.isPositive(rowCount, "rowCount must be positive");

    Collection<BeeField> fields = getTableFields(tblName);
    SqlInsert si = new SqlInsert(tblName);
    Map<String, Object> extUpdate = Maps.newHashMap();
    Map<String, String[]> relations = Maps.newHashMap();

    int minDay = new JustDate().getDay() - 1000;
    int maxDay = new JustDate().getDay() + 200;
    long minTime = new DateTime().getTime() - 1000L * TimeUtils.MILLIS_PER_DAY;
    long maxTime = new DateTime().getTime() + 200L * TimeUtils.MILLIS_PER_DAY;

    StringBuilder chars = new StringBuilder();
    for (char c = 'a'; c <= 'z'; c++) {
      chars.append(c);
    }
    chars.append(chars.toString().toUpperCase()).append(" ąčęėįšųūžĄČĘĖĮŠŲŪŽ");

    Random random = new Random();
    Object v;

    for (int row = 0; row < rowCount; row++) {
      for (BeeField field : fields) {
        String relation = field.getRelation();

        if (!BeeUtils.isEmpty(relation)) {
          String[] rs = relations.get(relation);

          if (!relations.containsKey(relation)) {
            rs = qs.getColumn(new SqlSelect()
                .addFields(relation, getIdName(relation))
                .addFrom(relation));

            if (BeeUtils.isEmpty(rs) && field.isNotNull()) {
              return ResponseObject
                  .error(field.getName(), ": Relation table", relation, "is empty");
            }
            relations.put(relation, rs);
          }
          if (BeeUtils.isEmpty(rs)) {
            v = null;
          } else {
            v = BeeUtils.toInt(rs[random.nextInt(rs.length)]);
          }
        } else {
          switch (field.getType()) {
            case BOOLEAN:
              v = random.nextBoolean();
              break;
            case CHAR:
              v = BeeUtils.randomString(1, field.getPrecision(), BeeConst.CHAR_SPACE, '\u007e');
              break;
            case DATE:
              v = new JustDate(BeeUtils.randomInt(minDay, maxDay));
              break;
            case DATETIME:
              v = new DateTime(BeeUtils.randomLong(minTime, maxTime));
              break;
            case DOUBLE:
              v = Math.random() * Math.pow(10, BeeUtils.randomInt(-7, 20));
              break;
            case INTEGER:
              v = random.nextInt();
              break;
            case LONG:
              v = random.nextLong() / random.nextInt();
              break;
            case NUMERIC:
              if (field.getPrecision() <= 1) {
                v = random.nextInt(10);
              } else {
                double x =
                    Math.random() * Math.pow(10, BeeUtils.randomInt(0, field.getPrecision()));
                if (field.getScale() <= 0) {
                  v = Math.round(x);
                } else {
                  v = Math.round(x) / Math.pow(10, field.getScale());
                }
              }
              break;
            case STRING:
              int len = field.getPrecision();
              if (len > 3) {
                len = BeeUtils.randomInt(1, len + 1);
              }
              v = BeeUtils.randomString(len, chars);
              break;
            default:
              v = null;
          }
        }
        if (!field.isNotNull() && random.nextInt(7) == 0) {
          v = null;
        }
        if (field.isExtended()) {
          if (v != null) {
            extUpdate.put(field.getName(), v);
          }
        } else {
          si.addConstant(field.getName(), v);
        }
      }
      long id = qs.insertData(si);

      if (id < 0) {
        return ResponseObject.error(tblName, si.getQuery(), "Error inserting data");
      } else {
        commitExtChanges(getTable(tblName), id, extUpdate, false);
      }
      si.reset();
      extUpdate.clear();
    }
    return ResponseObject.info(tblName, "generated", rowCount, "rows");
  }

  public List<TableInfo> getDataInfo() {
    List<TableInfo> lst = Lists.newArrayList();
    int cnt;

    for (BeeTable table : getTables()) {
      if (table.isActive()) {
        cnt = qs.dbRowCount(table.getName(), null);
      } else {
        cnt = -1;
      }
      lst.add(new TableInfo(table.getName(), cnt));
    }
    return lst;
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

  public String getLockName(String tblName) {
    return getTable(tblName).getLockName();
  }

  public BeeField getTableField(String tblName, String fldName) {
    return getTable(tblName).getField(fldName);
  }

  public Collection<BeeField> getTableFields(String tblName) {
    return getTable(tblName).getFields();
  }

  public Collection<String> getTableNames() {
    return ImmutableSet.copyOf(dataCache.keySet());
  }

  public Collection<String> getTableStates(String tblName) {
    Collection<String> states = Lists.newArrayList();

    for (BeeState state : getTable(tblName).getStates()) {
      states.add(state.getName());
    }
    return states;
  }

  public BeeRowSet getViewData(String viewName, IsCondition wh, int limit, int offset,
      String... states) {

    BeeView view = getView(viewName);

    SqlSelect ss = view.getQuery()
        .setLimit(limit)
        .setOffset(offset);

    if (!BeeUtils.isEmpty(wh)) {
      ss.setWhere(SqlUtils.and(ss.getWhere(), wh));
    }
    if (!BeeUtils.isEmpty(states)) {
      verifyStates(ss, view.getSource(), null, usr.getCurrentUserId(), states);
    }
    return qs.getViewData(ss, view);
  }

  @PostConstruct
  public void init() {
    initTables();
    initExtensions();
    initViews();
  }

  public void initDatabase(String engine) {
    SqlBuilderFactory.setDefaultEngine(BeeConst.getDsType(engine));
    dbName = qs.dbName();
    dbSchema = qs.dbSchema();
    String[] dbTables = qs.dbTables(dbName, dbSchema, null);

    for (BeeTable table : getTables()) {
      table.setActive(BeeUtils.inListSame(table.getName(), dbTables));

      Map<String, String[]> stateTables = Maps.newHashMap();

      for (BeeState state : table.getStates()) {
        String tblName = state.getTable();
        boolean active = false;

        if (BeeUtils.inListSame(tblName, dbTables)) {
          if (!stateTables.containsKey(tblName)) {
            stateTables.put(tblName, qs.dbFields(tblName));
          }
          String[] dbFields = stateTables.get(tblName);

          for (String fld : dbFields) {
            if (BeeUtils.startsSame(fld, table.getStateField(state.getName()))) { // TODO startsSame
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

  public boolean isTable(String tblName) {
    return dataCache.containsKey(tblName);
  }

  public boolean isView(String viewName) {
    return viewCache.containsKey(viewName);
  }

  public String joinExtField(HasFrom<?> query, String tblName, String tblAlias, String fldName) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);
    BeeField field = table.getField(fldName);

    if (field.isExtended()) {
      return table.joinExtField(query, tblAlias, field);
    }
    return BeeUtils.ifString(tblAlias, tblName);
  }

  public String joinState(HasFrom<?> query, String tblName, String tblAlias, String stateName) {
    Assert.notNull(query);
    BeeTable table = getTable(tblName);

    if (!table.hasState(stateName)) {
      LogUtils.warning(logger, "State not registered:", tblName, stateName);
      return null;
    }
    return table.joinState(query, tblAlias, table.getState(stateName));
  }

  public void rebuildActiveTables() {
    for (BeeTable table : getTables()) {
      if (table.isActive()) {
        rebuildTable(table.getName());
      }
    }
  }

  public void rebuildTable(String tblName) {
    rebuildTable(getTable(tblName), qs.isDbTable(getDbName(), getDbSchema(), tblName));
  }

  public int restoreTable(String tblName, String tmp) {
    int rc = 0;

    if (!BeeUtils.isEmpty(tmp)) {
      String[] tmpFields = qs.dbFields(tmp);
      Collection<String> fldList = Lists.newArrayList();

      for (String fld : qs.dbFields(tblName)) {
        if (BeeUtils.inListSame(fld, tmpFields)) {
          fldList.add(fld);
        }
      }
      if (!BeeUtils.isEmpty(fldList)) {
        String[] flds = fldList.toArray(new String[0]);

        rc = qs.updateData(new SqlInsert(tblName)
            .addFields(flds)
            .setDataSource(new SqlSelect().addFields(tmp, flds).addFrom(tmp)));
      }
      qs.updateData(SqlUtils.dropTable(tmp));
    }
    return rc;
  }

  public void setState(String tblName, long id, String stateName, boolean mdRole, int... bits) {
    BeeTable table = getTable(tblName);

    if (!table.hasState(stateName)) {
      LogUtils.warning(logger, "State not registered:", tblName, stateName);
    } else {
      BeeState state = table.getState(stateName);

      if (mdRole ? !state.supportsRoles() : !state.supportsUsers()) {
        LogUtils.warning(logger, "State does not support " + (mdRole ? "Roles" : "Users")
            + " mode:",
            tblName, stateName);
      } else {
        if (!state.isActive()) {
          state.setActive(true);
          rebuildTable(tblName);
        }
        Collection<Integer> bitSet = null;

        if (mdRole) {
          bitSet = usr.getRoles().keySet();
        } else {
          bitSet = usr.getUsers().keySet();
        }
        Map<Integer, Boolean> bitMap = Maps.newHashMap();

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
  }

  public void verifyStates(SqlSelect query, String tblName, String tblAlias,
      int userId, String... states) {
    Assert.notNull(query);
    Assert.notEmpty(userId);

    for (String stateName : states) {
      BeeTable table = getTable(tblName);

      if (!table.hasState(stateName)) {
        LogUtils.warning(logger, "State not registered:", tblName, stateName);
      } else {
        table.verifyState(query, tblAlias, table.getState(stateName), userId,
            usr.getUserRoles(userId));
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
      Map<String, SqlCreate> newTables = Maps.newHashMap();
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
                  field.isNotNull() ? Keyword.NOT_NULL : null);
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
          .addLong(table.getLockName(), Keyword.NOT_NULL)
          .addLong(table.getIdName(), Keyword.NOT_NULL);

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

  private BeeView getDefaultView(String tblName, boolean allFields) {
    BeeTable table = getTable(tblName);
    Collection<BeeField> fields = allFields ? table.getFields() : table.getMainFields();

    BeeView view = new BeeView(tblName, tblName, false);

    for (BeeField field : fields) {
      String fld = field.getName();
      loadViewField(view, fld, fld);
      String relTbl = field.getRelation();

      if (!BeeUtils.isEmpty(relTbl) && !BeeUtils.same(relTbl, tblName)) {
        BeeView vw = getDefaultView(relTbl, false);

        for (String xpr : vw.getExpressions().values()) {
          loadViewField(view, fld + xpr.replaceAll(BeeView.JOIN_MASK, ""), fld + ">" + xpr);
        }
      }
    }
    return view;
  }

  private BeeTable getTable(String tblName) {
    Assert.state(isTable(tblName), "Not a base table: " + tblName);
    return dataCache.get(tblName);
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
            boolean notNull = BeeUtils.toBoolean(field.getAttribute("notNull"));
            boolean unique = BeeUtils.toBoolean(field.getAttribute("unique"));
            String relation = field.getAttribute("relation");
            boolean cascade = BeeUtils.toBoolean(field.getAttribute("cascade"));

            tbl.addField(fldName
                , DataType.valueOf(field.getAttribute("type"))
                , BeeUtils.toInt(field.getAttribute("precision"))
                , BeeUtils.toInt(field.getAttribute("scale"))
                , notNull, unique, relation, cascade)
                .setExtended(extMode);

            String tblName = tbl.getField(fldName).getTable();

            if (!BeeUtils.isEmpty(relation)) {
              tbl.addForeignKey(tblName, fldName, relation,
                  cascade ? (notNull ? Keyword.CASCADE : Keyword.SET_NULL) : null);
            }
            if (unique) {
              tbl.addKey(true, tblName, fldName);
            }
          }
          NodeList keys = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeKey");

          for (int j = 0; j < keys.getLength(); j++) {
            Element key = (Element) keys.item(j);

            String[] keyFields = key.getAttribute("fields").split(",");
            String tblName = tbl.getField(keyFields[0]).getTable();

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

  private void loadViewField(BeeView vw, String name, String expression) {
    String src = vw.getSource();
    SqlSelect ss = vw.getInternalQuery();
    Map<String, String> aliases = vw.getAliases();

    String xpr = "";
    String als = aliases.get(xpr);
    BeeField field = null;
    String fld = null;
    String dst = null;
    boolean isError = false;

    for (String ff : expression.split(BeeView.JOIN_MASK)) {
      if (isTable(dst)) {
        xpr = xpr + fld;
        String key = xpr + ":" + dst;

        if (!aliases.containsKey(key)) {
          String tmpAls = SqlUtils.uniqueName();
          ss.addFromLeft(dst, tmpAls, SqlUtils.join(als, fld, tmpAls, getIdName(dst)));
          aliases.put(key, tmpAls);
        }
        als = aliases.get(key);
        src = dst;
      }
      fld = ff;

      if (!getTable(src).hasField(fld)) {
        isError = true;
        break;
      }
      field = getTableField(src, fld);

      if (field.isExtended()) {
        String key = fld + ":" + als;

        if (!aliases.containsKey(key)) {
          String tmpAls = joinExtField(ss, src, als, fld);
          aliases.put(key, tmpAls);
        }
        als = aliases.get(key);
      }
      dst = field.getRelation();
    }
    if (isError) {
      LogUtils.warning(logger, "Unknown field name:", fld, src, BeeUtils.bracket(xpr));
    } else {
      ss.addField(als, fld, name);
      vw.addField(name, expression, field);
    }
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
          loadViewField(vw, col.getAttribute("name"), col.getAttribute("expression"));
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
        for (Map<String, String> fKeys : qs
            .dbForeignKeys(getDbName(), getDbSchema(), null, tblName)) {
          String fk = fKeys.get(BeeConstants.FK_NAME);
          String tbl = fKeys.get(BeeConstants.FK_TABLE);
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
