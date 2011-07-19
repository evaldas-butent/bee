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
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.sql.HasFrom;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlConstants;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlConstants.SqlKeyword;
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
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.XmlState;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlKey;
import com.butent.bee.shared.data.XmlView;
import com.butent.bee.shared.data.XmlView.XmlColumn;
import com.butent.bee.shared.data.XmlView.XmlOrder;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.io.File;
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

  public enum SysObject {
    STATE("states"), TABLE("tables"), VIEW("views");

    private String path;

    private SysObject(String path) {
      this.path = path;
    }

    public String getFileName(String objName) {
      Assert.notEmpty(objName);
      return getPath() +
          BeeUtils.concat(".", objName, name().toLowerCase(), XmlUtils.defaultXmlExtension);
    }

    public String getPath() {
      return path + "/";
    }

    public String getSchema() {
      return name().toLowerCase() + ".xsd";
    }
  }

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
      rebuildTable(table);
    }
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
                  r.setValue(rs.getColumnIndex(fld), changes.getString(row, fld));
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

      if (usr.isUserTable(tblName)) {
        usr.invalidateCache();
      }
    }
    return response;
  }

  public ResponseObject commitExtChanges(BeeTable table, long id, Map<String, Object[]> updates,
      int idxField, int idxValue, boolean updateMode) {
    int c = 0;
    int idxInsQuery = 0;
    int idxUpdQuery = 1;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String col : updates.keySet()) {
      BeeField field = table.getField((String) updates.get(col)[idxField]);
      Object value = updates.get(col)[idxValue];
      String extTable = table.getExtTable(field);

      if (!queryMap.containsKey(extTable)) {
        queryMap.put(extTable, new IsQuery[Ints.max(idxInsQuery, idxUpdQuery) + 1]);
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
      ResponseObject resp = new ResponseObject();
      int res = 0;
      IsQuery updQuery = queries[idxUpdQuery];

      if (!BeeUtils.isEmpty(updQuery)) {
        resp = qs.updateDataWithResponse(updQuery);
        res = resp.getResponse(-1, logger);
      }
      if (res == 0) {
        resp = qs.updateDataWithResponse(queries[idxInsQuery]);
        res = resp.getResponse(-1, logger);
      }
      if (res < 0) {
        return resp;
      }
      c += res;
    }
    return ResponseObject.response(c);
  }

  public ResponseObject commitTranslationChanges(BeeTable table, long id,
      Map<String, Object[]> updates, int idxField, int idxLocale, int idxValue, boolean updateMode) {
    int c = 0;
    int idxInsQuery = 0;
    int idxUpdQuery = 1;
    Map<String, IsQuery[]> queryMap = Maps.newHashMap();

    for (String col : updates.keySet()) {
      BeeField field = table.getField((String) updates.get(col)[idxField]);
      String locale = (String) updates.get(col)[idxLocale];
      Object value = updates.get(col)[idxValue];
      String translationKey = table.getTranslationTable(field) + BeeUtils.parenthesize(locale);

      if (!queryMap.containsKey(translationKey)) {
        queryMap.put(translationKey, new IsQuery[Ints.max(idxInsQuery, idxUpdQuery) + 1]);
      }
      IsQuery[] queries = queryMap.get(translationKey);
      SqlInsert insQuery = (SqlInsert) queries[idxInsQuery];
      queries[idxInsQuery] = table.insertTranslationField(insQuery, id, field, locale, value);

      if (updateMode) {
        SqlUpdate updQuery = (SqlUpdate) queries[idxUpdQuery];
        queries[idxUpdQuery] = table.updateTranslationField(updQuery, id, field, locale, value);
      }
    }
    for (IsQuery[] queries : queryMap.values()) {
      ResponseObject resp = new ResponseObject();
      int res = 0;
      IsQuery updQuery = queries[idxUpdQuery];

      if (!BeeUtils.isEmpty(updQuery)) {
        resp = qs.updateDataWithResponse(updQuery);
        res = resp.getResponse(-1, logger);
      }
      if (res == 0) {
        resp = qs.updateDataWithResponse(queries[idxInsQuery]);
        res = resp.getResponse(-1, logger);
      }
      if (res < 0) {
        return resp;
      }
      c += res;
    }
    return ResponseObject.response(c);
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

    if (!response.hasErrors() && usr.isUserTable(tblName)) {
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
    return ResponseObject.response(qs.getViewData(union, null));
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
        ResponseObject resp = commitExtChanges(table, id, extUpdate, 0, 1, false);
        if (resp.hasErrors()) {
          return resp;
        }
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
    return stateCache.get(BeeUtils.normalize(stateName));
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
      view = getDefaultView(viewName, true);
      registerView(view);
    }
    return view;
  }

  public IsCondition getViewCondition(String viewName, Filter filter) {
    return getView(viewName).getCondition(filter);
  }

  public BeeRowSet getViewData(String viewName, IsCondition condition,
      Order order, int limit, int offset, String... columns) {

    BeeView view = getView(viewName);
    String source = view.getSource();

    SqlSelect ss = getViewQuery(viewName);

    if (!BeeUtils.isEmpty(columns)) {
      ss.resetFields();

      for (String col : columns) {
        ss.addField(view.getAlias(col), view.getField(col), col);
      }
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
    if (limit > 0) {
      ss.setLimit(limit);
    }
    if (offset > 0) {
      ss.setOffset(offset);
    }

    return qs.getViewData(ss, view);
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
    return getView(viewName).getQuery();
  }

  public int getViewSize(String viewName, IsCondition condition) {
    SqlSelect ss = getViewQuery(viewName);

    if (!BeeUtils.isEmpty(condition)) {
      ss.setWhere(SqlUtils.and(ss.getWhere(), condition));
    }
    return qs.dbRowCount(ss);
  }

  public XmlTable getXmlTable(String tableName) {
    Assert.notEmpty(tableName);

    XmlTable xmlTable = getXmlTable(tableName, false);
    XmlTable userTable = getXmlTable(tableName, true);

    if (xmlTable == null) {
      xmlTable = userTable;
    } else {
      xmlTable.protect().merge(userTable);
    }
    return xmlTable;
  }

  public XmlTable getXmlTable(String tableName, boolean userMode) {
    Assert.notEmpty(tableName);
    String resource = SysObject.TABLE.getFileName(tableName);

    if (userMode) {
      resource = Config.getUserPath(resource);
    } else {
      resource = Config.getConfigPath(resource);
    }
    return loadTable(resource);
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
  public boolean initState(String stateName) {
    return initState(stateName, Config.getPath(SysObject.STATE.getFileName(stateName), true));
  }

  @Lock(LockType.WRITE)
  public void initStates() {
    initObjects(SysObject.STATE);
    initTables();
  }

  @Lock(LockType.WRITE)
  public boolean initTable(String tableName) {
    return initTable(tableName, Config.getPath(SysObject.TABLE.getFileName(tableName), true));
  }

  @Lock(LockType.WRITE)
  public void initTables() {
    initObjects(SysObject.TABLE);

    String engine = SqlBuilderFactory.getEngine();
    if (BeeUtils.isEmpty(engine)) {
      engine = BeeUtils.ifString(Config.getProperty("DefaultEngine"), BeeConst.MYSQL);
    }
    initDatabase(engine);
    initViews();
  }

  @Lock(LockType.WRITE)
  public boolean initView(String viewName) {
    return initView(viewName, Config.getPath(SysObject.VIEW.getFileName(viewName), true));
  }

  @Lock(LockType.WRITE)
  public void initViews() {
    initObjects(SysObject.VIEW);
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
          } else {
            c++;
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(extUpdate)) {
          ResponseObject resp =
              commitExtChanges(table, id, extUpdate, idxField, idxNewValue, false);
          int res = resp.getResponse(-1, null);

          if (res < 0) {
            response.addError("Error inserting extended fields");

            for (String err : resp.getErrors()) {
              response.addError(err);
            }
          } else {
            c += res;
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(translationUpdate)) {
          ResponseObject resp = commitTranslationChanges(table, id, translationUpdate,
              idxField, idxLocale, idxNewValue, false);
          int res = resp.getResponse(-1, null);

          if (res < 0) {
            response.addError("Error inserting translation fields");

            for (String err : resp.getErrors()) {
              response.addError(err);
            }
          } else {
            c += res;
          }
        }
        if (!response.hasErrors()) {
          BeeRow newRow = new BeeRow(id, version);

          if (returnAllFields) {
            BeeRowSet newRs = getViewData(view.getName(),
                SqlUtils.equal(tblName, table.getIdName(), id), new Order(), 0, 0);
            newRow.setValues(newRs.getRow(0).getValues());
          }
          response.setResponse(newRow);
          response.addInfo("Insert count:", c);

          if (usr.isUserTable(tblName)) {
            usr.invalidateCache();
          }
        }
      }
    }
    return response;
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

  public void rebuildActiveTables() {
    for (BeeTable table : getTables()) {
      if (table.isActive()) {
        rebuildTable(table);
      }
    }
  }

  public void rebuildTable(String tblName) {
    rebuildTable(getTable(tblName));
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
        rebuildTable(table);
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
          ResponseObject resp = commitExtChanges(table, id, extUpdate, idxField, idxNewValue, true);
          int res = resp.getResponse(-1, null);

          if (res < 0) {
            response.addError("Error updating extended fields");

            for (String err : resp.getErrors()) {
              response.addError(err);
            }
          } else {
            c += res;
          }
        }
        if (!response.hasErrors() && !BeeUtils.isEmpty(translationUpdate)) {
          ResponseObject resp = commitTranslationChanges(table, id, translationUpdate,
              idxField, idxLocale, idxNewValue, true);
          int res = resp.getResponse(-1, null);

          if (res < 0) {
            response.addError("Error updating translation fields");

            for (String err : resp.getErrors()) {
              response.addError(err);
            }
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
            BeeRowSet newRs = getViewData(view.getName(), wh, new Order(), 0, 0);

            if (!newRs.isEmpty()) {
              boolean collision = false;

              for (String col : baseUpdate.keySet()) {
                int colIndex = newRs.getColumnIndex(col);

                if (BeeUtils.equals(newRs.getString(0, colIndex),
                    baseUpdate.get(col)[idxOldValue])) {

                  newRs.getRow(0).setValue(colIndex,
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
            BeeRowSet newRs = getViewData(view.getName(), wh, new Order(), 0, 0);
            newRow.setValues(newRs.getRow(0).getValues());
          }
          response.setResponse(newRow);
          response.addInfo("Update count:", c);

          if (usr.isUserTable(tblName)) {
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

  private void createForeignKeys(Collection<BeeForeignKey> fKeys) {
    for (BeeForeignKey fKey : fKeys) {
      String refTblName = fKey.getRefTable();
      makeStructureChanges(SqlUtils.createForeignKey(fKey.getTable(), fKey.getName(),
          fKey.getKeyField(), refTblName, getIdName(refTblName),
          fKey.isCascade(), fKey.isCascadeDelete()));
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
      } else if (key.isUnique()) {
        index = SqlUtils.createUniqueKey(tblName, keyName, keyFields);
      } else {
        index = SqlUtils.createIndex(tblName, keyName, keyFields);
      }
      makeStructureChanges(index);
    }
  }

  private Map<String, String> createTable(BeeTable table) {
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
        makeStructureChanges(sc);
      } else {
        tblBackup = tblName + "_BAK";
        int c = 0;
        String[] keys = qs.dbKeys(getDbName(), getDbSchema(), tblName, SqlKeyword.UNIQUE_KEY)
            .getColumn(SqlConstants.KEY_NAME);

        for (BeeKey key : table.getKeys()) {
          if (BeeUtils.same(key.getTable(), tblName) && key.isUnique()) {
            if (ArrayUtils.contains(key.getName(), keys)) {
              c++;
            } else {
              update = true;
              LogUtils.warning(logger, "UNIQUE KEY", key.getName(), "NOT IN", keys);
              break;
            }
          }
        }
        if (!update && keys.length > c) {
          update = true;
          LogUtils.warning(logger, "TOO MANY KEYS");
        }
      }
      if (!update) {
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
              update = true;
              LogUtils.warning(logger, "FOREIGN KEY", fKey.getName(), "NOT IN", fKeys);
              break;
            }
          }
        }
        if (!update && fKeys.length > c) {
          update = true;
          LogUtils.warning(logger, "TOO MANY FOREIGN KEYS");
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
          update = true;
          LogUtils.warning(logger, "FIELD COUNT DOESN'T MATCH");
        }
        if (!update) {
          int i = 0;
          for (Map<String, String> oldFieldInfo : oldFields) {
            Map<String, String> newFieldInfo = newFields.getRow(i++);

            for (String info : oldFieldInfo.keySet()) {
              if (!BeeUtils.same(info, SqlConstants.TBL_NAME)
                  && !BeeUtils.equals(oldFieldInfo.get(info), newFieldInfo.get(info))) {
                update = true;
                LogUtils.warning(logger, "FIELD", oldFieldInfo.get(SqlConstants.FLD_NAME) + ":",
                    info, oldFieldInfo.get(info), "!=", newFieldInfo.get(info));
                break;
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

  private BeeView getDefaultView(String tblName, boolean allFields) {
    BeeTable table = getTable(tblName);
    Collection<BeeField> fields = allFields ? table.getFields() : table.getMainFields();
    BeeView view =
        new BeeView(tblName, table.getName(), table.getIdName(), table.getVersionName(), false);

    for (BeeField field : fields) {
      String fld = field.getName();
      view.addColumn(fld, fld, null, tableCache);
      String relTbl = field.getRelation();

      if (!BeeUtils.isEmpty(relTbl) && !BeeUtils.same(relTbl, tblName)) {
        BeeView vw = getDefaultView(relTbl, false);

        for (String colName : vw.getColumns()) {
          view.addColumn(fld + colName, fld + ">" + vw.getExpression(colName), null, tableCache);
        }
      }
    }
    return view;
  }

  private BeeTable getTable(String tblName) {
    Assert.state(isTable(tblName), "Not a base table: " + tblName);
    return tableCache.get(BeeUtils.normalize(tblName));
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
    initStates();
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
    int cNew = 0;
    int cUpd = 0;
    Collection<File> paths = Lists.newArrayList();

    if (Config.CONFIG_DIR != null) {
      paths.add(Config.CONFIG_DIR);
    }
    if (Config.USER_DIR != null) {
      paths.add(Config.USER_DIR);
    }
    if (!BeeUtils.isEmpty(paths)) {
      List<File> resources =
          FileUtils.findFiles(obj.getFileName("*"), paths, null, null, true, true);

      if (!BeeUtils.isEmpty(resources)) {
        for (File resource : resources) {
          String resourcePath = resource.getPath();
          String objectName = NameUtils.getBaseName(resourcePath);
          objectName = objectName.substring(0, objectName.length() - obj.name().length() - 1);
          boolean isNew = false;
          boolean isOk = false;

          switch (obj) {
            case STATE:
              isNew = !isState(objectName);
              isOk = initState(objectName, resourcePath);
              break;
            case TABLE:
              isNew = !isTable(objectName);
              isOk = initTable(objectName, resourcePath);
              break;
            case VIEW:
              isNew = !isView(objectName);
              isOk = initView(objectName, resourcePath);
              break;
          }
          if (isOk) {
            if (isNew) {
              cNew++;
            } else {
              cUpd++;
            }
          }
        }
      }
    }
    if (BeeUtils.isEmpty(cNew + cUpd)) {
      LogUtils.severe(logger, obj.name() + ":", "No descriptions found");
    } else {
      LogUtils.infoNow(logger, obj.name() + ":",
          "Loaded", cNew, "and updated", cUpd, "descriptions");
    }
  }

  private boolean initState(String stateName, String resource) {
    Assert.notEmpty(stateName);
    Assert.notEmpty(resource);
    BeeState state = null;

    XmlState xmlState = loadState(resource);

    if (xmlState != null) {
      if (!BeeUtils.same(xmlState.name, stateName)) {
        LogUtils.warning(logger, "State name doesn't match resource name:",
            xmlState.name, resource);
      } else {
        state = new BeeState(xmlState.name, xmlState.mode, xmlState.checked);
      }
    }
    if (state != null) {
      registerState(state);
      LogUtils.info(logger, "Loaded state", BeeUtils.bracket(state.getName()),
          "description from", resource);
    } else {
      unregisterState(stateName);
    }
    return state != null;
  }

  private boolean initTable(String tableName, String resource) {
    Assert.notEmpty(tableName);
    Assert.notEmpty(resource);
    BeeTable table = null;

    XmlTable xmlTable = loadTable(resource);

    if (xmlTable != null) {
      if (!BeeUtils.same(xmlTable.name, tableName)) {
        LogUtils.warning(logger, "Table name doesn't match resource name:",
            xmlTable.name, resource);
      } else {
        table = new BeeTable(xmlTable.name, xmlTable.idName, xmlTable.versionName);
        String tbl = table.getName();

        for (int i = 0; i < 2; i++) {
          boolean extMode = (i > 0);
          Collection<XmlField> fields = extMode ? xmlTable.extFields : xmlTable.fields;

          if (!BeeUtils.isEmpty(fields)) {
            for (XmlField field : fields) {
              String fldName = field.name;
              boolean notNull = field.notNull;

              if (notNull && extMode) {
                LogUtils.warning(logger, "Extendend fields must bee nullable:", tbl, fldName);
                notNull = false;
              }
              table.addField(fldName, SqlDataType.valueOf(field.type),
                  field.precision, field.scale, notNull,
                  field.unique, field.relation, field.cascade)
                  .setTranslatable(field.translatable)
                  .setExtended(extMode);

              String tblName = table.getField(fldName).getTable();

              if (!BeeUtils.isEmpty(field.relation)) {
                table.addForeignKey(tblName, fldName, field.relation, field.cascade,
                    field.cascade && notNull);
              }
              if (field.unique) {
                table.addKey(true, tblName, fldName);
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
        if (table.isEmpty()) {
          LogUtils.warning(logger, resource, "Table has no fields defined:", tbl);
          table = null;
        }
      }
    }
    if (table != null) {
      String tbl = table.getName();

      if (isTable(tbl)) {
        getTable(tbl).applyChanges(table);
        LogUtils.info(logger, "Updated table", BeeUtils.bracket(tbl), "description from", resource);
      } else {
        registerTable(table);
        LogUtils.info(logger, "Loaded table", BeeUtils.bracket(tbl), "description from", resource);
      }
    } else {
      unregisterTable(tableName);
    }
    return table != null;
  }

  private boolean initView(String viewName, String resource) {
    Assert.notEmpty(viewName);
    Assert.notEmpty(resource);
    BeeView view = null;

    XmlView xmlView = loadView(resource);

    if (xmlView != null) {
      if (!BeeUtils.same(xmlView.name, viewName)) {
        LogUtils.warning(logger, "View name doesn't match resource name:", xmlView.name, resource);
      } else {
        String src = xmlView.source;

        if (!isTable(src)) {
          LogUtils.warning(logger, "Unrecognized view source:", xmlView.name, src);
        } else {
          String idName = getIdName(src);
          String versionName = getVersionName(src);
          view = new BeeView(xmlView.name, getTable(src).getName(), idName, versionName,
              xmlView.readOnly);
          String vw = view.getName();

          if (!BeeUtils.isEmpty(xmlView.columns)) {
            for (XmlColumn col : xmlView.columns) {
              if (BeeUtils.inListSame(col.name, idName, versionName)) {
                LogUtils.warning(logger, "Attempt to use reserved column name:", vw, col.name);
              } else {
                view.addColumn(col.name, col.expression, col.locale, tableCache);
              }
            }
          }
          if (!BeeUtils.isEmpty(xmlView.orders)) {
            for (XmlOrder order : xmlView.orders) {
              if (!view.hasColumn(order.column)) {
                LogUtils.warning(logger, "Unrecognized order column:", vw, order.column);
              } else {
                view.addOrder(order.column, order.descending);
              }
            }
          }
          if (view.isEmpty()) {
            LogUtils.warning(logger, resource, "View has no columns defined:", vw);
            view = null;
          }
        }
      }
    }
    if (view != null) {
      registerView(view);
      LogUtils.info(logger, "Loaded view", BeeUtils.bracket(view.getName()),
          "description from", resource);
    } else {
      unregisterView(viewName);
    }
    return view != null;
  }

  private XmlState loadState(String resource) {
    return XmlUtils.unmarshal(XmlState.class, resource,
        Config.getSchemaPath(SysObject.STATE.getSchema()));
  }

  private XmlTable loadTable(String resource) {
    return XmlUtils.unmarshal(XmlTable.class, resource,
        Config.getSchemaPath(SysObject.TABLE.getSchema()));
  }

  private XmlView loadView(String resource) {
    return XmlUtils.unmarshal(XmlView.class, resource,
        Config.getSchemaPath(SysObject.VIEW.getSchema()));
  }

  private void makeStructureChanges(IsQuery query) {
    if (qs.updateData(query) < 0) {
      Assert.untouchable();
    }
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
        String oldValue = (row.getShadow() == null) ? null : row.getShadow().get(i);
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
  private void rebuildTable(BeeTable table) {
    table.setActive(false);
    String tblMain = table.getName();
    Map<String, String> rebuilds = createTable(table);

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
        String tblBackup = rebuilds.get(tbl);

        if (!BeeUtils.isEmpty(tblBackup)) {
          makeStructureChanges(SqlUtils.dropTable(tbl));
          makeStructureChanges(SqlUtils.renameTable(tblBackup, tbl));
        }
        createKeys(keys);
        createForeignKeys(foreignKeys);
      }
    }
    table.setActive(true);
  }

  private void registerState(BeeState state) {
    if (!BeeUtils.isEmpty(state)) {
      stateCache.put(BeeUtils.normalize(state.getName()), state);
    }
  }

  private void registerTable(BeeTable table) {
    if (!BeeUtils.isEmpty(table)) {
      tableCache.put(BeeUtils.normalize(table.getName()), table);
    }
  }

  private void registerView(BeeView view) {
    if (!BeeUtils.isEmpty(view)) {
      viewCache.put(BeeUtils.normalize(view.getName()), view);
    }
  }

  private void unregisterState(String stateName) {
    if (!BeeUtils.isEmpty(stateName)) {
      stateCache.remove(BeeUtils.normalize(stateName));
    }
  }

  private void unregisterTable(String tableName) {
    if (!BeeUtils.isEmpty(tableName)) {
      tableCache.remove(BeeUtils.normalize(tableName));
    }
  }

  private void unregisterView(String viewName) {
    if (!BeeUtils.isEmpty(viewName)) {
      viewCache.remove(BeeUtils.normalize(viewName));
    }
  }
}
