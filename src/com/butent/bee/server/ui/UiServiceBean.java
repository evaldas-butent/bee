package com.butent.bee.server.ui;

import com.butent.bee.server.Assert;
import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.BeeRowSet.BeeRow;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
  IdGeneratorBean ig;
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
      if (!BeeUtils.same(SqlBuilderFactory.getEngine(), dsn)) {
        ig.destroy();
        SqlBuilderFactory.setDefaultEngine(dsn);
        sys.init();
      }

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
      } else if (svc.equals("rpc_ui_states")) {
        getStates(reqInfo, buff);
      } else if (svc.equals("rpc_ui_statetable")) {
        getStateTable(reqInfo, buff);
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
    if (!sys.commitChanges(BeeRowSet.restore(reqInfo.getContent()), buff)) {
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
    String fName = getXmlField(reqInfo, "form_name");

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

  private void getStates(RequestInfo reqInfo, ResponseBuffer buff) {
    String table = getXmlField(reqInfo, "table_name");
    Set<String> states = new HashSet<String>();

    for (String tbl : sys.getTableNames()) {
      if (BeeUtils.isEmpty(table) || BeeUtils.same(tbl, table)) {
        for (String state : sys.getTableStates(tbl)) {
          states.add(state);
        }
      }
    }
    buff.addColumn(new BeeColumn("BeeStates"));
    for (String state : states) {
      buff.add(state);
    }
  }

  private void getStateTable(RequestInfo reqInfo, ResponseBuffer buff) {
    String table = getXmlField(reqInfo, "table_name");
    String states = getXmlField(reqInfo, "table_states");
    BeeRowSet res = sys.editStateRoles(table, states);
    buff.add(res.serialize());
  }

  private void getTable(RequestInfo reqInfo, ResponseBuffer buff) {
    String table = getXmlField(reqInfo, "table_name");
    int limit = BeeUtils.toInt(getXmlField(reqInfo, "table_limit"));
    int offset = BeeUtils.toInt(getXmlField(reqInfo, "table_offset"));
    String states = getXmlField(reqInfo, "table_states");
    BeeRowSet res = sys.getViewData(table, limit, offset, states);
    buff.add(res.serialize());
  }

  private void getTables(ResponseBuffer buff) {
    buff.addColumn(new BeeColumn("BeeTable"));

    for (String tbl : sys.getTableNames()) {
      buff.add(tbl);
    }
  }

  private String getXmlField(RequestInfo reqInfo, String fieldName) {
    String xml = reqInfo.getContent();
    if (BeeUtils.isEmpty(xml)) {
      logger.warning("Request data not found");
      return null;
    }

    Map<String, String> fields = XmlUtils.getElements(xml);
    if (BeeUtils.isEmpty(fields)) {
      logger.warning("No elements with text found");
      return null;
    }
    return fields.get(fieldName);
  }

  private void gridInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String gName = getXmlField(reqInfo, "grid_name");
    String grd = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g").setWhere(
        SqlUtils.equal("g", "table", gName));

    String x = qs.getSingleRow(ss).getString("properties");

    if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
      grd = x.replaceFirst("^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
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
    String mName = getXmlField(reqInfo, "menu_name");
    String lRoot = getXmlField(reqInfo, "root_layout");
    String lItem = getXmlField(reqInfo, "item_layout");

    UiComponent menu = holder.getMenu(mName, lRoot, lItem,
        "/com/butent/bee/server/menu.xml");

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
    if (BeeUtils.same(cmd, "all")) {
      sys.rebuildTables(buff);

    } else if (BeeUtils.same(cmd, "ext")) {
      sys.initExtensions();
      buff.add("Extensions OK");

    } else if (BeeUtils.same(cmd, "views")) {
      sys.initViews();
      buff.add("Views OK");

    } else if (BeeUtils.startsSame(cmd, "roles")) {
      String[] xArr = cmd.split(" ", 5);
      String tbl = xArr[1];
      long id = BeeUtils.toLong(xArr[2]);
      String state = xArr[3];
      int[] roles = null;

      if (xArr.length > 4) {
        String[] rArr = xArr[4].split(" ");
        roles = new int[rArr.length];

        for (int i = 0; i < rArr.length; i++) {
          roles[i] = BeeUtils.toInt(rArr[i]);
        }
      }
      sys.setState(tbl, id, state, true, roles);
      buff.add("Toggle OK");

    } else {
      if (sys.isTable(cmd)) {
        sys.rebuildTable(cmd);
        buff.add("Rebuild " + cmd + " OK");
      } else {
        buff.add("ERROR: unknown table " + cmd);
      }
    }
  }
}
