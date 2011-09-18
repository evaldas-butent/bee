package com.butent.bee.server.data;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;

import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeView.ViewField;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsQuery;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

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
    private final String keyAlias;
    private final List<FieldInfo> fields = Lists.newArrayList();
    private Long id;
    private Long version;

    private TableInfo(String tableAlias, String tableName, String keyAlias) {
      this.tableAlias = tableAlias;
      this.tableName = tableName;
      this.keyAlias = keyAlias;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(".\ntableAlias: " + tableAlias)
          .append("\ntableName: " + tableName)
          .append("\nkeyAlias: " + keyAlias)
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

  private static Logger logger = Logger.getLogger(DataEditorBean.class.getName());

  @EJB
  SystemBean sys;
  @EJB
  QueryServiceBean qs;
  @EJB
  UserServiceBean usr;
  @Resource
  EJBContext ctx;

  public ResponseObject commitRow(BeeRowSet rs, boolean returnAllFields) {
    Assert.notNull(rs);

    ResponseObject response = new ResponseObject();
    BeeView view = sys.getView(rs.getViewName());
    BeeRow row = rs.getRow(0);
    Map<String, TableInfo> updates = Maps.newHashMap();

    if (!BeeUtils.isPositive(rs.getNumberOfColumns())) {
      response.addError("Nothing to commit");

    } else if (rs.getNumberOfRows() != 1) {
      response.addError("Can commit only one row at a time");

    } else if (view.isReadOnly()) {
      response.addError("View", view.getName(), "is read only.");

    } else {
      for (int i = 0; i < rs.getNumberOfColumns(); i++) {
        ValueType colType = rs.getColumnType(i);
        String colName = rs.getColumnId(i);

        if (!view.hasColumn(colName)) {
          response.addError("Unknown column:", BeeUtils.bracket(colName));
          break;
        } else {
          String oldValue = (row.getShadow() == null) ? null : row.getShadow().get(i);
          Object newValue = Value.parseValue(colType, row.getString(i), false).getObjectValue();
          String locale = view.getLocale(colName);
          ViewField colField = view.getViewField(view.getExpression(colName));

          if (!registerField(colField,
                new FieldInfo(colField.getAlias(), colName, colField.getField(),
                    oldValue, newValue, locale), updates, view, response)) {
            break;
          }
        }
      }
    }
    if (!response.hasErrors()) {
      TableInfo tblInfo = null;

      for (TableInfo info : updates.values()) {
        if (BeeUtils.isEmpty(info.keyAlias)) {
          tblInfo = info;
          break;
        }
      }
      Assert.notNull(tblInfo);
      Long id = row.getId();

      if (!BeeUtils.isEmpty(id)) {
        tblInfo.id = id;
        tblInfo.version = row.getVersion();

        if (updates.size() > 1 && !refreshUpdates(updates, view)) {
          response.addError("Optimistic lock exception");
        }
      }
      if (!response.hasErrors()) {
        id = commitTable(tblInfo, updates, view, response);
      }
      if (!response.hasErrors()) {
        if (returnAllFields) {
          BeeRowSet newRs = sys.getViewData(view.getName(),
              SqlUtils.equal(view.getSource(), view.getSourceIdName(), id), null, 0, 0);

          if (newRs.isEmpty()) {
            response.addError("Optimistic lock exception");
          } else {
            response.setResponse(newRs.getRow(0));
          }
        } else {
          response.setResponse(new BeeRow(id, tblInfo.version));
        }
      }
    }
    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    } else if (usr.isUserTable(view.getSource())) {
      usr.invalidateCache();
    }
    return response;
  }

  public ResponseObject deleteRow(String viewName, RowInfo row) {
    Assert.notEmpty(viewName);
    Assert.notNull(row);
    BeeView view = sys.getView(viewName);
    ResponseObject response = null;

    if (view.isReadOnly()) {
      return ResponseObject.error("View", view.getName(), "is read only.");
    }
    String tblName = view.getSource();
    Multimap<String, String[]> deletes = HashMultimap.create();
    SqlSelect ss = new SqlSelect();

    String als = registerDelete(ss, null, null, tblName, deletes);

    if (!BeeUtils.isEmpty(als)) {
      Map<String, String> res = qs.getRow(
          ss.setWhere(SqlUtils.equal(als, view.getSourceIdName(), row.getId())));

      if (res == null) {
        response = ResponseObject.error("Optimistic lock exception");
      } else {
        Multimap<String, Long> ids = HashMultimap.create();

        for (String[] pair : deletes.values()) {
          Long id = BeeUtils.toLongOrNull(res.get(pair[1]));

          if (!BeeUtils.isEmpty(id)) {
            ids.put(pair[0], id);
          }
        }
        if (ids.size() > 0) {
          for (String tbl : ids.keySet()) {
            response = qs.updateDataWithResponse(new SqlDelete(tbl)
                .setWhere(SqlUtils.inList(tbl, sys.getIdName(tbl), ids.get(tbl).toArray())));

            if (response.hasErrors()) {
              break;
            }
          }
        }
      }
    }
    if (response == null || !response.hasErrors()) {
      response = qs.updateDataWithResponse(new SqlDelete(tblName)
          .setWhere(SqlUtils.and(
              SqlUtils.equal(tblName, view.getSourceIdName(), row.getId()),
              SqlUtils.equal(tblName, view.getSourceVersionName(), row.getVersion()))));
    }
    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    } else if (usr.isUserTable(tblName)) {
      usr.invalidateCache();
    }
    return response;
  }

  public ResponseObject generateData(String tblName, int rowCount) {
    Assert.isTrue(sys.isTable(tblName), "Not a base table: " + tblName);
    Assert.isPositive(rowCount, "rowCount must be positive");

    Collection<BeeField> fields = sys.getTableFields(tblName);
    SqlInsert si = new SqlInsert(tblName);
    List<FieldInfo> extUpdate = Lists.newArrayList();
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
                .addFields(relation, sys.getIdName(relation))
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
            extUpdate.add(new FieldInfo(null, null, field.getName(), null, v, null));
          }
        } else {
          si.addConstant(field.getName(), v);
        }
      }
      long id = qs.insertData(si);

      if (id < 0) {
        return ResponseObject.error(tblName, si.getQuery(), "Error inserting data");
      } else {
        ResponseObject resp = commitExtensions(tblName, id, extUpdate, null);
        if (resp.hasErrors()) {
          return resp;
        }
      }
      si.reset();
      extUpdate.clear();
    }
    return ResponseObject.info(tblName, "generated", rowCount, "rows");
  }

  public void setState(String tblName, long id, String stateName, long... bits) {
    BeeTable table = sys.getTable(tblName);
    BeeState state = sys.getState(stateName);

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
        sys.rebuildTable(table.getName());
      }
      SqlUpdate su = table.updateState(id, state, bitMap);

      if (!BeeUtils.isEmpty(su) && qs.updateData(su) == 0) {
        qs.updateData(table.insertState(id, state, bitMap));
      }
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

      if (aliases != null && BeeUtils.equals(aliases.get(fldInfo.tableAlias).id, id)) {
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
          if (BeeUtils.same(tblInfo.tableAlias, info.keyAlias)) {
            relInfo = info;
            break;
          }
        }
        Long id = commitTable(relInfo, updates, view, response);

        if (response.hasErrors()) {
          break;
        } else if (BeeUtils.isEmpty(relInfo.id)) {
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

      if (BeeUtils.isEmpty(id)) { // INSERT
        SqlInsert si = new SqlInsert(tblName).addConstant(verName, version);

        for (FieldInfo col : baseUpdate) {
          si.addConstant(col.fieldName, col.newValue);
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
        IsCondition wh = SqlUtils.equal(tblName, idName, id);

        SqlUpdate su = new SqlUpdate(tblName).addConstant(verName, version);

        for (FieldInfo col : baseUpdate) {
          su.addConstant(col.fieldName, col.newValue);
        }
        ResponseObject resp = qs.updateDataWithResponse(
            su.setWhere(SqlUtils.and(wh, SqlUtils.equal(tblName, verName, tblInfo.version))));
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

      if (aliases != null && BeeUtils.equals(aliases.get(fldInfo.tableAlias).id, id)) {
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
      if (BeeUtils.allEmpty(id, tblInfo.keyAlias)) {
        id = tblInfo.id;
      }
      String idName = sys.getIdName(tblInfo.tableName);
      String verName = sys.getVersionName(tblInfo.tableName);

      ss.addField(tblInfo.tableAlias, idName, tblInfo.tableAlias + "_" + idName)
          .addField(tblInfo.tableAlias, verName, tblInfo.tableAlias + "_" + verName);

      for (FieldInfo fldInfo : tblInfo.fields) {
        if (!BeeUtils.isEmpty(fldInfo.fieldAlias)) {
          ss.addField(fldInfo.tableAlias, fldInfo.fieldName, fldInfo.fieldAlias);
        }
      }
    }
    Assert.notEmpty(id);
    BeeRowSet res = qs.getViewData(
        ss.setWhere(SqlUtils.equal(view.getSource(), view.getSourceIdName(), id)), view);

    if (res.isEmpty()) {
      return false;
    }
    for (TableInfo tblInfo : updates.values()) {
      String idColumn = tblInfo.tableAlias + "_" + sys.getIdName(tblInfo.tableName);
      String verColumn = tblInfo.tableAlias + "_" + sys.getVersionName(tblInfo.tableName);

      tblInfo.id = res.getLong(0, res.getColumnIndex(idColumn));
      tblInfo.version = res.getLong(0, res.getColumnIndex(verColumn));

      for (FieldInfo fldInfo : tblInfo.fields) {
        if (!BeeUtils.isEmpty(fldInfo.fieldAlias)) {
          String value = res.getString(0, res.getColumnIndex(fldInfo.fieldAlias));

          if (!BeeUtils.equals(value, fldInfo.oldValue)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private String registerDelete(SqlSelect ss, String srcAlias, String srcField, String tblName,
      Multimap<String, String[]> deletes) {

    Assert.state(!deletes.containsKey(tblName), "Closed cycle recursion: " + tblName);
    BeeTable table = sys.getTable(tblName);
    String tblAlias = null;

    for (BeeField field : table.getFields()) {
      if (field.isUnique()) {
        String relTable = field.getRelation();

        if (!BeeUtils.isEmpty(relTable)) {
          String fldName = field.getName();
          String fldAlias = SqlUtils.uniqueName();

          if (BeeUtils.isEmpty(tblAlias)) {
            tblAlias = SqlUtils.uniqueName();

            if (deletes.size() > 0) {
              ss.addFromLeft(tblName, tblAlias,
                  SqlUtils.join(srcAlias, srcField, tblAlias, sys.getIdName(tblName)));
            } else {
              ss.addFrom(tblName, tblAlias);
            }
            if (field.isExtended()) {
              tblAlias = table.joinExtField(ss, tblAlias, field);
            }
          }
          ss.addField(tblAlias, fldName, fldAlias);
          deletes.put(tblName, new String[] {relTable, fldAlias});
          registerDelete(ss, tblAlias, fldName, relTable, deletes);
        }
      }
    }
    return tblAlias;
  }

  private boolean registerField(ViewField colField, FieldInfo fldInfo,
      Map<String, TableInfo> updates, BeeView view, ResponseObject response) {

    boolean ok = true;
    String tblAlias = colField.getOwner();
    String keyAlias = null;
    ViewField srcField = view.getViewField(colField.getSourceExpression());

    if (BeeUtils.isEmpty(tblAlias)) {
      tblAlias = colField.getAlias();
    } else {
      String alias = colField.getAlias();

      if (!updates.containsKey(alias)) {
        updates.put(alias, new TableInfo(alias, colField.getTable(), BeeUtils.bracket(tblAlias)));
      }
    }
    if (srcField != null) {
      ok = registerField(srcField,
          new FieldInfo(srcField.getAlias(), null, srcField.getField(), null, null, null),
          updates, view, response);
      keyAlias = BeeUtils.ifString(srcField.getOwner(), srcField.getAlias());
    }
    if (ok) {
      if (!updates.containsKey(tblAlias)) {
        updates.put(tblAlias, new TableInfo(tblAlias, colField.getTable(), keyAlias));
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
