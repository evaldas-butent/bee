package com.butent.bee.server.ui;

import com.google.common.collect.Lists;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.sql.Condition;
import com.butent.bee.shared.sql.IsCondition;
import com.butent.bee.shared.sql.SqlBuilderFactory;
import com.butent.bee.shared.sql.SqlSelect;
import com.butent.bee.shared.sql.SqlUtils;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.List;
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
  @EJB
  UserServiceBean usr;
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

      } else if (svc.equals("rpc_ui_data_info")) {
        response = getDataInfo();
      } else if (svc.equals("rpc_ui_gen")) {
        response = generateData(reqInfo);
      } else if (svc.equals("rpc_ui_row_count")) {
        response = getRowCount(reqInfo);

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
    Object res = qs.doSql(sql);

    if (res instanceof BeeRowSet) {
      ResponseObject resp = ResponseObject.response(res);
      resp.addWarning(usr.localMesssages().rowsRetrieved(((BeeRowSet) res).getNumberOfRows()));
      return resp;
    } else {
      return ResponseObject.warning("Affected rows:", res);
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
    SqlSelect ss = new SqlSelect()
        .addFields("f", "form").addFrom("forms", "f").addOrder("f", "form");
    return ResponseObject.response(qs.getColumn(ss));
  }

  private ResponseObject generateData(RequestInfo reqInfo) {
    ResponseObject response;

    String[] arr = BeeUtils.split(reqInfo.getContent(), BeeConst.STRING_SPACE);
    String tableName = ArrayUtils.getQuietly(arr, 0);
    int rowCount = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 1));

    if (!sys.isTable(tableName)) {
      response = ResponseObject.error("Unknown table:", tableName);
    } else if (rowCount <= 0 || rowCount > 10000) {
      response = ResponseObject.error("Invalid row count:", rowCount);
    } else {
      response = sys.generateData(tableName, rowCount);
    }
    return response;
  }

  private ResponseObject getDataInfo() {
    return ResponseObject.response(sys.getDataInfo());
  }

  private ResponseObject getRowCount(RequestInfo reqInfo) {
    String table = reqInfo.getParameter("table_name");
    String where = reqInfo.getParameter("table_where");

    IsCondition condition = null;
    if (!BeeUtils.isEmpty(where)) {
      condition = Condition.restore(where);
    }
    return ResponseObject.response(sys.getRowCount(table, condition));
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
    return ResponseObject.response(states);
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
    String where = reqInfo.getParameter("table_where");
    String order = reqInfo.getParameter("table_order");
    String states = reqInfo.getParameter("table_states");

    IsCondition condition = null;
    if (!BeeUtils.isEmpty(where)) {
      condition = Condition.restore(where);
    }

    List<String> lst = null;
    if (!BeeUtils.isEmpty(order)) {
      lst = Lists.newArrayList(BeeUtils.split(order, 1));
    }

    String[] arr = new String[0];
    if (!BeeUtils.isEmpty(states)) {
      arr = states.split(" ");
    }

    BeeRowSet res = sys.getViewData(table, condition, lst, limit, offset, arr);
    return ResponseObject.response(res);
  }

  private ResponseObject getTables() {
    return ResponseObject.response(sys.getTableNames());
  }

  private ResponseObject gridInfo(RequestInfo reqInfo) {
    String gName = reqInfo.getParameter("grid_name");
    String grd = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g").setWhere(
        SqlUtils.equal("g", "table", gName));

    String x = qs.getValue(ss);

    if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
      grd = x.replaceFirst("^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
    }

    ss = new SqlSelect();
    ss.addFields("c", "caption").addFrom("columns", "c").setWhere(
          SqlUtils.equal("c", "table", grd)).addOrder("c", "order");

    String[] data = qs.getColumn(ss);

    if (!BeeUtils.isEmpty(data)) {
      BeeRowSet rs = new BeeRowSet();

      for (String row : data) {
        String colName = row.replaceAll("['\"]", "");
        rs.addColumn(new BeeColumn(ValueType.TEXT, colName, BeeUtils.createUniqueName("col")));
      }
      for (int i = 0; i < 20; i++) {
        int cnt = data.length;
        String[] cells = new String[cnt];

        for (int j = 0; j < cnt; j++) {
          cells[j] = (j == 0 ? Integer.toString(i + 1) : BeeConst.STRING_EMPTY);
        }
        rs.addRow(i + 1, cells);
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

    } else if (BeeUtils.same(cmd, "states")) {
      sys.initStates();
      sys.initTables();
      sys.initDatabase(SqlBuilderFactory.getEngine());
      response.addInfo("Extensions OK");

    } else if (BeeUtils.same(cmd, "tables")) {
      sys.initTables();
      sys.initViews();
      sys.initDatabase(SqlBuilderFactory.getEngine());
      response.addInfo("Extensions OK");

    } else if (BeeUtils.same(cmd, "views")) {
      sys.initViews();
      response.addInfo("Views OK");

    } else if (BeeUtils.startsSame(cmd, "setState")) {
      String[] xArr = cmd.split(" ", 5);
      String tbl = xArr[1];
      long id = BeeUtils.toLong(xArr[2]);
      String state = xArr[3];
      long[] bits = null;

      if (xArr.length > 4) {
        String[] rArr = xArr[4].split(" ");
        bits = new long[rArr.length];

        for (int i = 0; i < rArr.length; i++) {
          bits[i] = BeeUtils.toLong(rArr[i]);
        }
      }
      sys.setState(tbl, id, state, bits);
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
