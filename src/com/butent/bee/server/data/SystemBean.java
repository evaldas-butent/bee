package com.butent.bee.server.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.sql.BeeConstants;
import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.BeeConstants.Keyword;
import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlCreate;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.data.view.RowInfoCollection;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.TimeUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
  public static final String STATE_SCHEMA = "states.xsd";
  public static final String STRUCTURE_SCHEMA = "structure.xsd";
  public static final String VIEW_SCHEMA = "view.xsd";

  private static Logger logger = Logger.getLogger(SystemBean.class.getName());

  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;

  private String dbName;
  private String dbSchema;
  private Map<String, BeeState> stateCache = Maps.newHashMap();
  private Map<String, BeeTable> tableCache = Maps.newHashMap();
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
    Map<String, BeeField> editableFields = Maps.newHashMap();

    if (!BeeUtils.isEmpty(view)) {
      tblName = view.getSource();
    }
    if (isTable(tblName)) {
      table = getTable(tblName);

      if (view.isReadOnly()) {
        err = "Table " + tblName + " is read only.";
      } else {
        for (String colName : view.getColumns()) {
          if (view.isSourceField(colName)) {
            editableFields.put(colName, getTableField(tblName, view.getField(colName)));
          }
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
      Map<String, Object[]> translationUpdate = Maps.newHashMap();

      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Integer col : row.getShadow().keySet()) {
          String colId = changes.getColumnId(col);
          BeeField field = editableFields.get(colId);

          if (BeeUtils.isEmpty(field)) {
            err = "Cannot update column " + colId + " (Unknown source).";
            break;
          }
          String locale = view.getLocale(colId);
          String fld = field.getName();
          Object newValue = changes.getOriginal(row, col);
          Object oldValue = row.getShadow().get(col);

          if (!BeeUtils.isEmpty(locale)) {
            translationUpdate.put(fld, new Object[] {locale, newValue});
          } else if (field.isExtended()) {
            extUpdate.put(fld, newValue);
          } else {
            baseUpdate.put(fld, new Object[] {newValue, oldValue});
          }
        }
        if (!BeeUtils.isEmpty(err)) {
          break;
        }
      }

      if (row.isMarkedForInsert()) { // INSERT
        SqlInsert si = new SqlInsert(tblName);

        for (String fld : baseUpdate.keySet()) {
          si.addConstant(fld, baseUpdate.get(fld)[0]);
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
        if (!BeeUtils.isEmpty(translationUpdate)) {
          int res = commitTranslationChanges(table, id, translationUpdate, false);

          if (res < 0) {
            err = "Error inserting translation fields";
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

            for (String fld : baseUpdate.keySet()) {
              su.addConstant(fld, baseUpdate.get(fld)[0]);
            }
            long lock = System.currentTimeMillis();
            su.addConstant(table.getLockName(), lock);
            row.setVersion(lock);

            int res = qs.updateData(su.setWhere(wh));

            if (res == 0) { // Optimistic lock exception
              BeeRowSet rs = getViewData(view.getName(), idWh, null, 0, 0);

              if (!rs.isEmpty()) {
                BeeRow r = rs.getRow(0);
                boolean collision = false;

                for (String fld : baseUpdate.keySet()) {
                  if (!BeeUtils.equals(
                      BeeUtils.transformNoTrim(rs.getOriginal(r, fld)),
                      BeeUtils.transformNoTrim(baseUpdate.get(fld)[1]))) {
                    collision = true;
                    break;
                  }
                  rs.setValue(r, fld, changes.getString(row, fld));
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
          if (!BeeUtils.isEmpty(translationUpdate)) {
            int res = commitTranslationChanges(table, id, translationUpdate, true);

            if (res < 0) {
              err = "Error updating translation fields";
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

    for (String fld : extUpdate.keySet()) {
      Object value = extUpdate.get(fld);
      BeeField field = table.getField(fld);
      String extTable = field.getTable();

      if (!queryMap.containsKey(extTable)) {
        queryMap.put(extTable, new IsQuery[2]);
      }
      IsQuery[] queries = queryMap.get(extTable);
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

  public int commitTranslationChanges(BeeTable table, long id,
      Map<String, Object[]> translationUpdate, boolean updateMode) {
    int c = 0;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String fld : translationUpdate.keySet()) {
      String locale = (String) translationUpdate.get(fld)[0];
      Object value = translationUpdate.get(fld)[1];
      BeeField field = table.getField(fld);
      String translationTable = table.getTranslationTable(field);

      if (table.updateTranslationActive(field, locale)) {
        rebuildTable(table.getName());
      }
      if (!queryMap.containsKey(translationTable)) {
        queryMap.put(translationTable, new IsQuery[2]);
      }
      IsQuery[] queries = queryMap.get(translationTable);
      queries[0] = table.insertTranslationField((SqlInsert) queries[0], id, field, locale, value);

      if (updateMode) {
        queries[1] = table.updateTranslationField((SqlUpdate) queries[1], id, field, locale, value);
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

  public int deleteRows(String viewName, RowInfoCollection rows) {
    if (BeeUtils.isEmpty(viewName) || rows == null) {
      return -1;
    }
    String idName = getIdName(viewName);
    if (BeeUtils.isEmpty(idName)) {
      return -1;
    }

    int result = 0;

    for (RowInfo rowInfo : rows) {
      long id = rowInfo.getId();
      int z = qs.updateData(new SqlDelete(viewName).setWhere(SqlUtils.equal(viewName, idName, id)));
      if (z > 0) {
        result++;
      } else if (z < 0) {
        break;
      }
    }
    return result;
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

  public String getRelation(String tblName, String fldName) {
    return getTableField(tblName, fldName).getRelation();
  }

  public BeeState getState(String stateName) {
    Assert.state(isState(stateName), "Not a state: " + stateName);
    return stateCache.get(stateName);
  }

  public Collection<String> getTableNames() {
    return ImmutableSet.copyOf(tableCache.keySet());
  }

  public Collection<String> getTableStates(String tblName) {
    Collection<String> states = Lists.newArrayList();

    for (BeeState state : getTable(tblName).getStates()) {
      states.add(state.getName());
    }
    return states;
  }

  public BeeView getView(String viewName) {
    Assert.state(isView(viewName) || isTable(viewName), "Not a view: " + viewName);
    BeeView view = viewCache.get(viewName);

    if (BeeUtils.isEmpty(view)) {
      view = getDefaultView(viewName, true);
      registerView(view);
    }
    return view;
  }

  public IsCondition getViewCondition(String viewName, Filter filter) {
    return getView(viewName).getCondition(filter);
  }

  public BeeRowSet getViewData(String viewName, IsCondition condition, Order order,
      int limit, int offset, String... states) {

    SqlSelect ss = getViewQuery(viewName);
    BeeView view = getView(viewName);

    if (!BeeUtils.isEmpty(condition)) {
      ss.setWhere(SqlUtils.and(ss.getWhere(), condition));
    }

    if (order != null) {
      ss.resetOrder();
      String source = view.getSource();
      String idCol = getIdName(source);
      boolean hasId = false;

      for (Order.Column col : order.getColumns()) {
        String als = view.getAlias(col.getLabel());

        if (!BeeUtils.isEmpty(als)) {
          String fld = view.getField(col.getLabel());

          if (col.isAscending()) {
            ss.addOrder(als, fld);
          } else {
            ss.addOrderDesc(als, fld);
          }
          if (!hasId) {
            hasId = BeeUtils.same(fld, idCol) && BeeUtils.same(als, source);
          }
        }
      }
      if (!hasId) {
        ss.addOrder(source, idCol);
      }
    }

    if (!BeeUtils.isEmpty(states)) {
      ss = verifyStates(ss, view.getSource(), null, states);
    }

    if (limit > 0) {
      ss.setLimit(limit);
    }
    if (offset > 0) {
      ss.setOffset(offset);
    }

    return qs.getViewData(ss, view);
  }

  public List<DataInfo> getViewInfo() {
    List<DataInfo> lst = Lists.newArrayList();
    Set<String> views = Sets.newHashSet(getViewNames());
    views.addAll(getTableNames());

    for (String vw : views) {
      BeeTable source = getTable(getView(vw).getSource());
      int cnt = -1;

      if (source.isActive()) {
        cnt = getViewSize(vw, null);
      }
      lst.add(new DataInfo(vw, source.getIdName(), cnt));
    }
    return lst;
  }

  public Collection<String> getViewNames() {
    return ImmutableSet.copyOf(viewCache.keySet());
  }

  public SqlSelect getViewQuery(String viewName) {
    return getView(viewName).getQuery(tableCache);
  }

  public int getViewSize(String viewName, IsCondition condition) {
    SqlSelect ss = getViewQuery(viewName);

    if (!BeeUtils.isEmpty(condition)) {
      ss.setWhere(SqlUtils.and(ss.getWhere(), condition));
    }
    return qs.dbRowCount(ss);
  }

  public boolean hasField(String tblName, String fldName) {
    return getTable(tblName).hasField(fldName);
  }

  @Lock(LockType.WRITE)
  public void initDatabase(String engine) {
    SqlBuilderFactory.setDefaultEngine(BeeConst.getDsType(engine));
    dbName = qs.dbName();
    dbSchema = qs.dbSchema();
    String[] dbTables = qs.dbTables(dbName, dbSchema, null);

    for (BeeTable table : getTables()) {
      table.setActive(BeeUtils.inListSame(table.getName(), dbTables));

      Map<String, String[]> tableFields = Maps.newHashMap();

      for (BeeState state : table.getStates()) {
        String tblName = table.getStateTable(state);

        if (BeeUtils.inListSame(tblName, dbTables)) {
          if (!tableFields.containsKey(tblName)) {
            tableFields.put(tblName, qs.dbFields(tblName));
          }
        }
        table.setStateActive(state, tableFields.get(tblName));
      }
      for (BeeField field : table.getFields()) {
        String tblName = table.getTranslationTable(field);

        if (BeeUtils.inListSame(tblName, dbTables)) {
          if (!tableFields.containsKey(tblName)) {
            tableFields.put(tblName, qs.dbFields(tblName));
          }
        }
        table.setTranslationActive(field, tableFields.get(tblName));
      }
    }
    usr.invalidateCache();
    initViews(true);
  }

  public void initStates() {
    initStates(false);
  }

  public void initTables() {
    initTables(false);
  }

  public void initViews() {
    initViews(false);
  }

  public boolean isState(String stateName) {
    return stateCache.containsKey(stateName);
  }

  public boolean isTable(String tblName) {
    return tableCache.containsKey(tblName);
  }

  public boolean isView(String viewName) {
    return viewCache.containsKey(viewName);
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

  public void setState(String tblName, long id, String stateName, long... bits) {
    BeeTable table = getTable(tblName);
    BeeState state = getState(stateName);

    if (!table.hasState(state)) {
      LogUtils.warning(logger, "State not registered:", tblName, stateName);
    } else {
      Map<Long, Boolean> bitMap = Maps.newHashMap();

      for (long bit : usr.getUsers().keySet()) {
        bitMap.put(-bit, Longs.contains(bits, -bit));
      }
      for (long bit : usr.getRoles().keySet()) {
        bitMap.put(bit, Longs.contains(bits, bit));
      }
      if (table.updateStateActive(state, Longs.toArray(bitMap.keySet()))) {
        rebuildTable(tblName);
      }
      SqlUpdate su = table.updateState(id, state, bitMap);

      if (!BeeUtils.isEmpty(su) && qs.updateData(su) == 0) {
        qs.updateData(table.insertState(id, state, bitMap));
      }
    }
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
        tblName = table.getTranslationTable(field);
        SqlCreate sc = table.createTranslationTable(newTables.get(tblName), field);

        if (!BeeUtils.isEmpty(sc)) {
          newTables.put(tblName, sc);
        }
      }
      for (BeeState state : table.getStates()) {
        String tblName = table.getStateTable(state);

        SqlCreate sc = table.createStateTable(newTables.get(tblName), state);

        if (!BeeUtils.isEmpty(sc)) {
          newTables.put(tblName, sc);
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

    BeeView view = new BeeView(tblName, tblName, getIdName(tblName), false);

    for (BeeField field : fields) {
      String fld = field.getName();
      view.addField(fld, fld, null, tableCache);
      String relTbl = field.getRelation();

      if (!BeeUtils.isEmpty(relTbl) && !BeeUtils.same(relTbl, tblName)) {
        BeeView vw = getDefaultView(relTbl, false);

        for (String colName : vw.getColumns()) {
          view.addField(fld + colName, fld + ">" + vw.getExpression(colName), null, tableCache);
        }
      }
    }
    return view;
  }

  private BeeTable getTable(String tblName) {
    Assert.state(isTable(tblName), "Not a base table: " + tblName);
    return tableCache.get(tblName);
  }

  private BeeField getTableField(String tblName, String fldName) {
    return getTable(tblName).getField(fldName);
  }

  private Collection<BeeField> getTableFields(String tblName) {
    return getTable(tblName).getFields();
  }

  private Collection<BeeTable> getTables() {
    return ImmutableList.copyOf(tableCache.values());
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initStates(true);
    initTables(true);
  }

  @Lock(LockType.WRITE)
  private void initStates(boolean mainMode) {
    if (mainMode) {
      stateCache.clear();
    }
    String resource =
        mainMode ? Config.getConfigPath("states.xml") : Config.getUserPath("states.xml");

    Collection<BeeState> states = loadStates(resource, Config.getSchemaPath(STATE_SCHEMA));

    if (BeeUtils.isEmpty(states)) {
      if (mainMode) {
        LogUtils.warning(logger, resource, "No states defined");
      }
      return;
    }
    int cNew = 0;
    int cUpd = 0;

    for (BeeState state : states) {
      String stateName = state.getName();

      if (isState(stateName)) {
        if (mainMode) {
          LogUtils.warning(logger, resource, "Dublicate state name:", stateName);
          continue;
        }
        cUpd++;
      } else {
        cNew++;
      }
      registerState(state);
    }
    LogUtils.infoNow(logger, "Loaded", cNew + (mainMode ? "" : " and updated " + cUpd),
        "states descriptions from", resource);

    if (mainMode) {
      initStates();
    }
  }

  @Lock(LockType.WRITE)
  private void initTables(boolean mainMode) {
    if (mainMode) {
      tableCache.clear();
    }
    String resource =
        mainMode ? Config.getConfigPath("structure.xml") : Config.getUserPath("structure.xml");

    Collection<BeeTable> tables = loadTables(resource, Config.getSchemaPath(STRUCTURE_SCHEMA));

    if (BeeUtils.isEmpty(tables)) {
      if (mainMode) {
        LogUtils.severe(logger, resource, "No tables defined");
      }
      return;
    }
    int cNew = 0;
    int cUpd = 0;

    for (BeeTable table : tables) {
      String tblName = table.getName();
      boolean isCustom = !mainMode && (!isTable(tblName) || getTable(tblName).isCustom());

      if (mainMode && isTable(tblName)) {
        LogUtils.warning(logger, resource, "Dublicate table name:", tblName);
        continue;

      } else if (mainMode || isCustom) {
        if (table.isEmpty()) {
          LogUtils.warning(logger, resource, "Table", tblName, "has no fields defined");
          continue;
        }
        if (isCustom) {
          table.setCustom();
        }
        registerTable(table);
        cNew++;

      } else {
        if (getTable(tblName).applyChanges(table) > 0) {
          cUpd++;
        }
      }
    }
    LogUtils.infoNow(logger, "Loaded", cNew + (mainMode ? "" : " and updated " + cUpd),
        "tables descriptions from", resource);

    if (mainMode) {
      initTables();
    }
  }

  @Lock(LockType.WRITE)
  private void initViews(boolean mainMode) {
    if (mainMode) {
      viewCache.clear();
    }
    String resource =
        mainMode ? Config.getConfigPath("views.xml") : Config.getUserPath("views.xml");

    Collection<BeeView> views = loadViews(resource, Config.getSchemaPath(VIEW_SCHEMA));

    if (BeeUtils.isEmpty(views)) {
      if (mainMode) {
        LogUtils.severe(logger, resource, "No views defined");
      }
      return;
    }
    int cNew = 0;
    int cUpd = 0;

    for (BeeView view : views) {
      String viewName = view.getName();

      if (view.isEmpty()) {
        LogUtils.warning(logger, resource, "View", viewName, "has no columns defined");
        continue;
      } else if (isView(viewName)) {
        if (mainMode) {
          LogUtils.warning(logger, resource, "Dublicate view name:", viewName);
          continue;
        }
        cUpd++;
      } else {
        cNew++;
      }
      registerView(view);
    }
    LogUtils.infoNow(logger, "Loaded", cNew + (mainMode ? "" : " and updated " + cUpd),
        "views descriptions from", resource);

    if (mainMode) {
      initViews();
    }
  }

  @Lock(LockType.WRITE)
  private Collection<BeeState> loadStates(String resource, String schema) {
    Document xml = XmlUtils.getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    Collection<BeeState> data = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList states = root.getElementsByTagName("BeeState");

    for (int i = 0; i < states.getLength(); i++) {
      Element state = (Element) states.item(i);

      data.add(new BeeState(state.getAttribute("name")
          , state.getAttribute("mode")
          , BeeUtils.toBoolean(state.getAttribute("checked"))));
    }
    return data;
  }

  @Lock(LockType.WRITE)
  private Collection<BeeTable> loadTables(String resource, String schema) {
    Document xml = XmlUtils.getXmlResource(resource, schema);
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

      String[] states = BeeUtils.split(table.getAttribute("states"), ",");
      for (String state : states) {
        tbl.addState(getState(state));
      }
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
      data.add(tbl);
    }
    return data;
  }

  @Lock(LockType.WRITE)
  private Collection<BeeView> loadViews(String resource, String schema) {
    Document xml = XmlUtils.getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }
    Collection<BeeView> data = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList views = root.getElementsByTagName("BeeView");

    for (int i = 0; i < views.getLength(); i++) {
      Element view = (Element) views.item(i);

      String src = view.getAttribute("source");
      if (!isTable(src)) {
        LogUtils.warning(logger, "View source is not valid:", src);
        continue;
      }
      BeeView vw = new BeeView(view.getAttribute("name")
          , src, getIdName(src)
          , BeeUtils.toBoolean(view.getAttribute("readOnly")));

      NodeList nodeRoot = view.getElementsByTagName("BeeColumns");

      if (nodeRoot.getLength() > 0) {
        NodeList cols = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeColumn");

        for (int j = 0; j < cols.getLength(); j++) {
          Element col = (Element) cols.item(j);
          vw.addField(col.getAttribute("name")
              , col.getAttribute("expression")
              , col.getAttribute("locale")
              , tableCache);
        }
      }
      nodeRoot = view.getElementsByTagName("BeeOrder");

      if (nodeRoot.getLength() > 0) {
        NodeList orders = ((Element) nodeRoot.item(0)).getElementsByTagName("OrderBy");

        for (int j = 0; j < orders.getLength(); j++) {
          Element order = (Element) orders.item(j);
          vw.addOrder(order.getAttribute("name")
              , BeeUtils.toBoolean(order.getAttribute("descending")));
        }
      }
      data.add(vw);
    }
    return data;
  }

  @Lock(LockType.WRITE)
  private void rebuildTable(BeeTable table, boolean exists) {
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

  private void registerState(BeeState state) {
    if (!BeeUtils.isEmpty(state)) {
      stateCache.put(state.getName(), state);
    }
  }

  private void registerTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      tableCache.put(table.getName(), table);
    }
  }

  private void registerView(BeeView view) {
    if (!BeeUtils.isEmpty(view)) {
      viewCache.put(view.getName(), view);
    }
  }
}
