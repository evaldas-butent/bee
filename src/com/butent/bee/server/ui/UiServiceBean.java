package com.butent.bee.server.ui;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.client.ui.DsnService;
import com.butent.bee.server.Config;
import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.InitializationBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeTable.BeeRelation;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SearchBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.ExtensionFilter;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.ModuleHolderBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.ui.XmlSqlDesigner.DataType;
import com.butent.bee.server.ui.XmlSqlDesigner.DataTypeGroup;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SqlConstants.SqlDataType;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.XmlTable.XmlRelation;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.utils.XmlHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

/**
 * Manages <code>rpc_data</code> type service requests from client side, including such services as
 * <code>GET_MENU, GET_FORM, DO_SQL</code>.
 */

@Stateless
@LocalBean
public class UiServiceBean {

  private static BeeLogger logger = LogUtils.getLogger(UiServiceBean.class);

  @EJB
  UiHolderBean ui;
  @EJB
  QueryServiceBean qs;
  @EJB
  IdGeneratorBean ig;
  @EJB
  SystemBean sys;
  @EJB
  DataEditorBean deb;
  @EJB
  UserServiceBean usr;
  @EJB
  GridLoaderBean grd;
  @EJB
  DataSourceBean dsb;
  @EJB
  ModuleHolderBean mod;
  @EJB
  MailModuleBean mail;
  @EJB
  SearchBean search;
  @EJB
  InitializationBean ib;

  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;

    String svc = reqInfo.getService();

