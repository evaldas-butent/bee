package com.butent.bee.server.data;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.server.data.BeeTable.BeeKey;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.data.ViewEvent.ViewDeleteEvent;
import com.butent.bee.server.data.ViewEvent.ViewInsertEvent;
import com.butent.bee.server.data.ViewEvent.ViewUpdateEvent;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.Defaults.DefaultExpression;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.SqlConstants.SqlKeyword;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
@LocalBean
@TransactionAttribute(TransactionAttributeType.MANDATORY)
public class DataEditorBean {
  private static class FieldInfo {
    private final String tableAlias;
    private final String fieldAlias;
    private final String fieldName;
    private final String oldValue;
    private Object newValue;
    private final String locale;

    private FieldInfo(String tableAlias, String fieldAlias, String fieldName,
        String oldValue, Object newValue, String locale) {
      this.tableAlias = tableAlias;
      this.fieldAlias = fieldAlias;
      this.fieldName = fieldName;
      this.oldValue = oldValue;
      this.newValue = newValue;
      this.locale = locale;
    }
  }

  private static class TableInfo {
    private final String tableAlias;
    private final String tableName;
    private final String relation;
    private final List<FieldInfo> fields = Lists.newArrayList();
    private Long id;
    private Long version;

    private TableInfo(String tableAlias, String tableName, String relation) {
      this.tableAlias = tableAlias;
      this.tableName = tableName;
      this.relation = relation;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(".\ntableAlias: " + tableAlias)
          .append("\ntableName: " + tableName)
          .append("\nrelation: " + relation)
          .append("\nid: " + id)
          .append("\nversion: " + version);

      for (FieldInfo info : fields) {
        sb.append("\n\tfieldAlias: " + info.fieldAlias)
            .append("\n\ttableAlias: " + info.tableAlias)
            .append("\n\tfieldName: " + info.fieldName)
            .append("\n\toldValue: " + info.oldValue)
            .append("\n\tnewValue: " + info.newValue)
            .append("\n\tlocale: " + info.locale);
      }
      return sb.append("\n.").toString();
    }
  }

  private static BeeLogger logger = LogUtils.getLogger(DataEditorBean.class);

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @EJB
  ServerDefaults srvDef;
  @Resource
  EJBContext ctx;

  public ResponseObject commitRow(BeeRowSet rs, boolean returnAllFields) {
    Assert.notNull(rs);
    if (rs.getNumberOfRows() != 1) {
      return ResponseObject.error("Can commit only one row at a time");
    }
    return commitRow(rs, 0, returnAllFields ? BeeRow.class : RowInfo.class);
  }

