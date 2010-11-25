package com.butent.bee.egg.server.ui;

import com.butent.bee.egg.server.Assert;
import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.data.BeeTable.BeeForeignKey;
import com.butent.bee.egg.server.data.BeeTable.BeeStructure;
import com.butent.bee.egg.server.data.QueryServiceBean;
import com.butent.bee.egg.server.data.SystemBean;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeRowSet;
import com.butent.bee.egg.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.egg.shared.sql.Conditions;
import com.butent.bee.egg.shared.sql.IsCondition;
import com.butent.bee.egg.shared.sql.IsQuery;
import com.butent.bee.egg.shared.sql.SqlDelete;
import com.butent.bee.egg.shared.sql.SqlInsert;
import com.butent.bee.egg.shared.sql.SqlSelect;
import com.butent.bee.egg.shared.sql.SqlUpdate;
import com.butent.bee.egg.shared.sql.SqlUtils;
import com.butent.bee.egg.shared.ui.UiComponent;
import com.butent.bee.egg.shared.utils.BeeUtils;

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

  private void commitChanges(RequestInfo reqInfo, ResponseBuffer buff) {
    BeeRowSet upd = BeeRowSet.restore(reqInfo.getContent());

    String tbl = upd.getSource();
    String idName = upd.getIdName();
    String lockName = upd.getLockName();
    int c = 0;
    String err = "";

    if (BeeUtils.isEmpty(idName)) {
      err = "ID column not found";
    }

    for (BeeRow row : upd.getRows()) {
      if (!BeeUtils.isEmpty(err)) {
        break;
      }
      Map<Integer, String> shadow = row.getShadow();
      if (BeeUtils.isEmpty(shadow)) {
        continue;
      }
      long id = row.getLong(idName);
      String mode = "INSERT";

      if (shadow.containsKey(upd.getIdIndex())) {
        String tmpId = shadow.get(upd.getIdIndex());

        if (!BeeUtils.isEmpty(tmpId)) {
          id = Long.parseLong(tmpId);
          mode = "DELETE";
        }
      } else {
        if (!BeeUtils.isEmpty(row.getValue(idName))) {
          mode = "UPDATE";
        }
      }

      if (mode.equals("INSERT")) {
        if (!BeeUtils.isEmpty(lockName)) {
          row.setValue(lockName, BeeUtils.transform(System.currentTimeMillis()));
        }
        SqlInsert si = new SqlInsert(tbl);

        for (Integer col : shadow.keySet()) {
          si.addConstant(upd.getColumnName(col), row.getOriginal(col));
        }
        id = qs.insertData(si);

        if (id < 0) {
          err = "Error inserting data";
        } else {
          if (id > 0) {
            row.setValue(idName, BeeUtils.transform(id));
          }
          c++;
        }
      } else {
        IsCondition wh = SqlUtils.and(SqlUtils.equal(tbl, idName, id));
        if (!BeeUtils.isEmpty(lockName)) {
          ((Conditions) wh).add(SqlUtils.equal(tbl, lockName, row.getLong(lockName)));
        }
        IsQuery query;

        if (mode.equals("DELETE")) {
          query = new SqlDelete(tbl).setWhere(wh);

        } else { // UPDATE
          query = new SqlUpdate(tbl).setWhere(wh);

          if (!BeeUtils.isEmpty(lockName)) {
            row.setValue(lockName, BeeUtils.transform(System.currentTimeMillis()));
          }
          for (Integer col : shadow.keySet()) {
            ((SqlUpdate) query).addConstant(upd.getColumnName(col), row.getOriginal(col));
          }
        }
        int res = qs.updateData(query);

        switch (res) {
          case -1:
            err = "Error updating data";
            break;
          case 0:
            err = "Optimistic lock exception";
            break;
          default:
            c += res;
            break;
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
    String tbl = getXmlField(reqInfo, buff, "table_name");

    SqlSelect ss = new SqlSelect();
    ss.addAllFields("t").addFrom(tbl, "t");

    if (sys.beeTable(tbl)) {
      for (BeeForeignKey foreign : sys.getTable(tbl).getForeignKeys()) {
        String refTbl = foreign.getRefTable();

        ss.addFromLeft(refTbl,
            SqlUtils.join("t", foreign.getKeyField(), refTbl, foreign.getRefField()));

        for (BeeStructure field : sys.getFields(refTbl)) {
          if (field.isUnique()) {
            ss.addFields(refTbl, field.getName());
          }
        }
      }
    }

    BeeRowSet res = qs.getData(ss);

    buff.add(res.serialize());
  }

  private void getTables(ResponseBuffer buff) {
    buff.addColumn(new BeeColumn("BeeTable"));

    for (String tbl : sys.getTables()) {
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
