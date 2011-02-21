package com.butent.bee.server.ui;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
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

  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;

    String svc = reqInfo.getService();
    String dsn = reqInfo.getDsn();

    if (BeeUtils.isEmpty(dsn)) {
      String msg = "DSN not specified";
      logger.severe(msg);
      response = ResponseObject.error(msg);
    } else {
      if (!BeeUtils.same(SqlBuilderFactory.getEngine(), BeeConst.getDsType(dsn))) {
        ig.destroy();
        sys.initDatabase(dsn);
      }

      if (svc.equals("rpc_ui_form")) {
        response = formInfo(reqInfo);
      } else if (svc.equals("rpc_ui_form_list")) {
        response = formList();
      } else if (svc.equals("rpc_ui_menu")) {
        response = menuInfo(reqInfo);
      } else if (svc.equals("rpc_ui_grid")) {
        response = gridInfo(reqInfo);
      } else if (svc.equals("rpc_ui_rebuild")) {
        response = rebuildData(reqInfo);
      } else if (svc.equals("rpc_ui_sql")) {
        response = doSql(reqInfo);
      } else if (svc.equals("rpc_ui_tables")) {
        response = getTables();
      } else if (svc.equals("rpc_ui_table")) {
        response = getTable(reqInfo);
      } else if (svc.equals("rpc_ui_states")) {
        response = getStates(reqInfo);
      } else if (svc.equals("rpc_ui_statetable")) {
        response = getStateTable(reqInfo);
      } else if (svc.equals("rpc_ui_commit")) {
        response = commitChanges(reqInfo);
      } else {
        String msg = BeeUtils.concat(1, svc, "loader service not recognized");
        logger.warning(msg);
        response = ResponseObject.error(msg);
      }
    }
    return response;
  }

  private ResponseObject commitChanges(RequestInfo reqInfo) {
    ResponseObject response = sys.commitChanges(BeeRowSet.restore(reqInfo.getContent()));

    if (response.hasError()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject doSql(RequestInfo reqInfo) {
    String sql = reqInfo.getContent();
    String[] arr = sql.split(" ", 2);
    if (arr.length > 1) {
      sql = arr[1];
    } else {
      sql = null;
    }
    if (BeeUtils.isEmpty(sql)) {
      return ResponseObject.error("SQL command not found");
    }
    Object res = qs.processSql(sql);

    if (res instanceof BeeRowSet) {
      return ResponseObject.response(res);
    } else {
      return ResponseObject.warning("Affected:", res);
    }
  }

  private ResponseObject formInfo(RequestInfo reqInfo) {
    String fName = reqInfo.getParameter("form_name");

    UiComponent form = holder.getForm(fName);

    if (BeeUtils.isEmpty(form)) {
      String msg = "Form name not recognized: " + fName;
      logger.warning(msg);
      return ResponseObject.error(msg);
    } else {
      return ResponseObject.response(form);
    }
  }

  private ResponseObject formList() {
    SqlSelect ss = new SqlSelect();
    ss.addFields("f", "form").addFrom("forms", "f").addOrder("f", "form");
    return ResponseObject.response(qs.getData(ss));
  }

  private ResponseObject getStates(RequestInfo reqInfo) {
    String table = reqInfo.getParameter("table_name");
    Set<String> states = new HashSet<String>();

    for (String tbl : sys.getTableNames()) {
      if (BeeUtils.isEmpty(table) || BeeUtils.same(tbl, table)) {
        for (String state : sys.getTableStates(tbl)) {
          states.add(state);
        }
      }
    }
    BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.TEXT, "BeeStates", "BeeStates"));

    for (String state : states) {
      rs.addRow(state);
    }
    return ResponseObject.response(rs);
  }

  private ResponseObject getStateTable(RequestInfo reqInfo) {
    String table = reqInfo.getParameter("table_name");
    String states = reqInfo.getParameter("table_states");
    return sys.editStateRoles(table, states);
  }

  private ResponseObject getTable(RequestInfo reqInfo) {
    String table = reqInfo.getParameter("table_name");
    int limit = BeeUtils.toInt(reqInfo.getParameter("table_limit"));
    int offset = BeeUtils.toInt(reqInfo.getParameter("table_offset"));
    String states = reqInfo.getParameter("table_states");
    BeeRowSet res = sys.getViewData(table, limit, offset, states);
    return ResponseObject.response(res);
  }

  private ResponseObject getTables() {
    BeeRowSet rs = new BeeRowSet(new BeeColumn(ValueType.TEXT, "BeeTable", "BeeTable"));

    for (String tbl : sys.getTableNames()) {
      rs.addRow(tbl);
    }
    return ResponseObject.response(rs);
  }

  private ResponseObject gridInfo(RequestInfo reqInfo) {
    String gName = reqInfo.getParameter("grid_name");
    String grd = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g").setWhere(
        SqlUtils.equal("g", "table", gName));

    String x = qs.getString(ss, "properties");

    if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
      grd = x.replaceFirst("^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
    }

    ss = new SqlSelect();
    ss.addFields("c", "caption").addFrom("columns", "c").setWhere(
          SqlUtils.equal("c", "table", grd)).addOrder("c", "order");

    BeeRowSet data = qs.getData(ss);

    if (!data.isEmpty()) {
      BeeRowSet rs = new BeeRowSet();

      for (BeeRow row : data.getRows()) {
        String colName = data.getString(row, "caption").replaceAll("['\"]", "");
        rs.addColumn(new BeeColumn(ValueType.TEXT, colName, BeeUtils.createUniqueName("col")));
      }
      for (int i = 0; i < 20; i++) {
        int cnt = data.getNumberOfRows();
        String[] cells = new String[cnt];

        for (int j = 0; j < cnt; j++) {
          cells[j] = (j == 0 ? Integer.toString(i + 1) : BeeConst.STRING_EMPTY);
        }
        rs.addRow(cells);
      }
      return ResponseObject.response(rs);
    }
    String msg = "Grid name not recognized: " + grd;
    logger.warning(msg);
    return ResponseObject.warning(msg);
  }

  private ResponseObject menuInfo(RequestInfo reqInfo) {
    ResponseObject response = new ResponseObject();
    String mName = reqInfo.getParameter("menu_name");
    String lRoot = reqInfo.getParameter("root_layout");
    String lItem = reqInfo.getParameter("item_layout");

    UiComponent menu = holder.getMenu(mName, lRoot, lItem, Config.getPath("menu.xml"));

    if (BeeUtils.isEmpty(menu)) {
      String msg = "Error initializing menu: " + mName;
      logger.warning(msg);
      response.addError(msg);
    } else {
      response.setResponse(menu);
    }
    return response;
  }

  private ResponseObject rebuildData(RequestInfo reqInfo) {
    ResponseObject response = new ResponseObject();

    String cmd = reqInfo.getContent();
    String[] arr = cmd.split(" ", 2);
    if (arr.length > 1) {
      cmd = arr[1];
    }
    if (BeeUtils.same(cmd, "all")) {
      sys.rebuildActiveTables();
      response.addInfo("Recreate structure OK");

    } else if (BeeUtils.same(cmd, "ext")) {
      sys.initExtensions();
      sys.initDatabase(SqlBuilderFactory.getEngine());
      response.addInfo("Extensions OK");

    } else if (BeeUtils.same(cmd, "views")) {
      sys.initViews();
      response.addInfo("Views OK");

    } else if (BeeUtils.same(cmd, "setState")) {
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
      response.addInfo("Toggle OK");

    } else {
      if (sys.isTable(cmd)) {
        sys.rebuildTable(cmd);
        response.addInfo("Rebuild", cmd, "OK");
      } else {
        response.addError("Unknown table:", cmd);
      }
    }
    return response;
  }
}