  public ResponseObject commitRow(BeeRowSet rs, int rowIndex, Class<?> returnType) {
    Assert.notNull(rs);
    if (!BeeUtils.betweenExclusive(rowIndex, 0, rs.getNumberOfRows())) {
      return ResponseObject.error("commit row: row index", rowIndex, "row count",
          rs.getNumberOfRows());
    }

    ResponseObject response = new ResponseObject();
    ViewEvent event = null;
    BeeView view = sys.getView(rs.getViewName());
    BeeRow row = rs.getRow(rowIndex);
    Map<String, TableInfo> updates = Maps.newHashMap();

    if (!BeeUtils.isPositive(rs.getNumberOfColumns())) {
      response.addError("Nothing to commit");

    } else if (view.isReadOnly()) {
      response.addError("View", BeeUtils.bracket(view.getName()), "is read only.");

    } else {
      if (row.getId() == DataUtils.NEW_ROW_ID) {
        event = new ViewInsertEvent(rs.getViewName(), rs.getColumns(), row);
      } else {
        event = new ViewUpdateEvent(rs.getViewName(), rs.getColumns(), row);
      }
      sys.postViewEvent(event);

      if (event.hasErrors()) {
        for (String error : event.getErrorMessages()) {
          response.addError(error);
        }
      }
      if (!response.hasErrors()) {
        for (int i = 0; i < rs.getNumberOfColumns(); i++) {
          String colName = rs.getColumnId(i);

          if (!view.hasColumn(colName)) {
            response.addError("Unknown column:", BeeUtils.bracket(colName));
            break;
          } else if (view.isColReadOnly(colName)) {
            response.addError("Column:", BeeUtils.bracket(colName), "is read only.");
            break;
          } else {
            String oldValue = row.getShadowString(i);
            ValueType colType = view.getColumnType(colName).toValueType();
            Object newValue = Value.parseValue(colType, row.getString(i), false).getObjectValue();
            String locale = view.getColumnLocale(colName);

            if (!registerField(colName,
                new FieldInfo(view.getColumnSource(colName), colName, view.getColumnField(colName),
                    oldValue, newValue, locale), updates, view, response)) {
              break;
            }
          }
        }
      }
    }
    TableInfo tblInfo = null;
    Long id = null;

    if (!response.hasErrors()) {
      for (TableInfo info : updates.values()) {
        if (BeeUtils.isEmpty(info.relation)) {
          tblInfo = info;
          break;
        }
      }
      Assert.notNull(tblInfo);
      id = row.getId();

      if (DataUtils.isId(id)) {
        tblInfo.id = id;
        tblInfo.version = row.getVersion();

        if (updates.size() > 1 && !refreshUpdates(updates, view)) {
          response.addError("Optimistic lock exception");
          logger.warning("refreshUpdates:", view.getName(), updates);
        }
      }
      if (!response.hasErrors()) {
        id = commitTable(tblInfo, updates, view, response);
      }
      if (!response.hasErrors()) {
        if (RowInfo.class.equals(returnType)) {
          response.setResponse(new BeeRow(id, tblInfo.version));
        } else {
          BeeRowSet newRs = qs.getViewData(view.getName(), ComparisonFilter.compareId(id));

          if (newRs.isEmpty()) {
            response.addError("Optimistic lock exception");
            logger.warning("commitRow:", view.getName(), id, "row set empty");
          } else if (BeeRowSet.class.equals(returnType)) {
            response.setResponse(newRs);
          } else {
            response.setResponse(newRs.getRow(0));
          }
        }
      }
    }
    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    } else {
      row.setVersion(tblInfo.version);

      if (row.getId() == DataUtils.NEW_ROW_ID) {
        row.setId(id);
      }
      event.setAfter();
      sys.postViewEvent(event);
    }
    return response;
  }

  public ResponseObject deleteRows(String viewName, RowInfo[] rows) {
    Assert.notEmpty(viewName);
    Assert.noNulls((Object[]) rows);
    BeeView view = sys.getView(viewName);
    String tblName = view.getSourceName();
    HasConditions wh = SqlUtils.or();

    if (view.isReadOnly()) {
      return ResponseObject.error("View", BeeUtils.bracket(view.getName()), "is read only.");
    }
    List<Long> ids = Lists.newArrayList();

    for (RowInfo row : rows) {
      ids.add(row.getId());
      IsCondition whId = SqlUtils.equals(tblName, view.getSourceIdName(), row.getId());
      long version = row.getVersion();

      if (version > 0) {
        wh.add(SqlUtils.and(whId, SqlUtils.equals(tblName, view.getSourceVersionName(), version)));
      } else {
        wh.add(whId);
      }
    }
    ResponseObject response;
    ViewDeleteEvent deleteEvent = new ViewDeleteEvent(view.getName(), ids);
    sys.postViewEvent(deleteEvent);

    if (deleteEvent.hasErrors()) {
      response = new ResponseObject();

      for (String error : deleteEvent.getErrorMessages()) {
        response.addError(error);
      }
    } else {
      response = qs.updateDataWithResponse(new SqlDelete(tblName).setWhere(wh));
      int cnt = response.getResponse(-1, logger);

      if (cnt < rows.length && !response.hasErrors()) {
        String err = "Optimistic lock exception";
        logger.severe(err, "Deleted", cnt, "of", rows.length);
        response.addError(err);
        response.setResponse(0);
      }
    }
    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    } else {
      deleteEvent.setAfter();
      sys.postViewEvent(deleteEvent);
    }
    return response;
  }

  public ResponseObject generateData(String tbl, int rowCount, int refCount, int childCount,
      Set<String> cache) {
    Assert.isTrue(sys.isTable(tbl), "Not a base table: " + tbl);
    Assert.isPositive(rowCount, "rowCount must be positive");
    BeeTable table = sys.getTable(tbl);
    String tblName = table.getName();
    ResponseObject response = new ResponseObject();

    if (cache == null) {
      cache = Sets.newHashSet();
    } else if (cache.contains(BeeUtils.normalize(tblName))) {
      return response.addWarning(tblName, "already generated");
    }
    cache.add(BeeUtils.normalize(tblName));

    Collection<BeeField> fields = sys.getTableFields(tblName);
    SqlInsert si = new SqlInsert(tblName);
    Map<String, String[]> relations = Maps.newHashMap();
    Map<List<String>, List<String[]>> uniques = Maps.newHashMap();
    Map<String, Object> updates = Maps.newHashMap();
    List<FieldInfo> extUpdate = Lists.newArrayList();

    Map<String, List<String>> uniqueKeys = Maps.newHashMap();

    for (BeeKey key : table.getKeys()) {
      if (key.isUnique()) {
        List<String> keyFields = Lists.newArrayList();

        for (String keyField : key.getKeyFields()) {
          if (!BeeUtils.same(keyField, sys.getIdName(tblName))
              && table.getField(keyField) instanceof BeeRelation) {
            keyFields.add(keyField);
          } else {
            keyFields.clear();
            break;
          }
        }
        for (String keyField : keyFields) {
          uniqueKeys.put(keyField, keyFields);
        }
      }
    }
    int minDay = new JustDate().getDays() - 1000;
    int maxDay = new JustDate().getDays() + 200;
    long minTime = new DateTime().getTime() - 1000L * TimeUtils.MILLIS_PER_DAY;
    long maxTime = new DateTime().getTime() + 200L * TimeUtils.MILLIS_PER_DAY;
    StringBuilder chars = new StringBuilder();
    for (char c = 'a'; c <= 'z'; c++) {
      chars.append(c);
    }
    chars.append(chars.toString().toUpperCase()).append(" ąčęėįšųūžĄČĘĖĮŠŲŪŽ");

    long id = 0;
    boolean stop = false;
    int cnt = 0;
    Random random = new Random();

    for (int row = 0; row < rowCount; row++) {
      si.reset();
      updates.clear();
      extUpdate.clear();

      for (BeeField field : fields) {
        String fldName = field.getName();
        Object v = null;

        if (updates.containsKey(fldName)) {
          continue;
        }
        if (field.isNotNull() || random.nextInt(7) != 0) {
          if (field instanceof BeeRelation) {
            String relation = ((BeeRelation) field).getRelation();

            if (((BeeRelation) field).isEditable()) {
              cache.remove(BeeUtils.normalize(relation));
              ResponseObject resp = generateData(relation, 1, refCount, childCount, cache);
              response.addMessages(resp.getMessages());

              v = resp.getResponse(-1L, logger);

              if ((Long) v < 0) {
                return response;
              }
            } else {
              String[] rs = relations.get(relation);

              if (!relations.containsKey(relation)) {
                rs = qs.getColumn(new SqlSelect()
                    .addFields(relation, sys.getIdName(relation))
                    .addFrom(relation));

                if (ArrayUtils.isEmpty(rs) && BeeUtils.isPositive(refCount)) {
                  ResponseObject resp = generateData(relation, random.nextInt(refCount) + 1,
                      refCount, childCount, cache);
                  response.addMessages(resp.getMessages());

                  if (resp.hasErrors()) {
                    return response;
                  } else {
                    rs = qs.getColumn(new SqlSelect()
                        .addFields(relation, sys.getIdName(relation))
                        .addFrom(relation));
                  }
                }
                relations.put(relation, rs);
              }
              if (!ArrayUtils.isEmpty(rs)) {
                if (uniqueKeys.containsKey(fldName)) {
                  List<String> key = uniqueKeys.get(fldName);
                  List<String[]> unq;
                  boolean lastKey = true;

                  for (String keyFld : key) {
                    if (!BeeUtils.same(keyFld, fldName) && !updates.containsKey(keyFld)) {
                      lastKey = false;
                      break;
                    }
                  }
                  if (!lastKey) {
                    updates.put(fldName, null);
                    continue;
                  }
                  if (uniques.containsKey(key)) {
                    unq = uniques.get(key);
                  } else {
                    HasConditions join = SqlUtils.and();
                    SqlSelect ss = new SqlSelect();

                    for (int i = 0; i < key.size(); i++) {
                      BeeRelation rel = (BeeRelation) table.getField(key.get(i));
                      String relTbl = rel.getRelation();
                      String idName = sys.getIdName(relTbl);

                      ss.addFields(relTbl, idName)
                          .addFrom(relTbl);

                      join.add(SqlUtils.join(tblName, rel.getName(), "subq", idName));
                    }
                    unq = qs.getData(new SqlSelect()
                        .addAllFields("subq")
                        .addFrom(ss, "subq")
                        .addFromLeft(tblName, join)
                        .setWhere(SqlUtils.isNull(tblName, sys.getIdName(tblName)))
                        .setLimit(1000000))
                        .getRows();

                    uniques.put(key, unq);
                  }
                  if (!BeeUtils.isEmpty(unq)) {
                    int idx = random.nextInt(unq.size());
                    String[] vals = unq.get(idx);
                    unq.remove(idx);

                    for (int i = 0; i < key.size(); i++) {
                      if (BeeUtils.same(key.get(i), fldName)) {
                        v = vals[i];
                      } else {
                        updates.put(key.get(i), vals[i]);
                      }
                    }
                  }
                } else {
                  v = BeeUtils.toInt(rs[random.nextInt(rs.length)]);
                }
              }
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
                v = Math.random() * Math.pow(10, random.nextInt(8));
                break;
              case INTEGER:
              case LONG:
                v = random.nextInt(10000000);
                break;
              case DECIMAL:
                if (field.getPrecision() <= 1) {
                  v = random.nextInt(10);
                } else {
                  double x = Math.random() * Math.pow(10, random.nextInt(field.getPrecision()));
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
                  int minLen = field.isUnique() ? Math.min(10, len) : 1;
                  len = BeeUtils.randomInt(minLen, len + 1);
                }
                v = BeeUtils.randomString(len, chars);
                break;
              case TEXT:
                v = BeeUtils.randomString(BeeUtils.randomInt(1, 2000), chars);
                break;
            }
          }
        }
        if (field.isNotNull() && v == null) {
          stop = true;
          break;
        }
        updates.put(fldName, v);
      }
      if (stop) {
        break;
      }
      for (String fldName : updates.keySet()) {
        Object v = updates.get(fldName);

        if (table.getField(fldName).isExtended()) {
          if (v != null) {
            extUpdate.add(new FieldInfo(null, null, fldName, null, v, null));
          }
        } else {
          si.addConstant(fldName, v);
        }
      }
      id = qs.insertData(si);

      if (id < 0) {
        return response.addError("Error inserting data:", si.getQuery());
      } else {
        ResponseObject resp = commitExtensions(tblName, id, extUpdate, null);
        if (resp.hasErrors()) {
          return response.addMessages(resp.getMessages());
        }
        cnt++;
      }
    }
    if (cnt < rowCount) {
      response.addWarning(tblName, ": generated", cnt, "of", rowCount, "rows");
    } else {
      if (rowCount == 1) {
        return response.setResponse(id);
      }
      response.addInfo(tblName, ": generated", cnt, "rows");
    }
    if (BeeUtils.isPositive(childCount)) {
      for (String child : sys.getTableNames()) {
        if (!BeeUtils.same(child, tblName)) {
          for (BeeForeignKey fKey : sys.getTable(child).getForeignKeys()) {
            if (BeeUtils.same(fKey.getRefTable(), tblName)
                && fKey.getCascade() == SqlKeyword.DELETE) {

              ResponseObject resp = generateData(child,
                  BeeUtils.randomInt(rowCount, rowCount * childCount), refCount, childCount,
                  cache);
              response.addMessages(resp.getMessages());

              if (resp.hasErrors()) {
                return response;
              }
            }
          }
        }
      }
    }
    return response;
  }

  public void setState(String tblName, RightsState state, long id, long... bits) {
    BeeTable table = sys.getTable(tblName);

    Map<Long, Boolean> bitMap = Maps.newHashMap();

    for (long bit : usr.getRoles()) {
      bitMap.put(bit, bits == null || Longs.contains(bits, bit));
    }
    if (table.activateState(state, bitMap.keySet())) {
      sys.rebuildTable(table.getName());
    }
    SqlUpdate su = table.updateState(id, state, bitMap);

    if (su != null && qs.updateData(su) == 0) {
      qs.updateData(table.insertState(id, state, bitMap));
    }
  }

  private ResponseObject commitExtensions(String tblName, long id, List<FieldInfo> updates,
      Map<String, TableInfo> aliases) {
    int c = 0;
    Map<String, IsQuery> queryMap = Maps.newHashMap();
    BeeTable table = sys.getTable(tblName);

    for (FieldInfo fldInfo : updates) {
      BeeField field = table.getField(fldInfo.fieldName);
      Object value = fldInfo.newValue;
      String extensionKey = table.getExtTable(field);

      IsQuery query = queryMap.get(extensionKey);

      if (aliases != null && Objects.equal(aliases.get(fldInfo.tableAlias).id, id)) {
        query = table.updateExtField((SqlUpdate) query, id, field, value);
      } else {
        query = table.insertExtField((SqlInsert) query, id, field, value);
      }
      queryMap.put(extensionKey, query);
    }
    for (IsQuery query : queryMap.values()) {
      ResponseObject resp = qs.updateDataWithResponse(query);
      int res = resp.getResponse(-1, logger);

      if (res < 0) {
        return resp;
      }
      c += res;
    }
    return ResponseObject.response(c);
  }

  private Long commitTable(TableInfo tblInfo, Map<String, TableInfo> updates, BeeView view,
      ResponseObject response) {

    Assert.notNull(tblInfo);
    String tblName = tblInfo.tableName;

    List<FieldInfo> baseUpdate = Lists.newArrayList();
    List<FieldInfo> extUpdate = Lists.newArrayList();
    List<FieldInfo> translationUpdate = Lists.newArrayList();

    for (FieldInfo fldInfo : tblInfo.fields) {
      if (BeeUtils.isEmpty(fldInfo.fieldAlias)) {
        TableInfo relInfo = null;

        for (TableInfo info : updates.values()) {
          if (BeeUtils.same(BeeUtils.join(".", tblInfo.tableAlias, fldInfo.fieldName),
              info.relation)) {
            relInfo = info;
            break;
          }
        }
        Long id = commitTable(relInfo, updates, view, response);

        if (response.hasErrors()) {
          break;
        } else if (!DataUtils.isId(relInfo.id)) {
          fldInfo.newValue = id;
        } else {
          continue;
        }
      }
      if (!BeeUtils.isEmpty(fldInfo.locale)) {
        translationUpdate.add(fldInfo);
      } else if (sys.isExtField(tblName, fldInfo.fieldName)) {
        extUpdate.add(fldInfo);
      } else {
        baseUpdate.add(fldInfo);
      }
    }
    int c = 0;
    Long id = tblInfo.id;
    long version = System.currentTimeMillis();

    if (!response.hasErrors()) {
      String idName = sys.getIdName(tblName);
      String verName = sys.getVersionName(tblName);

      if (!DataUtils.isId(id)) { // INSERT
        SqlInsert si = new SqlInsert(tblName).addConstant(verName, version);

        for (FieldInfo col : baseUpdate) {
          si.addConstant(col.fieldName, col.newValue);
        }
        Map<String, Pair<DefaultExpression, Object>> defaults = sys.getTableDefaults(tblName);

        if (!BeeUtils.isEmpty(defaults)) {
          for (String fldName : defaults.keySet()) {
            if (!si.hasField(fldName) && !sys.isExtField(tblName, fldName)) {
              Pair<DefaultExpression, Object> pair = defaults.get(fldName);
              si.addConstant(fldName, srvDef.getValue(tblName, fldName, pair.getA(), pair.getB()));
            }
          }
        }
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
      } else if (!BeeUtils.isEmpty(baseUpdate)) { // UPDATE
        IsCondition wh = SqlUtils.equals(tblName, idName, id);

        SqlUpdate su = new SqlUpdate(tblName).addConstant(verName, version);

        for (FieldInfo col : baseUpdate) {
          su.addConstant(col.fieldName, col.newValue);
        }
        ResponseObject resp = qs.updateDataWithResponse(
            su.setWhere(SqlUtils.and(wh, SqlUtils.equals(tblName, verName, tblInfo.version))));
        int res = resp.getResponse(-1, logger);

        if (res == 0 && refreshUpdates(updates, view)) { // Optimistic lock exception
          resp = qs.updateDataWithResponse(su.setWhere(wh));
          res = resp.getResponse(-1, logger);
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
      if (c > 0) {
        tblInfo.version = version;
      }
    }
    if (!response.hasErrors() && !BeeUtils.isEmpty(extUpdate)) {
      ResponseObject resp = commitExtensions(tblName, id, extUpdate, updates);
      int res = resp.getResponse(-1, null);

      if (res < 0) {
        response.addError("Error commiting extended fields");

        for (String err : resp.getErrors()) {
          response.addError(err);
        }
      } else {
        c += res;
      }
    }
    if (!response.hasErrors() && !BeeUtils.isEmpty(translationUpdate)) {
      ResponseObject resp = commitTranslations(tblName, id, translationUpdate, updates);
      int res = resp.getResponse(-1, null);

      if (res < 0) {
        response.addError("Error commiting translation fields");

        for (String err : resp.getErrors()) {
          response.addError(err);
        }
      } else {
        c += res;
      }
    }
    if (!response.hasErrors()) {
      response.addInfo("Commit count:", BeeUtils.bracket(tblName), c);
    }
    return id;
  }

  private ResponseObject commitTranslations(String tblName, long id, List<FieldInfo> updates,
      Map<String, TableInfo> aliases) {
    int c = 0;
    Map<String, IsQuery> queryMap = Maps.newHashMap();
    BeeTable table = sys.getTable(tblName);

    for (FieldInfo fldInfo : updates) {
      BeeField field = table.getField(fldInfo.fieldName);
      Object value = fldInfo.newValue;
      String locale = fldInfo.locale;
      String translationKey = table.getTranslationTable(field) + BeeUtils.parenthesize(locale);

      IsQuery query = queryMap.get(translationKey);

      if (aliases != null && Objects.equal(aliases.get(fldInfo.tableAlias).id, id)) {
        query = table.updateTranslationField((SqlUpdate) query, id, field, locale, value);
      } else {
        query = table.insertTranslationField((SqlInsert) query, id, field, locale, value);
      }
      queryMap.put(translationKey, query);
    }
    for (IsQuery query : queryMap.values()) {
      ResponseObject resp = qs.updateDataWithResponse(query);
      int res = resp.getResponse(-1, logger);

      if (res < 0) {
        return resp;
      }
      c += res;
    }
    return ResponseObject.response(c);
  }

  private boolean refreshUpdates(Map<String, TableInfo> updates, BeeView view) {
    long id = 0;
    SqlSelect ss = view.getQuery().resetFields();

    for (TableInfo tblInfo : updates.values()) {
      if (id == 0 && BeeUtils.isEmpty(tblInfo.relation)) {
        id = tblInfo.id;
      }
      String idName = sys.getIdName(tblInfo.tableName);
      String verName = sys.getVersionName(tblInfo.tableName);

      ss.addField(tblInfo.tableAlias, idName, tblInfo.tableAlias + "_" + idName)
          .addField(tblInfo.tableAlias, verName, tblInfo.tableAlias + "_" + verName);

      if (!BeeUtils.isEmpty(ss.getGroupBy())) {
        ss.addGroup(tblInfo.tableAlias, idName, verName);
      }
      for (FieldInfo fldInfo : tblInfo.fields) {
        if (!BeeUtils.isEmpty(fldInfo.fieldAlias)) {
          ss.addField(fldInfo.tableAlias, fldInfo.fieldName, fldInfo.fieldAlias);
        }
      }
    }
    Assert.state(DataUtils.isId(id));
    SimpleRow res = qs.getRow(ss.setWhere(view.getCondition(ComparisonFilter.compareId(id), null)));

    if (res == null) {
      logger.warning("refreshUpdates:", ss.getQuery(), "getRow is null");
      return false;
    }
    for (TableInfo tblInfo : updates.values()) {
      String idColumn = tblInfo.tableAlias + "_" + sys.getIdName(tblInfo.tableName);
      String verColumn = tblInfo.tableAlias + "_" + sys.getVersionName(tblInfo.tableName);

      tblInfo.id = res.getLong(idColumn);
      tblInfo.version = res.getLong(verColumn);

      for (FieldInfo fldInfo : tblInfo.fields) {
        if (!BeeUtils.isEmpty(fldInfo.fieldAlias)) {
          String value = res.getValue(fldInfo.fieldAlias);

          if (!BeeUtils.isEmpty(value)
              && SqlDataType.DATE.equals(view.getColumnType(fldInfo.fieldAlias))) {
            JustDate date = res.getDate(fldInfo.fieldAlias);
            value = (date == null) ? null : date.serialize();
          }

          if (!Objects.equal(value, fldInfo.oldValue)) {
            logger.warning("refreshUpdates:", tblInfo.tableName, tblInfo.id, fldInfo.fieldName,
                "old:", fldInfo.oldValue, "value:", value);
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean registerField(String colName, FieldInfo fldInfo,
      Map<String, TableInfo> updates, BeeView view, ResponseObject response) {

    boolean ok = true;
    String tblAlias = view.getColumnOwner(colName);
    String relation = null;
    String srcName = view.getColumnParent(colName);

    if (BeeUtils.isEmpty(tblAlias)) {
      tblAlias = view.getColumnSource(colName);
    } else {
      String als = view.getColumnSource(colName);

      if (!updates.containsKey(als)) {
        updates.put(als, new TableInfo(als, view.getColumnTable(colName), tblAlias));
      }
    }
    if (srcName != null) {
      String als = view.getColumnSource(srcName);
      String fld = view.getColumnField(srcName);
      ok = registerField(srcName, new FieldInfo(als, null, fld, null, null, null),
          updates, view, response);
      relation = BeeUtils.join(".", BeeUtils.notEmpty(view.getColumnOwner(srcName), als), fld);
    }
    if (ok) {
      if (!updates.containsKey(tblAlias)) {
        updates.put(tblAlias, new TableInfo(tblAlias, view.getColumnTable(colName), relation));
      }
      boolean found = false;

      for (FieldInfo info : updates.get(tblAlias).fields) {
        if (BeeUtils.same(info.fieldName, fldInfo.fieldName)
            && BeeUtils.same(info.locale, fldInfo.locale)) {

          if (BeeUtils.same(info.fieldAlias, fldInfo.fieldAlias)) {
            found = true;
          } else {
            response.addError("Attempt to update field more than once:",
                BeeUtils.bracket(fldInfo.fieldName));
            ok = false;
            break;
          }
        }
      }
      if (ok && !found) {
        updates.get(tblAlias).fields.add(fldInfo);
      }
    }
    return ok;
  }
}
