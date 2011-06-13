package com.butent.bee.server.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
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
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
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

  public void activateTable(String tblName) {
    BeeTable table = getTable(tblName);

    if (!table.isActive()) {
      if (!qs.isDbTable(getDbName(), getDbSchema(), table.getName())) {
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

    BeeView view = getView(changes.getViewName());
    String tblName = view.getSource();
    BeeTable table = getTable(tblName);
    Map<String, BeeField> editableFields = Maps.newHashMap();

    if (view.isReadOnly()) {
      err = "View " + view.getName() + " is read only.";
    } else {
      for (String colName : view.getColumns()) {
        if (view.isEditable(colName)) {
          editableFields.put(colName, getTableField(tblName, view.getField(colName)));
        }
      }
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
        long version = System.currentTimeMillis();
        si.addConstant(table.getVersionName(), version);
        row.setVersion(version);

        long id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
          break;
        }
        c++;
        row.setNewId(id);

        if (!BeeUtils.isEmpty(extUpdate)) {
          int res = -1;// commitExtChanges(table, id, extUpdate, false);

          if (res < 0) {
            err = "Error inserting extended fields";
            break;
          }
          c += res;
        }
        if (!BeeUtils.isEmpty(translationUpdate)) {
          int res = -1;// commitTranslationChanges(table, id, translationUpdate, false);

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
            SqlUtils.equal(tblName, table.getVersionName(), row.getVersion()));

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
            long version = System.currentTimeMillis();
            su.addConstant(table.getVersionName(), version);
            row.setVersion(version);

            int res = qs.updateData(su.setWhere(wh));

            if (res == 0) { // Optimistic lock exception
              BeeRowSet rs = getViewData(view.getName(), null, idWh, null, 0, 0);

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
            int res = -1;// commitExtChanges(table, id, extUpdate, true);

            if (res < 0) {
              err = "Error updating extended fields";
              break;
            }
            c += res;
          }
          if (!BeeUtils.isEmpty(translationUpdate)) {
            int res = -1;// commitTranslationChanges(table, id, translationUpdate, true);

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

      if (BeeUtils.inList(tblName, UserServiceBean.TBL_USERS, UserServiceBean.TBL_ROLES,
          UserServiceBean.TBL_USER_ROLES)) {
        usr.invalidateCache();
      }
    }
    return response;
  }

  public int commitExtChanges(BeeTable table, long id, Map<String, Object[]> updates,
      int idxField, int idxValue, boolean updateMode) {
    int c = 0;
    int idxInsQuery = 0;
    int idxUpdQuery = 1;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String col : updates.keySet()) {
      BeeField field = table.getField((String) updates.get(col)[idxField]);
      Object value = updates.get(col)[idxValue];
      String extTable = field.getTable();

      if (!queryMap.containsKey(extTable)) {
        queryMap.put(extTable, new IsQuery[2]);
      }
      IsQuery[] queries = queryMap.get(extTable);
      SqlInsert insQuery = (SqlInsert) queries[idxInsQuery];
      queries[idxInsQuery] = table.insertExtField(insQuery, id, field, value);

      if (updateMode) {
        SqlUpdate updQuery = (SqlUpdate) queries[idxUpdQuery];
        queries[idxUpdQuery] = table.updateExtField(updQuery, id, field, value);
      }
    }
    for (IsQuery[] queries : queryMap.values()) {
      int res = 0;
      IsQuery updQuery = queries[idxUpdQuery];

      if (!BeeUtils.isEmpty(updQuery)) {
        res = qs.updateData(updQuery);
      }
      if (res == 0) {
        res = qs.updateData(queries[idxInsQuery]);
      }
      if (res < 0) {
        return -1;
      }
      c += res;
    }
    return c;
  }

  public int commitTranslationChanges(BeeTable table, long id, Map<String, Object[]> updates,
      int idxField, int idxLocale, int idxValue, boolean updateMode) {
    int c = 0;
    int idxInsQuery = 0;
    int idxUpdQuery = 1;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String col : updates.keySet()) {
      BeeField field = table.getField((String) updates.get(col)[idxField]);
      String locale = (String) updates.get(col)[idxLocale];
      Object value = updates.get(col)[idxValue];
      String translationTable = table.getTranslationTable(field);

      if (table.updateTranslationActive(field, locale)) {
        rebuildTable(table.getName());
      }
      if (!queryMap.containsKey(translationTable)) {
        queryMap.put(translationTable, new IsQuery[2]);
      }
      IsQuery[] queries = queryMap.get(translationTable);
      SqlInsert insQuery = (SqlInsert) queries[idxInsQuery];
      queries[idxInsQuery] = table.insertTranslationField(insQuery, id, field, locale, value);

      if (updateMode) {
        SqlUpdate updQuery = (SqlUpdate) queries[idxUpdQuery];
        queries[idxUpdQuery] = table.updateTranslationField(updQuery, id, field, locale, value);
      }
    }
    for (IsQuery[] queries : queryMap.values()) {
      int res = 0;
      IsQuery updQuery = queries[idxUpdQuery];

      if (!BeeUtils.isEmpty(updQuery)) {
        res = qs.updateData(updQuery);
      }
      if (res == 0) {
        res = qs.updateData(queries[idxInsQuery]);
      }
      if (res < 0) {
        return -1;
      }
      c += res;
    }
    return c;
  }

  public ResponseObject deleteRow(String viewName, RowInfo row) {
    Assert.notEmpty(viewName);
    Assert.notNull(row);
    BeeView view = getView(viewName);

    if (view.isReadOnly()) {
      return ResponseObject.error("View", view.getName(), "is read only.");
    }
    String tblName = view.getSource();
    IsCondition wh = SqlUtils.equal(tblName, getIdName(tblName), row.getId());

    if (!BeeUtils.isEmpty(row.getVersion())) {
      wh = SqlUtils.and(wh, SqlUtils.equal(tblName, getVersionName(tblName), row.getVersion()));
    }
    ResponseObject response = qs.updateDataWithResponse(new SqlDelete(tblName).setWhere(wh));

    if (!response.hasErrors()
        && BeeUtils.inList(tblName, UserServiceBean.TBL_USERS, UserServiceBean.TBL_ROLES,
            UserServiceBean.TBL_USER_ROLES)) {
      usr.invalidateCache();
    }
    return response;
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
    return ResponseObject.response(qs.getViewData(union, null, null));
  }

  public ResponseObject generateData(String tblName, int rowCount) {
    Assert.isTrue(isTable(tblName), "Not a base table: " + tblName);
    Assert.isPositive(rowCount, "rowCount must be positive");
    BeeTable table = getTable(tblName);

    Collection<BeeField> fields = getTableFields(tblName);
    SqlInsert si = new SqlInsert(table.getName());
    Map<String, Object[]> extUpdate = Maps.newHashMap();
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
              v = (random.nextBoolean() ? -1 : 1)
                  * Math.random() * Math.pow(10, BeeUtils.randomInt(-7, 20));
              break;
            case INTEGER:
              v = random.nextInt();
              break;
            case LONG:
              v = random.nextLong() / random.nextInt();
              break;
            case DECIMAL:
              if (field.getPrecision() <= 1) {
                v = random.nextInt(10);
              } else {
                double x = (random.nextBoolean() ? -1 : 1)
                    * Math.random() * Math.pow(10, BeeUtils.randomInt(0, field.getPrecision()));
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
            extUpdate.put(field.getName(), new Object[] {field.getName(), v});
          }
        } else {
          si.addConstant(field.getName(), v);
        }
      }
      long id = qs.insertData(si);

      if (id < 0) {
        return ResponseObject.error(tblName, si.getQuery(), "Error inserting data");
      } else {
        commitExtChanges(table, id, extUpdate, 0, 1, false);
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

  public String getRelation(String tblName, String fldName) {
    return getTableField(tblName, fldName).getRelation();
  }

  public BeeState getState(String stateName) {
    Assert.state(isState(stateName), "Not a state: " + stateName);
    return stateCache.get(cacheKey(stateName));
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
    Assert.state(isView(viewName) || isTable(viewName), "Not a view: " + viewName);
    BeeView view = viewCache.get(cacheKey(viewName));

    if (BeeUtils.isEmpty(view)) {
      view = getDefaultView(viewName, true);
      registerView(view);
    }
    return view;
  }

  public IsCondition getViewCondition(String viewName, Filter filter) {
    return getView(viewName).getCondition(filter);
  }

  public BeeRowSet getViewData(String viewName, String columnName, IsCondition condition,
      Order order, int limit, int offset, String... states) {

    BeeView view = getView(viewName);
    String source = view.getSource();

    SqlSelect ss;
    Integer columnCount;
    
    if (BeeUtils.isEmpty(columnName)) {
      ss = getViewQuery(viewName);
      columnCount = null;
    } else {
      ss = new SqlSelect().addFields(source, columnName).addFrom(source);
      columnCount = 1;
    }

    if (!BeeUtils.isEmpty(condition)) {
      ss.setWhere(SqlUtils.and(ss.getWhere(), condition));
    }

    if (order != null) {
      ss.resetOrder();
      String idCol = getIdName(source);
      boolean hasId = false;

      for (Order.Column col : order.getColumns()) {
        String als = view.getAlias(col.getSource());

        if (!BeeUtils.isEmpty(als)) {
          String fld = view.getField(col.getSource());

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
    
    return qs.getViewData(ss, view, columnCount);
  }

  public List<DataInfo> getViewList() {
    List<DataInfo> lst = Lists.newArrayList();
    Set<String> views = Sets.newHashSet(getViewNames());
    views.addAll(getTableNames());

    for (String vw : views) {
      BeeTable source = getTable(getView(vw).getSource());
      int cnt = -1;

      if (source.isActive()) {
        cnt = getViewSize(vw, null);
      }
      lst.add(new DataInfo(vw, source.getIdName(), source.getVersionName(), cnt));
    }
    return lst;
  }

  public Collection<String> getViewNames() {
    Collection<String> views = Lists.newArrayList();

    for (BeeView view : viewCache.values()) {
      views.add(view.getName());
    }
    return views;
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

  public ResponseObject insertRow(BeeRowSet rs, boolean returnAllFields) {
    Assert.notNull(rs);
    ResponseObject response = new ResponseObject();

    if (!BeeUtils.isPositive(rs.getNumberOfColumns())) {
      response.addError("Nothing to insert");

    } else if (rs.getNumberOfRows() != 1) {
      response.addError("Can insert only one row at a time");

    } else {
      BeeView view = getView(rs.getViewName());
      String tblName = view.getSource();
      BeeTable table = getTable(tblName);

      if (view.isReadOnly()) {
        response.addError("View", view.getName(), "is read only.");
      } else {
        Map<String, Object[]> baseUpdate = Maps.newHashMap();
        Map<String, Object[]> extUpdate = Maps.newHashMap();
        Map<String, Object[]> translationUpdate = Maps.newHashMap();
        int idxField = 0;
        int idxLocale = 1;
        int idxOldValue = 2;
        int idxNewValue = 3;

        prepareRow(rs, response, baseUpdate, extUpdate, translationUpdate,
            idxField, idxLocale, idxOldValue, idxNewValue);

        int c = 0;
        long id = 0;
        long version = System.currentTimeMillis();

        if (!response.hasErrors()) {
          SqlInsert si = new SqlInsert(tblName);

          for (String col : baseUpdate.keySet()) {
            si.addConstant((String) baseUpdate.get(col)[idxField],
                  baseUpdate.get(col)[idxNewValue]);
          }
          si.addConstant(table.getVersionName(), version);
          ResponseObject resp = qs.insertDataWithResponse(si);
          id = resp.getResponse(-1L, logger);

          if (id < 0) {
            response.addError("Error inserting row");

            for (String err : resp.getErrors()) {
              response.addError(err);
            }
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(extUpdate)) {
          int res = commitExtChanges(table, id, extUpdate, idxField, idxNewValue, false);

          if (res < 0) {
            response.addError("Error inserting extended fields");
          }
          c += res;
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(translationUpdate)) {
          int res = commitTranslationChanges(table, id, translationUpdate,
                idxField, idxLocale, idxNewValue, false);

          if (res < 0) {
            response.addError("Error inserting translation fields");
          }
          c += res;
        }
        if (!response.hasErrors()) {
          BeeRow newRow = new BeeRow(id, version);

          if (returnAllFields) {
            BeeRowSet newRs = getViewData(view.getName(), null, 
                SqlUtils.equal(tblName, table.getIdName(), id), new Order(), 0, 0);
            newRow.setValues(newRs.getRow(0).getValues());
          }
          response.setResponse(newRow);
          response.addInfo("Insert count:", c);

          if (BeeUtils.inList(tblName, UserServiceBean.TBL_USERS, UserServiceBean.TBL_ROLES,
              UserServiceBean.TBL_USER_ROLES)) {
            usr.invalidateCache();
          }
        }
      }
    }
    return response;
  }

  public boolean isState(String stateName) {
    return !BeeUtils.isEmpty(stateName) && stateCache.containsKey(cacheKey(stateName));
  }

  public boolean isTable(String tblName) {
    return !BeeUtils.isEmpty(tblName) && tableCache.containsKey(cacheKey(tblName));
  }

  public boolean isView(String viewName) {
    return !BeeUtils.isEmpty(viewName) && viewCache.containsKey(cacheKey(viewName));
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
    BeeTable table = getTable(tblName);
    rebuildTable(table, qs.isDbTable(getDbName(), getDbSchema(), table.getName()));
  }

  public int restoreTable(String tblName, String tmp) {
    int rc = 0;

    if (!BeeUtils.isEmpty(tmp)) {
      String[] tmpFields = qs.dbFields(tmp);
      Collection<String> srcFlds = Lists.newArrayList();
      Collection<String> dstFlds = Lists.newArrayList();

      for (String fld : qs.dbFields(tblName)) {
        for (String tmpFld : tmpFields) {
          if (BeeUtils.same(fld, tmpFld)) {
            srcFlds.add(tmpFld);
            dstFlds.add(fld);
          }
        }
      }
      if (!BeeUtils.isEmpty(srcFlds)) {
        rc = qs.updateData(new SqlInsert(tblName)
            .addFields(dstFlds.toArray(new String[0]))
            .setDataSource(new SqlSelect()
                .addFields(tmp, srcFlds.toArray(new String[0])).addFrom(tmp)));
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

  public ResponseObject updateRow(BeeRowSet rs, boolean returnAllFields) {
    Assert.notNull(rs);
    ResponseObject response = new ResponseObject();

    if (!BeeUtils.isPositive(rs.getNumberOfColumns())) {
      response.addError("Nothing to update");

    } else if (rs.getNumberOfRows() != 1) {
      response.addError("Can update only one row at a time");

    } else {
      BeeView view = getView(rs.getViewName());
      String tblName = view.getSource();
      BeeTable table = getTable(tblName);

      if (view.isReadOnly()) {
        response.addError("View", view.getName(), "is read only.");
      } else {
        Map<String, Object[]> baseUpdate = Maps.newHashMap();
        Map<String, Object[]> extUpdate = Maps.newHashMap();
        Map<String, Object[]> translationUpdate = Maps.newHashMap();
        int idxField = 0;
        int idxLocale = 1;
        int idxOldValue = 2;
        int idxNewValue = 3;

        prepareRow(rs, response, baseUpdate, extUpdate, translationUpdate,
            idxField, idxLocale, idxOldValue, idxNewValue);

        int c = 0;
        long id = rs.getRow(0).getId();
        long oldVersion = rs.getRow(0).getVersion();
        BeeRow newRow = new BeeRow(id, oldVersion);
        IsCondition wh = SqlUtils.equal(tblName, table.getIdName(), id);

        if (!response.hasErrors() && !BeeUtils.isEmpty(extUpdate)) {
          int res = commitExtChanges(table, id, extUpdate, idxField, idxNewValue, true);

          if (res < 0) {
            response.addError("Error updating extended fields");
          } else {
            c += res;
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(translationUpdate)) {
          int res = commitTranslationChanges(table, id, translationUpdate,
                idxField, idxLocale, idxNewValue, true);

          if (res < 0) {
            response.addError("Error updating translation fields");
          } else {
            c += res;
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(baseUpdate)) {
          newRow.setVersion(System.currentTimeMillis());

          SqlUpdate su = new SqlUpdate(tblName)
                .addConstant(table.getVersionName(), newRow.getVersion());

          for (String col : baseUpdate.keySet()) {
            su.addConstant((String) baseUpdate.get(col)[idxField],
                  baseUpdate.get(col)[idxNewValue]);
          }
          ResponseObject resp = qs.updateDataWithResponse(su.setWhere(SqlUtils.and(wh,
              SqlUtils.equal(tblName, table.getVersionName(), oldVersion))));

          int res = resp.getResponse(-1, logger);

          if (res == 0) { // Optimistic lock exception
            BeeRowSet newRs = getViewData(view.getName(), null, wh, new Order(), 0, 0);

            if (!newRs.isEmpty()) {
              boolean collision = false;

              for (String col : baseUpdate.keySet()) {
                int colIndex = newRs.getColumnIndex(col);

                if (BeeUtils.equals(newRs.getString(0, colIndex),
                    baseUpdate.get(col)[idxOldValue])) {

                  newRs.setValue(0, colIndex,
                      BeeUtils.transformNoTrim(baseUpdate.get(col)[idxNewValue]));
                } else {
                  collision = true;
                  newRs.rollback();
                  break;
                }
              }
              if (collision) {
                response.setResponse(newRs.getRow(0));
              } else {
                newRow.setValues(newRs.getRow(0).getValues());
                resp = qs.updateDataWithResponse(su.setWhere(wh));
                res = resp.getResponse(-1, logger);
              }
            }
          }
          if (res > 0) {
            c += res;
          } else {
            response.addError("Error updating row:", id);

            if (res < 0) {
              for (String err : resp.getErrors()) {
                response.addError(err);
              }
            } else {
              response.addError("Optimistic lock exception");
            }
          }
        }
        if (!response.hasErrors()) {
          if (returnAllFields && BeeUtils.isEmpty(newRow.getValues())) {
            BeeRowSet newRs = getViewData(view.getName(), null, wh, new Order(), 0, 0);
            newRow.setValues(newRs.getRow(0).getValues());
          }
          response.setResponse(newRow);
          response.addInfo("Update count:", c);

          if (BeeUtils.inList(tblName, UserServiceBean.TBL_USERS, UserServiceBean.TBL_ROLES,
              UserServiceBean.TBL_USER_ROLES)) {
            usr.invalidateCache();
          }
        }
      }
    }
    return response;
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

  private String cacheKey(String objectName) {
    Assert.notEmpty(objectName);
    return objectName.trim().toLowerCase();
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
          .addLong(table.getVersionName(), Keyword.NOT_NULL)
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
    return tableCache.get(cacheKey(tblName));
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
          , table.getAttribute("versionName"));

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
          , getTable(src).getName(), getIdName(src)
          , BeeUtils.toBoolean(view.getAttribute("readOnly")));

      NodeList nodeRoot = view.getElementsByTagName("BeeColumns");

      if (nodeRoot.getLength() > 0) {
        NodeList cols = ((Element) nodeRoot.item(0)).getElementsByTagName("BeeColumn");

        for (int j = 0; j < cols.getLength(); j++) {
          Element col = (Element) cols.item(j);
          String name = col.getAttribute("name");

          if (BeeUtils.inListSame(name, getIdName(src), getVersionName(src))) {
            LogUtils.warning(logger, "Attempt to use reserved column name:", vw.getName(), name);
          } else {
            vw.addField(name
                , col.getAttribute("expression")
                , col.getAttribute("locale")
                , tableCache);
          }
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

  private void prepareRow(BeeRowSet rs, ResponseObject response, Map<String, Object[]> baseUpdate,
      Map<String, Object[]> extUpdate, Map<String, Object[]> translationUpdate,
      int idxField, int idxLocale, int idxOldValue, int idxNewValue) {

    BeeView view = getView(rs.getViewName());
    BeeRow row = rs.getRow(0);

    for (int i = 0; i < rs.getNumberOfColumns(); i++) {
      ValueType colType = rs.getColumnType(i);
      String colName = rs.getColumnId(i);

      if (!view.hasColumn(colName) || !view.isEditable(colName)) {
        response.addError("Column", colName, "is read only.");
      } else {
        String fldName = view.getField(colName);
        String locale = view.getLocale(colName);
        BeeField field = getTableField(view.getSource(), fldName);
        String oldValue = row.getShadow().get(i);
        Object newValue = Value.parseValue(colType, row.getString(i), false).getObjectValue();

        Object[] arr = new Object[Ints.max(idxField, idxLocale, idxOldValue, idxNewValue) + 1];
        arr[idxField] = fldName;
        arr[idxOldValue] = oldValue;
        arr[idxNewValue] = newValue;
        arr[idxLocale] = locale;

        if (!BeeUtils.isEmpty(locale)) {
          translationUpdate.put(colName, arr);
        } else if (field.isExtended()) {
          extUpdate.put(colName, arr);
        } else {
          baseUpdate.put(colName, arr);
        }
      }
    }
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
      stateCache.put(cacheKey(state.getName()), state);
    }
  }

  private void registerTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      tableCache.put(cacheKey(table.getName()), table);
    }
  }

  private void registerView(BeeView view) {
    if (!BeeUtils.isEmpty(view)) {
      viewCache.put(cacheKey(view.getName()), view);
    }
  }
}
