package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.data.QueryServiceBean;
import com.butent.bee.egg.server.data.SystemBean;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.SqlDelete;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class UiServiceBean {

  private static Logger logger = Logger.getLogger(UiServiceBean.class.getName());

  @EJB
  UiHolderBean holder;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @Resource
  EJBContext ctx;

  public void doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    String dsn = reqInfo.getDsn();
    if (BeeUtils.isEmpty(dsn)) {
      String msg = "DSN not specified";
      logger.severe(msg);
      buff.add(msg);
    } else {
      qs.switchEngine(dsn);

      if (svc.equals("rpc_ui_form")) {
        formInfo(reqInfo, buff);
      } else if (svc.equals("rpc_ui_form_list")) {
        formList(buff);
      } else if (svc.equals("rpc_ui_menu")) {
        menuInfo(reqInfo, buff);
      } else if (svc.equals("rpc_ui_grid")) {
        gridInfo(reqInfo, buff);
      } else if (svc.equals("rpc_ui_rebuild")) {
        rebuildData(reqInfo, buff);
      } else if (svc.equals("rpc_ui_sql")) {
        doSql(reqInfo, buff);
      } else if (svc.equals("rpc_ui_tables")) {
        getTables(buff);
      } else if (svc.equals("rpc_ui_table")) {
        getTable(reqInfo, buff);
      } else if (svc.equals("rpc_ui_commit")) {
        commitChanges(reqInfo, buff);
      } else {
        String msg = BeeUtils.concat(1, svc, "loader service not recognized");
        logger.warning(msg);
        buff.add(msg);
      }
    }
  }

  private void addRelation(SqlSelect ss, String tbl, BeeStructure fld) {
    String relation = fld.getRelation();

    if (!BeeUtils.isEmpty(relation)) {
      String als = BeeUtils.randomString(3, 3, 'a', 'z');
      BeeTable relTbl = sys.getTable(relation);
      boolean addFrom = false;

      for (BeeStructure relFld : relTbl.getFields()) {
        if (relFld.isUnique()) {
          addFrom = true;
          ss.addField(als, relFld.getName(), fld.getName() + relFld.getName());
        }
      }
      if (addFrom) {
        ss.addFromLeft(relation, als, SqlUtils.join(tbl, fld.getName(), als, relTbl.getIdName()));
      }
    }
  }

  private void commitChanges(RequestInfo reqInfo, ResponseBuffer buff) {
    BeeRowSet upd = BeeRowSet.restore(reqInfo.getContent());
    int c = 0;
    String err = "";

    BeeTable tbl = null;
    BeeTable extTbl = null;
    int idIndex = -1;
    int lockIndex = -1;
    int extLockIndex = -1;
    String src = upd.getSource();

    if (sys.isBeeTable(src)) {
      tbl = sys.getTable(src);
      extTbl = tbl.getExtTable();

      for (int i = 0; i < upd.getColumns().length; i++) {
        BeeColumn column = upd.getColumns()[i];

        if (BeeUtils.same(column.getFieldSource(), tbl.getName())) {
          String name = column.getFieldName();

          if (BeeUtils.same(name, tbl.getIdName())) {
            idIndex = i;
            continue;
          }
          if (BeeUtils.same(name, tbl.getLockName())) {
            lockIndex = i;
            continue;
          }
          if (!BeeUtils.isEmpty(extTbl) && BeeUtils.same(name, extTbl.getLockName())) {
            extLockIndex = i;
            continue;
          }
        }
      }
      if (idIndex < 0) {
        err = "Cannot update table " + tbl.getName() + " (Unknown ID index).";
      }
    } else {
      err = "Cannot update table (Not a base table " + src + ").";
    }
    for (BeeRow row : upd.getRows()) {
      if (!BeeUtils.isEmpty(err)) {
        break;
      }
      List<Object[]> baseList = new ArrayList<Object[]>();
      List<Object[]> extList = new ArrayList<Object[]>();

      if (!BeeUtils.isEmpty(row.getShadow())) {
        for (Integer col : row.getShadow().keySet()) {
          BeeColumn column = upd.getColumn(col);
          String fld = column.getFieldName();

          if (BeeUtils.isEmpty(fld)) {
            err = "Cannot update column " + upd.getColumnName(col) + " (Unknown source).";
            break;
          }
          if (!BeeUtils.same(column.getFieldSource(), src)) {
            err = "Cannot update column (Wrong source " + column.getFieldSource() + ").";
            break;
          }
          Object[] entry = new Object[]{fld, row.getOriginal(col)};

          if (tbl.isField(fld)) {
            baseList.add(entry);
          } else if (!BeeUtils.isEmpty(extTbl) && extTbl.isField(fld)) {
            extList.add(entry);
          } else {
            err = "Cannot update column " + upd.getColumnName(col) + " (Unknown field: " + fld
                + ").";
            break;
          }
        }
        if (!BeeUtils.isEmpty(err)) {
          break;
        }
      }
      long id = row.getLong(idIndex);

      if (row.markedForDelete()) { // DELETE
        IsCondition wh = SqlUtils.equal(tbl.getName(), tbl.getIdName(), id);

        if (lockIndex >= 0) {
          wh = SqlUtils.and(wh,
              SqlUtils.equal(tbl.getName(), tbl.getLockName(), row.getLong(lockIndex)));
        }
        SqlDelete sd = new SqlDelete(tbl.getName()).setWhere(wh);
        int res = qs.updateData(sd);

        if (res < 0) {
          err = "Error deleting data";
          break;
        } else if (res == 0) {
          err = "Optimistic lock exception";
          break;
        }
        c += res;

      } else if (row.markedForInsert()) { // INSERT
        SqlInsert si = new SqlInsert(tbl.getName());

        for (Object[] entry : baseList) {
          si.addConstant((String) entry[0], entry[1]);
        }
        if (lockIndex >= 0) {
          long lock = System.currentTimeMillis();
          si.addConstant(tbl.getLockName(), lock);
          row.setValue(lockIndex, BeeUtils.transform(lock));
        }
        id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
          break;
        } else {
          c++;
          row.setValue(idIndex, BeeUtils.transform(id));

          if (!BeeUtils.isEmpty(extList)) {
            si = new SqlInsert(extTbl.getName());

            for (Object[] entry : extList) {
              si.addConstant((String) entry[0], entry[1]);
            }
            if (extLockIndex >= 0) {
              long lock = System.currentTimeMillis();
              si.addConstant(extTbl.getLockName(), lock);
              row.setValue(extLockIndex, BeeUtils.transform(lock));
            }
            si.addConstant(extTbl.getIdName(), id);

            if (qs.insertData(si) < 0) {
              err = "Error inserting data";
              break;
            }
            c++;
          }
        }

      } else { // UPDATE
        if (!BeeUtils.isEmpty(baseList)) {
          SqlUpdate su = new SqlUpdate(tbl.getName());

          for (Object[] entry : baseList) {
            su.addConstant((String) entry[0], entry[1]);
          }
          IsCondition wh = SqlUtils.equal(tbl.getName(), tbl.getIdName(), id);

          if (lockIndex >= 0) {
            wh = SqlUtils.and(wh,
                SqlUtils.equal(tbl.getName(), tbl.getLockName(), row.getLong(lockIndex)));

            long lock = System.currentTimeMillis();
            su.addConstant(tbl.getLockName(), lock);
            row.setValue(lockIndex, BeeUtils.transform(lock));
          }
          int res = qs.updateData(su.setWhere(wh));

          if (res < 0) {
            err = "Error updating data";
            break;
          } else if (res == 0) {
            err = "Optimistic lock exception";
            break;
          }
          c += res;
        }
        if (!BeeUtils.isEmpty(extList)) {
          if (extLockIndex < 0 || !BeeUtils.isEmpty(row.getLong(extLockIndex))) {
            SqlUpdate su = new SqlUpdate(extTbl.getName());

            for (Object[] entry : extList) {
              su.addConstant((String) entry[0], entry[1]);
            }
            IsCondition wh = SqlUtils.equal(extTbl.getName(), extTbl.getIdName(), id);

            if (extLockIndex >= 0) {
              wh = SqlUtils.and(wh,
                  SqlUtils.equal(extTbl.getName(), extTbl.getLockName(), row.getLong(extLockIndex)));

              long lock = System.currentTimeMillis();
              su.addConstant(extTbl.getLockName(), lock);
              row.setValue(extLockIndex, BeeUtils.transform(lock));
            }
            int res = qs.updateData(su.setWhere(wh));

            if (res < 0) {
              err = "Error updating data";
              break;
            } else if (res == 0 && extLockIndex >= 0) {
              err = "Optimistic lock exception";
              break;
            } else if (res > 0) {
              c += res;
              continue;
            }
          }
          SqlInsert si = new SqlInsert(extTbl.getName());

          for (Object[] entry : extList) {
            si.addConstant((String) entry[0], entry[1]);
          }
          if (extLockIndex >= 0) {
            long lock = System.currentTimeMillis();
            si.addConstant(extTbl.getLockName(), lock);
            row.setValue(extLockIndex, BeeUtils.transform(lock));
          }
          si.addConstant(extTbl.getIdName(), id);

          if (qs.insertData(si) < 0) {
            err = "Error inserting data";
            break;
          }
          c++;
        }
      }
    }
    if (BeeUtils.isEmpty(err)) {
      buff.add(c);
      buff.add(upd.serialize());
    } else {
      buff.add(-1);
      buff.add(err);
      ctx.setRollbackOnly();
    }
  }

  private void doSql(RequestInfo reqInfo, ResponseBuffer buff) {
    String sql = reqInfo.getContent();
    String[] arr = sql.split(" ", 2);
    if (arr.length > 1) {
      sql = arr[1];
    } else {
      sql = null;
    }
    if (BeeUtils.isEmpty(sql)) {
      buff.add("SQL command not found");
      return;
    }
    Object res = qs.processSql(sql);

    if (res instanceof BeeRowSet) {
      buff.addColumns(((BeeRowSet) res).getColumns());

      for (BeeRow row : ((BeeRowSet) res).getRows()) {
        for (int col = 0; col < ((BeeRowSet) res).getColumnCount(); col++) {
          buff.add(row.getValue(col));
        }
      }
    } else {
      buff.add("Affected: " + res);
    }
  }

  private void formInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String fName = getXmlField(reqInfo, buff, "form_name");

    UiComponent form = holder.getForm(fName);

    if (BeeUtils.isEmpty(form)) {
      String msg = "Form name not recognized: " + fName;
      logger.warning(msg);
      buff.add(msg);
    } else {
      buff.add(form.serialize());
    }
  }

  private void formList(ResponseBuffer buff) {
    SqlSelect ss = new SqlSelect();
    ss.addFields("f", "form").addFrom("forms", "f").addOrder("f", "form");

    BeeRowSet res = qs.getData(ss);

    buff.addColumns(res.getColumns());

    for (BeeRow row : res.getRows()) {
      for (int col = 0; col < res.getColumnCount(); col++) {
        buff.add(row.getValue(col));
      }
    }
  }

  private void getTable(RequestInfo reqInfo, ResponseBuffer buff) {
    String table = getXmlField(reqInfo, buff, "table_name");
    String als = BeeUtils.randomString(3, 3, 'a', 'z');

    SqlSelect ss = new SqlSelect();
    ss.addFrom(table, als);

    if (sys.isBeeTable(table)) {
      BeeTable tbl = sys.getTable(table);

      for (BeeStructure fld : tbl.getFields()) {
        ss.addFields(als, fld.getName());
        addRelation(ss, als, fld);
      }
      ss.addFields(als, tbl.getLockName(), tbl.getIdName());

      BeeTable extTbl = tbl.getExtTable();

      if (!BeeUtils.isEmpty(extTbl)) {
        String extName = extTbl.getName();
        ss.addFromLeft(extName, SqlUtils.join(als, tbl.getIdName(), extName, extTbl.getIdName()));

        for (BeeStructure extFld : extTbl.getFields()) {
          ss.addFields(extName, extFld.getName());
          addRelation(ss, extName, extFld);
        }
        ss.addFields(extName, extTbl.getLockName());
      }
    } else {
      ss.addAllFields(als);
    }

    BeeRowSet res = qs.getData(ss);

    buff.add(res.serialize());
  }

  private void getTables(ResponseBuffer buff) {
    buff.addColumn(new BeeColumn("BeeTable"));

    for (String tbl : sys.getTableNames()) {
      buff.add(tbl);
    }
  }

  private String getXmlField(RequestInfo reqInfo, ResponseBuffer buff,
      String fieldName) {
    String xml = reqInfo.getContent();
    if (BeeUtils.isEmpty(xml)) {
      buff.add("Request data not found");
      return null;
    }

    Map<String, String> fields = XmlUtils.getElements(xml);
    if (BeeUtils.isEmpty(fields)) {
      buff.addLine("No elements with text found in", xml);
      return null;
    }
    return fields.get(fieldName);
  }

  private void gridInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String gName = getXmlField(reqInfo, buff, "grid_name");
    String grd = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g").setWhere(
        SqlUtils.equal("g", "table", gName));

    String x = qs.getSingleRow(ss).getString("properties");

    if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
      grd = x.replaceFirst(
            "^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
    }

    ss = new SqlSelect();
    ss.addFields("c", "caption").addFrom("columns", "c").setWhere(
          SqlUtils.equal("c", "table", grd)).addOrder("c", "order");

    BeeRowSet data = qs.getData(ss);

    if (!data.isEmpty()) {
      for (BeeRow row : data.getRows()) {
        buff.addColumn(
            new BeeColumn(row.getString("caption").replaceAll("['\"]", "")));
      }
      for (int i = 0; i < 20; i++) {
        for (int j = 0; j < data.getRowCount(); j++) {
          buff.add(j == 0 ? i + 1 : BeeConst.STRING_EMPTY);
        }
      }
      return;
    }
    String msg = "Grid name not recognized: " + grd;
    logger.warning(msg);
    buff.add(msg);
  }

  private void menuInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String mName = getXmlField(reqInfo, buff, "menu_name");
    String lRoot = getXmlField(reqInfo, buff, "root_layout");
    String lItem = getXmlField(reqInfo, buff, "item_layout");

    UiComponent menu = holder.getMenu(mName, lRoot, lItem,
        "/com/butent/bee/egg/server/menu.xml");

    if (BeeUtils.isEmpty(menu)) {
      String msg = "Error initializing menu: " + mName;
      logger.warning(msg);
      buff.add(msg);
    } else {
      buff.add(menu.serialize());
    }
  }

  private void rebuildData(RequestInfo reqInfo, ResponseBuffer buff) {
    String cmd = reqInfo.getContent();
    String[] arr = cmd.split(" ", 2);
    if (arr.length > 1) {
      cmd = arr[1];
    }
    if (BeeUtils.same(cmd, "tables")) {
      sys.rebuildTables(buff);
    } else if (BeeUtils.same(cmd, "keys")) {
      sys.rebuildKeys(buff);
    } else if (BeeUtils.same(cmd, "data")) {
      sys.createData(buff);
    } else {
      sys.createAll(buff);
    }
  }
}
