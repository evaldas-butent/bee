package com.butent.bee.server.ui;

import com.google.common.collect.Lists;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.ui.XmlSqlDesigner.DataField;
import com.butent.bee.server.ui.XmlSqlDesigner.DataKey;
import com.butent.bee.server.ui.XmlSqlDesigner.DataRelation;
import com.butent.bee.server.ui.XmlSqlDesigner.DataTable;
import com.butent.bee.server.ui.XmlSqlDesigner.DataType;
import com.butent.bee.server.ui.XmlSqlDesigner.DataTypeGroup;
import com.butent.bee.server.ui.XmlSqlDesigner.KeyType;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlKey;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Manages <code>rpc_data</code> type service requests from client side, including such services as
 * <code>GET_MENU, GET_FORM, DO_SQL</code>.
 */

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
  @EJB
  GridHolderBean grd;
  @Resource
  EJBContext ctx;

  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;

    String svc = reqInfo.getService();

    if (BeeUtils.same(svc, Service.GET_X_FORM)) {
      response = formInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_X_FORM_LIST)) {
      response = formList();
    } else if (BeeUtils.same(svc, Service.GET_MENU)) {
      response = menuInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_X_GRID)) {
      response = gridInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_GRID)) {
      response = getGrid(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_FORM)) {
      response = getForm(reqInfo);

    } else if (BeeUtils.same(svc, Service.REBUILD)) {
      response = rebuildData(reqInfo);
    } else if (BeeUtils.same(svc, Service.DO_SQL)) {
      response = doSql(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_TABLE_LIST)) {
      response = getTables();
    } else if (BeeUtils.same(svc, Service.QUERY)) {
      response = getViewData(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_STATES)) {
      response = getStates(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_STATE_TABLE)) {
      response = getStateTable(reqInfo);
    } else if (BeeUtils.same(svc, Service.COMMIT)) {
      response = commitChanges(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_VIEW_LIST)) {
      response = getViewList();
    } else if (BeeUtils.same(svc, Service.GENERATE)) {
      response = generateData(reqInfo);
    } else if (BeeUtils.same(svc, Service.COUNT_ROWS)) {
      response = getViewSize(reqInfo);
    } else if (BeeUtils.same(svc, Service.DELETE_ROWS)) {
      response = deleteRows(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_CELL)) {
      response = updateCell(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_ROW)) {
      response = updateRow(reqInfo);
    } else if (BeeUtils.same(svc, Service.INSERT_ROW)) {
      response = insertRow(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_VIEW_INFO)) {
      response = getViewInfo(reqInfo);

    } else {
      String msg = BeeUtils.concat(1, "data service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  public ResponseObject getViewInfo(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(0);
    List<ExtendedProperty> info = Lists.newArrayList();

    if (sys.isView(viewName)) {
      info.addAll(sys.getView(viewName).getInfo());
    } else {
      for (String name : sys.getViewNames()) {
        PropertyUtils.appendWithPrefix(info, name, sys.getView(name).getInfo());
      }
    }
    return ResponseObject.response(info);
  }

  private ResponseObject buildSchema() {
    XmlSqlDesigner designer = new XmlSqlDesigner();
    DataTypeGroup typeGroup = new DataTypeGroup();
    typeGroup.label = "SQL types";
    typeGroup.types = Lists.newArrayList();
    designer.types = Lists.newArrayList(typeGroup);
    designer.tables = Lists.newArrayList();

    for (SqlDataType type : SqlDataType.values()) {
      DataType dataType = new DataType();
      dataType.sql = type;
      dataType.label = dataType.sql.name();
      typeGroup.types.add(dataType);
    }
    for (String tableName : sys.getTableNames()) {
      XmlTable xmlTable = sys.getXmlTable(tableName);

      if (xmlTable != null) {
        DataTable table = new DataTable();
        table.name = xmlTable.name;
        table.fields = Lists.newArrayList(
            new DataField(xmlTable.idName, SqlDataType.LONG.name(), true,
                xmlTable.isProtected()));
        table.keys = Lists.newArrayList(new DataKey(KeyType.PRIMARY, xmlTable.idName));

        Collection<XmlField> fields = Lists.newArrayList();

        if (!BeeUtils.isEmpty(xmlTable.fields)) {
          fields.addAll(xmlTable.fields);
        }
        if (!BeeUtils.isEmpty(xmlTable.extFields)) {
          fields.addAll(xmlTable.extFields);
        }
        for (XmlField xmlField : fields) {
          String type = xmlField.type;

          if (BeeUtils.isPositive(xmlField.precision)) {
            type = type + "(" + xmlField.precision;

            if (BeeUtils.isPositive(xmlField.scale)) {
              type = type + "," + xmlField.scale;
            }
            type = type + ")";
          }
          DataField field =
              new DataField(xmlField.name, type, xmlField.notNull, xmlField.isProtected());

          if (!BeeUtils.isEmpty(xmlField.relation)) {
            field.relation =
                new DataRelation(xmlField.relation, sys.getIdName(xmlField.relation));
          }
          table.fields.add(field);

          if (xmlField.unique) {
            table.keys.add(new DataKey(KeyType.UNIQUE, xmlField.name));
          }
        }
        if (!BeeUtils.isEmpty(xmlTable.keys)) {
          for (XmlKey xmlKey : xmlTable.keys) {
            table.keys.add(new DataKey(xmlKey.unique ? KeyType.UNIQUE : KeyType.INDEX,
                xmlKey.fields.toArray(new String[0])));
          }
        }
        designer.tables.add(table);
      }
    }
    return ResponseObject.response(new BeeResource(null, XmlUtils.marshal(designer)));
  }

  private ResponseObject commitChanges(RequestInfo reqInfo) {
    ResponseObject response = sys.commitChanges(BeeRowSet.restore(reqInfo.getContent()));

    if (response.hasErrors()) {
      ctx.setRollbackOnly();
    }
    return response;
  }

  private ResponseObject deleteRows(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String rows = reqInfo.getParameter(Service.VAR_VIEW_ROWS);
    Assert.notEmpty(viewName);
    Assert.notEmpty(rows);

    ResponseObject response = new ResponseObject();
    BeeView view = sys.getView(viewName);
    int cnt = 0;

    if (view.isReadOnly()) {
      response.addError("View", view.getName(), "is read only.");
    } else {
      for (String s : Codec.beeDeserialize(rows)) {
        RowInfo row = RowInfo.restore(s);
        ResponseObject resp = sys.deleteRow(viewName, row);
        int res = resp.getResponse(-1, logger);

        if (res > 0) {
          cnt += res;
        } else {
          response.addError("Error deleting row:", row.getId());

          if (res < 0) {
            for (String err : resp.getErrors()) {
              response.addError(err);
            }
          } else {
            response.addError("Optimistic lock exception");
          }
        }
      }
    }
    return response.setResponse(cnt);
  }

  private ResponseObject doSql(RequestInfo reqInfo) {
    String sql = reqInfo.getContent();

    if (BeeUtils.isEmpty(sql)) {
      return ResponseObject.error("SQL command not found");
    }
    Object res = qs.doSql(sql);

    if (res instanceof BeeRowSet) {
      ResponseObject resp = ResponseObject.response(res);
      resp.addWarning(usr.localMesssages().rowsRetrieved(((BeeRowSet) res).getNumberOfRows()));
      return resp;
    } else if (res instanceof Number) {
      return ResponseObject.warning("Affected rows:", res);
    } else {
      return ResponseObject.error(res);
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

  private ResponseObject getForm(RequestInfo reqInfo) {
    String formName = reqInfo.getContent();
    if (BeeUtils.isEmpty(formName)) {
      return ResponseObject.error("Form name not specified");
    }

    String formPath = "forms/";
    String formExt = XmlUtils.defaultXmlExtension;

    String fileName = Config.getPath(formPath + formName + "." + formExt);
    String xml = null;
    if (!BeeUtils.isEmpty(fileName)) {
      xml = FileUtils.fileToString(fileName);
    }

    if (!BeeUtils.isEmpty(xml)) {
      return ResponseObject.response(xml);
    }
    return ResponseObject.error("form", formName, "not found");
  }

  private ResponseObject getGrid(RequestInfo reqInfo) {
    String gridName = reqInfo.getContent();
    if (BeeUtils.isEmpty(gridName)) {
      return ResponseObject.error("Which grid?");
    }
    if (grd.isGrid(gridName)) {
      return ResponseObject.response(grd.getGrid(gridName));
    }
    return ResponseObject.error("grid", gridName, "not found");
  }

  private ResponseObject getStates(RequestInfo reqInfo) {
    String table = reqInfo.getParameter(Service.VAR_VIEW_NAME);
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
    String table = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String states = reqInfo.getParameter(Service.VAR_VIEW_STATES);
    return sys.editStateRoles(table, states);
  }

  private ResponseObject getTables() {
    return ResponseObject.response(sys.getTableNames());
  }

  private ResponseObject getViewData(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String columns = reqInfo.getParameter(Service.VAR_VIEW_COLUMNS);

    int limit = BeeUtils.toInt(reqInfo.getParameter(Service.VAR_VIEW_LIMIT));
    int offset = BeeUtils.toInt(reqInfo.getParameter(Service.VAR_VIEW_OFFSET));

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    String sort = reqInfo.getParameter(Service.VAR_VIEW_ORDER);

    IsCondition condition = null;
    if (!BeeUtils.isEmpty(where)) {
      condition = sys.getViewCondition(viewName, Filter.restore(where));
    }

    Order order = null;
    if (!BeeUtils.isEmpty(sort)) {
      order = Order.restore(sort);
    }

    String[] cols = new String[0];
    if (!BeeUtils.isEmpty(columns)) {
      cols = columns.split(Service.VIEW_COLUMN_SEPARATOR);
    }

    BeeRowSet res = sys.getViewData(viewName, condition, order, limit, offset, cols);
    return ResponseObject.response(res);
  }

  private ResponseObject getViewList() {
    return ResponseObject.response(sys.getViewList());
  }

  private ResponseObject getViewSize(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    Filter filter = null;
    if (!BeeUtils.isEmpty(where)) {
      filter = Filter.restore(where);
    }
    return ResponseObject.response(sys.getViewSize(viewName,
        sys.getViewCondition(viewName, filter)));
  }

  private ResponseObject gridInfo(RequestInfo reqInfo) {
    String gName = reqInfo.getParameter("grid_name");
    String grid = gName;

    SqlSelect ss = new SqlSelect();
    ss.addFields("g", "properties").addFrom("grids", "g")
        .setWhere(SqlUtils.equal("g", "table", gName));

    String x = qs.getValue(ss);

    if (!BeeUtils.isEmpty(x) && x.contains("parent_table")) {
      grid = x.replaceFirst("^((?s).)*parent_table\\s*=\\s*[\\[\"'](.+)[\\]\"']((?s).)*$", "$2");
    }

    ss = new SqlSelect();
    ss.addFields("c", "caption").addFrom("columns", "c").setWhere(
          SqlUtils.equal("c", "table", grid)).addOrder("c", "order");

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
    String msg = "Grid name not recognized: " + grid;
    logger.warning(msg);
    return ResponseObject.warning(msg);
  }

  private ResponseObject insertRow(RequestInfo reqInfo) {
    return sys.insertRow(BeeRowSet.restore(reqInfo.getContent()), true);
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

    if (BeeUtils.same(cmd, "all")) {
      sys.rebuildActiveTables();
      response.addInfo("Recreate structure OK");

    } else if (BeeUtils.same(cmd, "states")) {
      sys.initStates();
      response.addInfo("Extensions OK");

    } else if (BeeUtils.same(cmd, "tables")) {
      sys.initTables();
      response.addInfo("Extensions OK");

    } else if (BeeUtils.same(cmd, "views")) {
      sys.initViews();
      response.addInfo("Views OK");

    } else if (BeeUtils.same(cmd, "grids")) {
      grd.initGrids();
      response.addInfo("Grids OK");

    } else if (BeeUtils.startsSame(cmd, "setState")) {
      String[] arr = cmd.split(" ", 5);
      String tbl = arr[1];
      long id = BeeUtils.toLong(arr[2]);
      String state = arr[3];
      long[] bits = null;

      if (arr.length > 4) {
        String[] rArr = arr[4].split(" ");
        bits = new long[rArr.length];

        for (int i = 0; i < rArr.length; i++) {
          bits[i] = BeeUtils.toLong(rArr[i]);
        }
      }
      sys.setState(tbl, id, state, bits);
      response.addInfo("Toggle OK");

    } else if (BeeUtils.startsSame(cmd, "schema")) {
      String schema = cmd.substring("schema".length());

      if (BeeUtils.isEmpty(schema)) {
        response = buildSchema();
      } else {
        response = saveSchema(schema);
      }
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

  private ResponseObject saveSchema(String schema) {
    return ResponseObject.warning(schema);
  }

  private ResponseObject updateCell(RequestInfo reqInfo) {
    return sys.updateRow(BeeRowSet.restore(reqInfo.getContent()), false);
  }

  private ResponseObject updateRow(RequestInfo reqInfo) {
    return sys.updateRow(BeeRowSet.restore(reqInfo.getContent()), true);
  }
}