    if (BeeUtils.same(svc, Service.GET_GRID)) {
      response = getGrid(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_FORM)) {
      response = getForm(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_DECORATORS)) {
      response = getDecorators();

    } else if (BeeUtils.same(svc, Service.MAIL)) {
      response = doMail(reqInfo);
    } else if (BeeUtils.same(svc, Service.REBUILD)) {
      response = rebuildData(reqInfo);
    } else if (BeeUtils.same(svc, Service.DO_SQL)) {
      response = doSql(reqInfo);
    } else if (BeeUtils.same(svc, Service.QUERY)) {
      response = getViewData(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_DATA_INFO)) {
      response = getDataInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GENERATE)) {
      response = generateData(reqInfo);
    } else if (BeeUtils.same(svc, Service.COUNT_ROWS)) {
      response = getViewSize(reqInfo);
    } else if (BeeUtils.same(svc, Service.DELETE_ROWS)) {
      response = deleteRows(reqInfo);
    } else if (BeeUtils.same(svc, Service.DELETE)) {
      response = delete(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_CELL)) {
      response = updateCell(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_ROW)) {
      response = updateRow(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE)) {
      response = update(reqInfo);
    } else if (BeeUtils.same(svc, Service.INSERT_ROW)) {
      response = insertRow(reqInfo);
    } else if (BeeUtils.same(svc, Service.INSERT_ROWS)) {
      response = insertRows(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_VIEW_INFO)) {
      response = getViewInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_TABLE_INFO)) {
      response = getTableInfo(reqInfo);

    } else if (BeeUtils.same(svc, DsnService.SVC_GET_DSNS)) {
      response = getDsns();
    } else if (BeeUtils.same(svc, DsnService.SVC_SWITCH_DSN)) {
      response = switchDsn(reqInfo.getParameter(DsnService.VAR_DSN));

    } else if (BeeUtils.same(svc, Service.SEARCH)) {
      response = search.processQuery(reqInfo.getParameter(0));

    } else {
      String msg = BeeUtils.joinWords("data service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  private void buildDbList(String rootTable, Set<String> tables, boolean initial) {
    boolean recurse = BeeUtils.isSuffix(rootTable, '*');
    String root = BeeUtils.normalize(BeeUtils.removeSuffix(rootTable, '*'));

    if (!initial && tables.contains(root) || !sys.isTable(root)) {
      return;
    }
    tables.add(root);

    for (String tbl : sys.getTableNames()) {
      if (!tables.contains(BeeUtils.normalize(tbl))) {
        for (BeeField field : sys.getTableFields(tbl)) {
          if (field instanceof BeeRelation
              && BeeUtils.same(((BeeRelation) field).getRelation(), root)) {
            if (recurse) {
              buildDbList(tbl + '*', tables, false);
            } else {
              tables.add(BeeUtils.normalize(tbl));
            }
          }
        }
      }
    }
  }

  private ResponseObject buildDbSchema(Iterable<String> roots) {
    XmlSqlDesigner designer = new XmlSqlDesigner();
    designer.types = Lists.newArrayList();
    designer.tables = Lists.newArrayList();

    for (int i = 0; i < 2; i++) {
      boolean extMode = (i > 0);
      DataTypeGroup typeGroup = new DataTypeGroup();
      typeGroup.label = BeeUtils.joinWords("SQL", extMode ? "extended" : "", "types");
      typeGroup.color = (extMode ? "rgb(0,255,0)" : "rgb(255,255,255)");
      typeGroup.types = Lists.newArrayList();

      for (SqlDataType type : SqlDataType.values()) {
        String typeName = type.name();
        DataType dataType = new DataType();
        dataType.label = (extMode ? "Extended " : "") + typeName;
        dataType.sql = typeName + (extMode ? XmlSqlDesigner.EXT : "");
        typeGroup.types.add(dataType);
      }
      designer.types.add(typeGroup);
    }
    DataTypeGroup typeGroup = new DataTypeGroup();
    typeGroup.label = "Table states";
    typeGroup.color = "rgb(255,0,0)";

    DataType dataType = new DataType();
    dataType.label = "STATE";
    dataType.sql = XmlSqlDesigner.STATE;
    typeGroup.types = Lists.newArrayList(dataType);

    designer.types.add(typeGroup);

    Set<String> tables = Sets.newHashSet();

    if (roots == null || !roots.iterator().hasNext()) {
      roots = sys.getTableNames();
    }
    for (String root : roots) {
      buildDbList(root, tables, true);
    }
    for (String tableName : tables) {
      XmlTable xmlTable = sys.getXmlTable(sys.getTable(tableName).getModuleName(), tableName);

      if (xmlTable != null) {
        Collection<XmlField> fields = Lists.newArrayList();

        if (!BeeUtils.isEmpty(xmlTable.fields)) {
          fields.addAll(xmlTable.fields);
        }
        if (!BeeUtils.isEmpty(xmlTable.extFields)) {
          fields.addAll(xmlTable.extFields);
        }
        for (XmlField xmlField : fields) {
          if (xmlField instanceof XmlRelation) {
            XmlRelation xmlRelation = (XmlRelation) xmlField;
            xmlRelation.relationField = sys.getIdName(xmlRelation.relation);
          }
        }
        designer.tables.add(xmlTable);
      }
    }
    return ResponseObject.response(new Resource(null, XmlUtils.marshal(designer, null)));
  }

  private ResponseObject delete(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    Assert.notEmpty(viewName);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    Assert.notEmpty(where);

    BeeView view = sys.getView(viewName);
    if (view.isReadOnly()) {
      return ResponseObject.error("View", BeeUtils.bracket(view.getName()), "is read only.");
    }

    String tblName = view.getSourceName();
    Filter filter = Filter.restore(where);

    return qs.updateDataWithResponse(new SqlDelete(tblName).setWhere(view.getCondition(filter)));
  }

  private ResponseObject deleteRows(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    Assert.notEmpty(viewName);
    String[] entries = Codec.beeDeserializeCollection(reqInfo.getParameter(Service.VAR_VIEW_ROWS));
    Assert.isPositive(ArrayUtils.length(entries));
    RowInfo[] rows = new RowInfo[entries.length];

    for (int i = 0; i < entries.length; i++) {
      rows[i] = RowInfo.restore(entries[i]);
    }
    return deb.deleteRows(viewName, rows);
  }

  private ResponseObject doMail(RequestInfo reqInfo) {
    int c = 0;
    String to = null;
    String subject = null;
    String body = null;

    if (!BeeUtils.isEmpty(reqInfo.getContent())) {
      for (String part : Splitter.on(CharMatcher.is(';')).trimResults().omitEmptyStrings()
          .split(reqInfo.getContent())) {
        switch (++c) {
          case 1:
            to = part;
            break;
          case 2:
            subject = part;
            break;
          case 3:
            body = part;
            break;
        }
      }
    }
    if (c < 3) {
      return ResponseObject.error("Syntax: mail <ToAddress>;<Subject>;<Body>");
    }
    Long sender = qs.getLong(new SqlSelect()
        .addFields(TBL_ACCOUNTS, COL_ADDRESS)
        .addFrom(TBL_ACCOUNTS)
        .setWhere(SqlUtils.and(SqlUtils.equal(TBL_ACCOUNTS, COL_USER, usr.getCurrentUserId()),
            SqlUtils.notNull(TBL_ACCOUNTS, COL_ACCOUNT_DEFAULT))));

    if (!DataUtils.isId(sender)) {
      return ResponseObject.error("No default mail account for user:", usr.getCurrentUser());
    }
    try {
      mail.sendMail(mail.getAccount(sender),
          Sets.newHashSet(mail.storeAddress(new InternetAddress(to))),
          null, null, subject, body, null);
    } catch (MessagingException e) {
      return ResponseObject.error(e);
    }
    return ResponseObject.info("Mail sent");
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

  private ResponseObject generateData(RequestInfo reqInfo) {
    ResponseObject response;

    String[] arr = BeeUtils.split(reqInfo.getContent(), BeeConst.CHAR_SPACE);
    String tableName = ArrayUtils.getQuietly(arr, 0);
    int rowCount = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 1));
    int refCount = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 2));
    int childCount = BeeUtils.toInt(ArrayUtils.getQuietly(arr, 3));

    if (BeeUtils.isEmpty(tableName)) {
      return ResponseObject.error("Syntax: gen <table> <rowCount> [refCount] [childCount]");
    } else if (!sys.isTable(tableName)) {
      response = ResponseObject.error("Unknown table:", tableName);
    } else if (rowCount <= 0 || rowCount > 100000) {
      response = ResponseObject.error("Invalid row count:", rowCount);
    } else {
      response = deb.generateData(tableName, rowCount, refCount, childCount, null);
    }
    return response;
  }

  private ResponseObject getDataInfo(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    if (BeeUtils.isEmpty(viewName)) {
      return ResponseObject.response(sys.getDataInfo());
    } else {
      DataInfo dataInfo = sys.getDataInfo(viewName);
      dataInfo.setRowCount(qs.getViewSize(viewName, null));
      return ResponseObject.response(dataInfo);
    }
  }

  private ResponseObject getDecorators() {
    File dir = new File(Config.WEB_INF_DIR, DecoratorConstants.DIRECTORY);
    List<File> files = FileUtils.findFiles(dir, Lists.newArrayList(FileUtils.INPUT_FILTER,
        new ExtensionFilter(XmlUtils.DEFAULT_XML_EXTENSION)));
    if (files.isEmpty()) {
      return ResponseObject.error("getDecorators: no xml found in", dir.getPath());
    }

    Document dstDoc = XmlUtils.createDocument();
    Element dstRoot = dstDoc.createElement(DecoratorConstants.TAG_DECORATORS);
    dstRoot.setAttribute(XmlHelper.ATTR_XMLNS, DecoratorConstants.NAMESPACE);
    dstDoc.appendChild(dstRoot);

    for (File file : files) {
      String path = file.getPath();

      Document srcDoc =
          XmlUtils.getXmlResource(path, Config.getSchemaPath(DecoratorConstants.SCHEMA));
      if (srcDoc == null) {
        return ResponseObject.error("getDecorators: cannot load xml:", path);
      }

      List<Element> elements = XmlUtils.getChildrenElements(srcDoc.getDocumentElement(),
          Sets.newHashSet(DecoratorConstants.TAG_ABSTRACT, DecoratorConstants.TAG_DECORATOR));
      if (elements.isEmpty()) {
        logger.warning("no decorators found in", path);
      }

      for (Element decorator : elements) {
        dstRoot.appendChild(dstDoc.importNode(decorator, true));
      }
      logger.debug(elements.size(), "decorators loaded from", path);
    }
    return ResponseObject.response(XmlUtils.toString(dstDoc, false));
  }

  private ResponseObject getDsns() {
    return ResponseObject.response(dsb.getDsns());
  }

  private ResponseObject getForm(RequestInfo reqInfo) {
    String formName = reqInfo.getContent();

    if (BeeUtils.isEmpty(formName)) {
      return ResponseObject.error("Which form?");
    }
    if (ui.isForm(formName)) {
      return ui.getForm(formName);
    }
    return ResponseObject.error("Form", formName, "not found");
  }

  private ResponseObject getGrid(RequestInfo reqInfo) {
    String gridName = reqInfo.getContent();

    if (BeeUtils.isEmpty(gridName)) {
      return ResponseObject.error("Which grid?");
    }
    if (ui.isGrid(gridName)) {
      return ResponseObject.response(ui.getGrid(gridName));
    }
    if (sys.isView(gridName)) {
      return ResponseObject.response(grd.getDefaultGrid(sys.getView(gridName)));
    }
    return ResponseObject.error("Grid", gridName, "not found");
  }

  private ResponseObject getTableInfo(RequestInfo reqInfo) {
    String tableName = reqInfo.getParameter(0);
    List<ExtendedProperty> info = Lists.newArrayList();

    if (sys.isTable(tableName)) {
      info.addAll(sys.getTableInfo(tableName));
    } else {
      for (String name : sys.getTableNames()) {
        PropertyUtils.appendWithPrefix(info, name, sys.getTableInfo(name));
      }
    }
    return ResponseObject.response(info);
  }

  private ResponseObject getViewData(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String columns = reqInfo.getParameter(Service.VAR_VIEW_COLUMNS);

    int limit = BeeUtils.toInt(reqInfo.getParameter(Service.VAR_VIEW_LIMIT));
    int offset = BeeUtils.toInt(reqInfo.getParameter(Service.VAR_VIEW_OFFSET));

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    String sort = reqInfo.getParameter(Service.VAR_VIEW_ORDER);

    String getSize = reqInfo.getParameter(Service.VAR_VIEW_SIZE);
    String rowId = reqInfo.getParameter(Service.VAR_VIEW_ROW_ID);

    Filter filter = null;
    if (!BeeUtils.isEmpty(rowId)) {
      filter = ComparisonFilter.compareId(BeeUtils.toLong(rowId));
    } else if (!BeeUtils.isEmpty(where)) {
      filter = Filter.restore(where);
    }
    Order order = null;
    if (!BeeUtils.isEmpty(sort)) {
      order = Order.restore(sort);
    }

    List<String> colNames = NameUtils.toList(columns);

    int cnt = BeeConst.UNDEF;
    if (!BeeUtils.isEmpty(getSize)) {
      cnt = qs.getViewSize(viewName, filter);
    }
    BeeRowSet res = qs.getViewData(viewName, filter, order, limit, offset, colNames);

    if (cnt >= 0 && res != null) {
      res.setTableProperty(Service.VAR_VIEW_SIZE,
          BeeUtils.toString(Math.max(cnt, res.getNumberOfRows())));
    }
    return ResponseObject.response(res);
  }

  private ResponseObject getViewInfo(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(0);
    List<ExtendedProperty> info = Lists.newArrayList();

    if (!BeeUtils.isEmpty(viewName)) {
      if (sys.isView(viewName)) {
        info.addAll(sys.getView(viewName).getExtendedInfo());
      } else {
        return ResponseObject.warning("Unknown view name:", viewName);
      }
    } else {
      for (String name : sys.getViewNames()) {
        PropertyUtils.appendWithPrefix(info, name, sys.getView(name).getExtendedInfo());
      }
    }
    return ResponseObject.response(info);
  }

  private ResponseObject getViewSize(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);

    Filter filter = null;
    if (!BeeUtils.isEmpty(where)) {
      filter = Filter.restore(where);
    }
    return ResponseObject.response(qs.getViewSize(viewName, filter));
  }

  private ResponseObject insertRow(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), true);
  }

  private ResponseObject insertRows(RequestInfo reqInfo) {
    BeeRowSet rowSet = BeeRowSet.restore(reqInfo.getContent());
    if (rowSet == null || rowSet.isEmpty() || rowSet.getNumberOfColumns() <= 0
        || !sys.isView(rowSet.getViewName())) {
      return ResponseObject.error("insertRows:", "invalid rowSet");
    }

    ResponseObject response = deb.commitRow(rowSet, 0, BeeRowSet.class);
    if (response.hasErrors() || rowSet.getNumberOfRows() <= 1) {
      return response;
    }
    BeeRowSet result = (BeeRowSet) response.getResponse();

    for (int i = 1; i < rowSet.getNumberOfRows(); i++) {
      response = deb.commitRow(rowSet, i, BeeRow.class);
      if (response.hasErrors()) {
        return response;
      }
      result.addRow((BeeRow) response.getResponse());
    }
    return ResponseObject.response(result);
  }

  private ResponseObject rebuildData(RequestInfo reqInfo) {
    ResponseObject response = new ResponseObject();

    String cmd = reqInfo.getContent();

    if (BeeUtils.same(cmd, "all")) {
      sys.rebuildActiveTables();
      response.addInfo("Recreate structure OK");

    } else if (BeeUtils.same(cmd, "tables")) {
      sys.initTables();
      response.addInfo("Tables OK");

    } else if (BeeUtils.same(cmd, "views")) {
      sys.initViews();
      response.addInfo("Views OK");

    } else if (BeeUtils.same(cmd, "grids")) {
      ui.initGrids();
      response.addInfo("Grids OK");

    } else if (BeeUtils.same(cmd, "menu")) {
      ui.initMenu();
      response.addInfo("Menu OK");

    } else if (BeeUtils.startsSame(cmd, "check")) {
      String err = null;
      List<String> tbls = Lists.newArrayList();
      int idx = -1;

      for (String w : NameUtils.NAME_SPLITTER.split(cmd)) {
        idx++;

        if (idx == 0) {
          continue;
        } else if (idx == 1 && BeeUtils.same(w, "all")) {
          break;
        } else if (!sys.isTable(w)) {
          err = BeeUtils.joinWords("Unknown table:", w);
          break;
        } else {
          tbls.add(w);
        }
      }
      if (BeeUtils.isEmpty(err)) {
        List<Property> resp = sys.checkTables(tbls.toArray(new String[0]));

        if (BeeUtils.isEmpty(resp)) {
          response.addWarning("No changes in table structure");
        } else {
          response.setResponse(resp);
        }
      } else {
        response.addError(err);
      }
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
      deb.setState(tbl, id, state, bits);
      response.addInfo("Toggle OK");

    } else if (BeeUtils.startsSame(cmd, "schema")) {
      String schema = cmd.substring("schema".length()).trim();

      response = buildDbSchema(Splitter.onPattern("[ ,]").trimResults().omitEmptyStrings()
          .split(schema));

    } else if (!BeeUtils.isEmpty(cmd)) {
      String tbl = NameUtils.getWord(cmd, 0);
      if (sys.isTable(tbl)) {
        sys.rebuildTable(tbl);
        response.addInfo("Rebuild", tbl, "OK");
      } else {
        response.addError("Unknown table:", tbl);
      }

    } else {
      response.addError("Rebuild what?");
    }
    return response;
  }

  private ResponseObject switchDsn(String dsn) {
    if (!BeeUtils.isEmpty(dsn)) {
      ig.destroy();
      sys.initTables(dsn);
      ib.init();
      return ResponseObject.response(dsn);
    }
    return ResponseObject.error("DSN not specified");
  }

  private ResponseObject update(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    if (BeeUtils.isEmpty(viewName)) {
      return ResponseObject.error("parameter not found:", Service.VAR_VIEW_NAME);
    }

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    if (BeeUtils.isEmpty(where)) {
      return ResponseObject.error("parameter not found:", Service.VAR_VIEW_WHERE);
    }

    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.error("parameter not found:", Service.VAR_COLUMN);
    }

    String value = reqInfo.getParameter(Service.VAR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.error("parameter not found:", Service.VAR_VALUE);
    }

    BeeView view = sys.getView(viewName);
    if (view.isReadOnly()) {
      return ResponseObject.error("View", BeeUtils.bracket(view.getName()), "is read only.");
    }

    String tblName = view.getSourceName();
    Filter filter = Filter.restore(where);

    return qs.updateDataWithResponse(new SqlUpdate(tblName).setWhere(view.getCondition(filter))
        .addConstant(column, Value.restore(value)));
  }

  private ResponseObject updateCell(RequestInfo reqInfo) {
    BeeRowSet rs = BeeRowSet.restore(reqInfo.getContent());
    ResponseObject response = deb.commitRow(rs, false);

    if (!response.hasErrors()) {
      long rowId = rs.getRow(0).getId();
      BeeRowSet updated = qs.getViewData(rs.getViewName(), ComparisonFilter.compareId(rowId), null,
          BeeConst.UNDEF, BeeConst.UNDEF, DataUtils.getColumnNames(rs.getColumns()));

      if (DataUtils.isEmpty(updated)) {
        response = ResponseObject.error("could not read updated row");
      } else {
        response = ResponseObject.response(updated.getRow(0));
      }
    }
    return response;
  }

  private ResponseObject updateRow(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), true);
  }
}
