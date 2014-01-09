package com.butent.bee.server.ui;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

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
import com.butent.bee.server.modules.ec.TecDocBean;
import com.butent.bee.server.modules.mail.MailModuleBean;
import com.butent.bee.server.news.NewsBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.ui.XmlSqlDesigner.DataType;
import com.butent.bee.server.ui.XmlSqlDesigner.DataTypeGroup;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
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
import com.butent.bee.shared.modules.commons.CommonsConstants.RightsState;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.utils.XmlHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

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
  @EJB
  TecDocBean tcd;
  @EJB
  NewsBean news;

  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;

    String svc = reqInfo.getService();

    if (BeeUtils.same(svc, Service.GET_GRID)) {
      response = getGrid(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_FORM)) {
      response = getForm(reqInfo);

    } else if (BeeUtils.same(svc, Service.MAIL)) {
      response = doMail(reqInfo);
    } else if (BeeUtils.same(svc, Service.REBUILD)) {
      response = rebuildData(reqInfo);
    } else if (BeeUtils.same(svc, Service.DO_SQL)) {
      response = doSql(reqInfo);
    } else if (BeeUtils.same(svc, Service.QUERY)) {
      response = getViewData(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_VALUE)) {
      response = getValue(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_DATA)) {
      response = getData(reqInfo);

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
    } else if (BeeUtils.same(svc, Service.GET_VIEW_INFO)) {
      response = getViewInfo(reqInfo);
    } else if (BeeUtils.same(svc, Service.GET_TABLE_INFO)) {
      response = getTableInfo(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_DSNS)) {
      response = getDsns();
    } else if (BeeUtils.same(svc, Service.SWITCH_DSN)) {
      response = switchDsn(reqInfo.getParameter(Service.VAR_DSN));

    } else if (BeeUtils.same(svc, Service.SEARCH)) {
      response = search.processQuery(reqInfo.getParameter(0));
    } else if (BeeUtils.same(svc, Service.HISTOGRAM)) {
      response = getHistogram(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_RELATED_VALUES)) {
      response = getRelatedValues(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_RELATED_VALUES)) {
      response = updateRelatedValues(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_DECORATORS)) {
      response = getDecorators();

    } else if (BeeUtils.same(svc, Service.GET_AUTOCOMPLETE)) {
      response = getAutocomplete();
    } else if (BeeUtils.same(svc, Service.UPDATE_AUTOCOMPLETE)) {
      response = updateAutocomplete(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_NEWS)) {
      response = news.getNews();
    } else if (BeeUtils.same(svc, Service.SUBSCRIBE_TO_FEEDS)) {
      response = news.subscribe(reqInfo);
    } else if (BeeUtils.same(svc, Service.ACCESS)) {
      response = news.onAccess(reqInfo);

    } else if (BeeUtils.same(svc, Service.IMPORT_OSAMA_TIEKEJAI)) {
      response = importOsamaTiekejai(reqInfo);
    } else if (BeeUtils.same(svc, Service.IMPORT_OSAMA_DARBUOTOJIAI)) {
      response = importOsamaDarbuotojai(reqInfo);
    } else if (BeeUtils.same(svc, Service.IMPORT_OSAMA_PREK_SIST)) {
      response = importOsamaPrekSist(reqInfo);
    } else {
      String msg = BeeUtils.joinWords("data service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  public ResponseObject getAutocomplete() {
    SqlSelect query = new SqlSelect()
        .addFields(TBL_AUTOCOMPLETE, COL_AUTOCOMPLETE_KEY, COL_AUTOCOMPLETE_VALUE)
        .addFrom(TBL_AUTOCOMPLETE)
        .setWhere(SqlUtils.equals(TBL_AUTOCOMPLETE, COL_AUTOCOMPLETE_USER, usr.getCurrentUserId()))
        .addOrder(TBL_AUTOCOMPLETE, COL_AUTOCOMPLETE_KEY, sys.getVersionName(TBL_AUTOCOMPLETE));

    SimpleRowSet data = qs.getData(query);

    if (!DataUtils.isEmpty(data)) {
      List<String> result = Lists.newArrayList();

      ListMultimap<String, String> map = ArrayListMultimap.create();
      for (SimpleRow row : data) {
        map.put(row.getValue(COL_AUTOCOMPLETE_KEY), row.getValue(COL_AUTOCOMPLETE_VALUE));
      }

      for (String key : map.keySet()) {
        result.add(key);

        List<String> values = map.get(key);
        result.add(Integer.toString(values.size()));
        result.addAll(values);
      }

      return ResponseObject.response(result);

    } else {
      return ResponseObject.emptyResponse();
    }
  }

  public ResponseObject getDecorators() {
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

  public BeeRowSet getFavorites() {
    return qs.getViewData(TBL_FAVORITES, usr.getCurrentUserFilter(COL_FAVORITE_USER));
  }

  public BeeRowSet getFilters() {
    Order order = new Order(COL_FILTER_KEY, true);
    order.add(COL_FILTER_ORDINAL, true);

    return qs.getViewData(TBL_FILTERS, usr.getCurrentUserFilter(COL_FILTER_USER), order);
  }

  public Pair<BeeRowSet, BeeRowSet> getGridAndColumnSettings() {
    Filter userFilter = usr.getCurrentUserFilter(GridDescription.COL_GRID_SETTING_USER);

    BeeRowSet gridSettings =
        DataUtils.emptyToNull(qs.getViewData(GridDescription.VIEW_GRID_SETTINGS, userFilter));
    BeeRowSet columnSettings =
        DataUtils.emptyToNull(qs.getViewData(ColumnDescription.VIEW_COLUMN_SETTINGS, userFilter));

    return Pair.of(gridSettings, columnSettings);
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
      boolean extMode = i > 0;
      DataTypeGroup typeGroup = new DataTypeGroup();
      typeGroup.label = BeeUtils.joinWords("SQL", extMode ? "extended" : "", "types");
      typeGroup.color = extMode ? "rgb(0,255,0)" : "rgb(255,255,255)";
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
    Iterable<String> r;

    if (roots == null || !roots.iterator().hasNext()) {
      r = sys.getTableNames();
    } else {
      r = roots;
    }
    for (String root : r) {
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

    SqlDelete delete = new SqlDelete(tblName)
        .setWhere(view.getCondition(filter, sys.getViewFinder()));
    return qs.updateDataWithResponse(delete);
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
        .addFields(MailConstants.TBL_ACCOUNTS, MailConstants.COL_ADDRESS)
        .addFrom(MailConstants.TBL_ACCOUNTS)
        .setWhere(SqlUtils.and(SqlUtils.equals(MailConstants.TBL_ACCOUNTS, MailConstants.COL_USER,
            usr.getCurrentUserId()),
            SqlUtils.notNull(MailConstants.TBL_ACCOUNTS, MailConstants.COL_ACCOUNT_DEFAULT))));

    if (!DataUtils.isId(sender)) {
      return ResponseObject.error("No default mail account for user:", usr.getCurrentUser());
    }

    ResponseObject response = mail.sendMail(sender, to, subject, body);
    if (response.isEmpty()) {
      response.addInfo("Mail sent");
    }
    return response;
  }

  private ResponseObject doSql(RequestInfo reqInfo) {
    String sql = reqInfo.getContent();

    if (BeeUtils.isEmpty(sql)) {
      return ResponseObject.error("SQL command not found");
    }
    Object res = qs.doSql(sql);

    if (res instanceof BeeRowSet) {
      ResponseObject resp = ResponseObject.response(res);
      int rc = ((BeeRowSet) res).getNumberOfRows();
      resp.addWarning(usr.getLocalizableMesssages().rowsRetrieved(rc));
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
      Set<String> cache = Sets.newHashSet();
      response = deb.generateData(tableName, rowCount, refCount, childCount, cache);
    }
    return response;
  }

  private ResponseObject getData(RequestInfo reqInfo) {
    String viewList = reqInfo.getParameter(Service.VAR_VIEW_LIST);
    if (BeeUtils.isEmpty(viewList)) {
      return ResponseObject.parameterNotFound(Service.VAR_VIEW_LIST);
    }

    List<String> viewNames = NameUtils.toList(viewList);
    List<BeeRowSet> result = Lists.newArrayList();

    for (String viewName : viewNames) {
      BeeRowSet rs = qs.getViewData(viewName);
      result.add(rs);
    }
    return ResponseObject.response(result);
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

  private ResponseObject getHistogram(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String columns = reqInfo.getParameter(Service.VAR_VIEW_COLUMNS);

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    String order = reqInfo.getParameter(Service.VAR_VIEW_ORDER);

    Filter filter = BeeUtils.isEmpty(where) ? null : Filter.restore(where);

    SimpleRowSet res = qs.getHistogram(viewName, filter, NameUtils.toList(columns),
        NameUtils.toList(order));
    return ResponseObject.response(res);
  }

  private ResponseObject getRelatedValues(RequestInfo reqInfo) {
    String tableName = reqInfo.getParameter(Service.VAR_TABLE);
    if (BeeUtils.isEmpty(tableName)) {
      return ResponseObject.parameterNotFound(Service.VAR_TABLE);
    }

    String filterColumn = reqInfo.getParameter(Service.VAR_FILTER_COLUMN);
    if (BeeUtils.isEmpty(filterColumn)) {
      return ResponseObject.parameterNotFound(Service.VAR_FILTER_COLUMN);
    }

    Long filterValue = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_VALUE));
    if (!DataUtils.isId(filterValue)) {
      return ResponseObject.parameterNotFound(Service.VAR_VALUE);
    }

    String resultColumn = reqInfo.getParameter(Service.VAR_VALUE_COLUMN);
    if (BeeUtils.isEmpty(resultColumn)) {
      return ResponseObject.parameterNotFound(Service.VAR_VALUE_COLUMN);
    }

    Long[] values = qs.getRelatedValues(tableName, filterColumn, filterValue, resultColumn);
    String response = DataUtils.buildIdList(values);

    return ResponseObject.response(Strings.nullToEmpty(response));
  }

  private ResponseObject getTableInfo(RequestInfo reqInfo) {
    String tableName = reqInfo.getParameter(0);
    List<ExtendedProperty> info = Lists.newArrayList();

    if (sys.isTable(tableName)) {
      info.addAll(sys.getTableInfo(tableName));

    } else {
      List<String> names = sys.getTableNames();
      Collections.sort(names);

      if (BeeUtils.isEmpty(tableName)) {
        for (String name : names) {
          PropertyUtils.appendWithPrefix(info, name, sys.getTableInfo(name));
        }

      } else {
        for (String name : names) {
          if (BeeUtils.containsSame(name, tableName)) {
            PropertyUtils.appendWithPrefix(info, name, sys.getTableInfo(name));
          }
        }

        if (info.isEmpty()) {
          return ResponseObject.warning("table not found", tableName);
        }
      }
    }
    return ResponseObject.response(info);
  }

  private ResponseObject getValue(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    String rowId = reqInfo.getParameter(Service.VAR_VIEW_ROW_ID);
    String column = reqInfo.getParameter(Service.VAR_COLUMN);

    Filter filter = ComparisonFilter.compareId(BeeUtils.toLong(rowId));

    BeeRowSet rowSet = qs.getViewData(viewName, filter, null, BeeConst.UNDEF, BeeConst.UNDEF,
        Lists.newArrayList(column));

    if (DataUtils.isEmpty(rowSet)) {
      return ResponseObject.response(null, String.class).addWarning("row not found", viewName,
          rowId);
    } else {
      String value = rowSet.getString(0, column);
      return ResponseObject.response(value, String.class);
    }
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

  @Deprecated
  private ResponseObject importOsamaDarbuotojai(RequestInfo reqInfo) {
    String fileId = reqInfo.getContent();

    SqlSelect select = new SqlSelect().addField("Files", "Repository", "Path")
        .addFrom("Files")
        .setWhere(SqlUtils.equals("Files", sys.getIdName("Files"), Long.parseLong(fileId)));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet fd = qs.getData(select);

    String path = fd.getValue(0, fd.getColumnIndex("Path"));

    select = new SqlSelect().addField(TBL_COMPANIES,
        sys.getIdName(TBL_COMPANIES), "oID")
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.contains(TBL_COMPANIES, COL_COMPANY_NAME, "Osama"));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet osamaCompany = qs.getData(select);

    if (osamaCompany.isEmpty()) {
      return ResponseObject.error("Osama company not found");
    }

    long osamaCompanyId = osamaCompany.getLong(0, 0);

    try {

      FileInputStream fstream = new FileInputStream(path);

      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;

      while ((strLine = br.readLine()) != null) {
        Splitter splitter = Splitter.on(';');
        List<String> lst = Lists.newArrayList(splitter.split(strLine));

        String[] data = ArrayUtils.toArray(lst);
        logger.info("Current data row", data);
        /* Check person is exist */
        select =
            new SqlSelect().addField(TBL_PERSONS,
                sys.getIdName(TBL_PERSONS), "personId")
                .addFrom(TBL_PERSONS)
                .setWhere(
                    SqlUtils.and(
                        SqlUtils.equals(TBL_PERSONS, COL_FIRST_NAME, data[2]),
                        SqlUtils.equals(TBL_PERSONS, COL_LAST_NAME, data[3])));

        logger.info("do query: ", select.getQuery());

        SimpleRowSet person = qs.getData(select);

        if (person.isEmpty()) {
          /* Person not exists */
          SqlInsert insert = new SqlInsert(TBL_PERSONS)
              .addFields(COL_FIRST_NAME, COL_LAST_NAME, "DateOfBirth")
              .addValues(data[2], data[3], TimeUtils.parseDate(data[7]));

          logger.info("do query: ", insert.getQuery());

          long personId = qs.insertData(insert);
          long contactid = -1;

          insert = new SqlInsert("Contacts")
              .addFields("Address")
              .addValues(data[8]);

          logger.info("do query: ", insert.getQuery());

          contactid = qs.insertData(insert);

          /* adding contact id to person */
          SqlUpdate update = new SqlUpdate("Persons")
              .addConstant("Contact", contactid)
              .setWhere(SqlUtils.equals("Persons", sys.getIdName("Persons"), personId));

          logger.info("do query: ", update.getQuery());

          qs.updateData(update);

          /* check department */
          select =
              new SqlSelect().addField("CompanyDepartments",
                  sys.getIdName("CompanyDepartments"), "depId").addFrom("CompanyDepartments")
                  .setWhere(
                      SqlUtils.and(SqlUtils.equals("CompanyDepartments", "Company", osamaCompanyId)
                          , SqlUtils.equals("CompanyDepartments", "Name", data[6])));

          logger.info("do query: ", select.getQuery());

          SimpleRowSet department = qs.getData(select);
          long departmentId = -1;

          if (department.isEmpty()) {
            /* department not exists */
            insert = new SqlInsert("CompanyDepartments").addFields("Company", "Name")
                .addValues(osamaCompanyId, data[6]);

            logger.info("do query: ", insert.getQuery());

            departmentId = qs.insertData(insert);

          } else {
            departmentId = department.getLong(0, 0);
          }

          /* checking positions */

          select =
              new SqlSelect().addField("Positions",
                  sys.getIdName("Positions"), "posId").addFrom("Positions")
                  .setWhere(
                      SqlUtils.equals("Positions", "Name", data[4])
                  );

          logger.info("do query: ", select.getQuery());

          SimpleRowSet position = qs.getData(select);
          long positionId = -1;

          if (position.isEmpty()) {
            /* position not found */
            insert = new SqlInsert("Positions").addFields("Name")
                .addValues(data[4]);

            logger.info("do query: ", insert.getQuery());

            positionId = qs.insertData(insert);

          } else {
            positionId = position.getLong(0, 0);
          }

          /* inserting company person contact */
          long cpcontId = -1;

          /* inserting company person */
          if (!BeeUtils.isEmpty(data[10]) || !BeeUtils.isEmpty(data[11])) {
            long emailId = -1;

            if (!BeeUtils.isEmpty(data[10])) {
              select =
                  new SqlSelect().addField(TBL_EMAILS,
                      sys.getIdName(TBL_EMAILS), "emailId")
                      .addFrom(TBL_EMAILS)
                      .setWhere(SqlUtils.equals(TBL_EMAILS, COL_EMAIL, data[10]));
              logger.info("do query: ", select.getQuery());

              SimpleRowSet email = qs.getData(select);

              if (email.isEmpty()) {
                insert = new SqlInsert(TBL_EMAILS)
                    .addFields(COL_EMAIL)
                    .addValues(data[10]);

                logger.info("do query: ", insert.getQuery());

                emailId = qs.insertData(insert);
              }
            }

            insert = new SqlInsert(TBL_CONTACTS)
                .addFields(COL_PHONE, COL_EMAIL)
                .addValues(data[11], emailId > 0 ? emailId : null);

            logger.info("do query: ", insert.getQuery());

            cpcontId = qs.insertData(insert);

          }

          insert = new SqlInsert("CompanyPersons")
              .addFields("Company", "Department", "Person", "TabNo", "AccountingCode",
                  "DateOfEmployment", "Position", "Contact")
              .addValues(osamaCompanyId, departmentId, personId, data[0],
                  BeeUtils.isEmpty(data[1]) ? null : data[1],
                  TimeUtils.parseDate(data[5]), positionId, cpcontId > 0 ? cpcontId : null);

          logger.info("do query: ", insert.getQuery());

          qs.insertData(insert);

        } else {
          logger.warning("record", data, "allready exist!");
        }

      }
      in.close();
    } catch (Exception e) {
      logger.severe(e);
      return ResponseObject.error(e);
    }

    return ResponseObject.info("Data imported");
  }

  @Deprecated
  private ResponseObject importOsamaPrekSist(RequestInfo reqInfo) {
    String fileId =
        reqInfo.getContent();

    SqlSelect select = new SqlSelect().addField("Files", "Repository", "Path").addFrom("Files")
        .setWhere(SqlUtils.equals("Files", sys.getIdName("Files"), Long.parseLong(fileId)));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet fd = qs.getData(select);

    String path = fd.getValue(0, fd.getColumnIndex("Path"));

    select = new SqlSelect()
        .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), "oID")
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.contains(TBL_COMPANIES, COL_COMPANY_NAME, "Osama"));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet osamaCompany = qs.getData(select);

    if (osamaCompany.isEmpty()) {
      return ResponseObject.error("Osama company not found");
    }

    long osamaCompanyId = osamaCompany.getLong(0, 0);

    try {
      FileInputStream fstream = new FileInputStream(path);

      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new
          InputStreamReader(in));
      String strLine;

      while ((strLine = br.readLine()) != null) {
        Splitter splitter = Splitter.on(';');
        List<String> lst = Lists.newArrayList(splitter.split(strLine));

        String[] data = ArrayUtils.toArray(lst);
        logger.info("Current data row", data);

        select =
            new SqlSelect()
                .addField(TBL_COMPANIES, sys.getIdName(TBL_COMPANIES), "companyId")
                .addFrom(TBL_COMPANIES)
                .setWhere(SqlUtils.equals(TBL_COMPANIES, COL_COMPANY_NAME, data[1]));

        logger.info("do query: ", select.getQuery());

        SimpleRowSet company = qs.getData(select);
        long companyId = -1;

        if (company.isEmpty()) {
          SqlInsert insert =
              new SqlInsert(TBL_COMPANIES)
                  .addFields(COL_COMPANY_NAME, COL_COMPANY_EXCHANGE_CODE)
                  .addValues(data[1], data[0]);

          logger.info("do query: ", insert.getQuery());

          companyId = qs.insertData(insert);

        } else {
          companyId = company.getLong(0, 0);
        }

        long userId = -1;

        if (!BeeUtils.isEmpty(data[2])) {
          select =
              new SqlSelect()
                  .addField(TBL_COMPANY_PERSONS,
                      sys.getIdName(TBL_COMPANY_PERSONS), "CompanyPersonId")
                  .addField(TBL_COMPANY_PERSONS, COL_PERSON, "PersonID")
                  .addField(TBL_PERSONS, COL_FIRST_NAME, COL_FIRST_NAME)
                  .addField(TBL_PERSONS, COL_LAST_NAME, COL_LAST_NAME)
                  .addFrom(TBL_COMPANY_PERSONS)
                  .addFromLeft(TBL_PERSONS,
                      sys.joinTables(TBL_PERSONS, TBL_COMPANY_PERSONS, COL_PERSON))
                  .setWhere(
                      SqlUtils.and(SqlUtils.equals(TBL_COMPANY_PERSONS,
                          COL_COMPANY, osamaCompanyId),
                          SqlUtils.equals(TBL_COMPANY_PERSONS, "AccountingCode", data[2])));

          logger.info("do query: ", select.getQuery());

          SimpleRowSet companyPerson = qs.getData(select);

          if (!companyPerson.isEmpty()) {
            long companyPersonId =
                companyPerson.getLong(0, companyPerson.getColumnIndex("CompanyPersonId"));

            select =
                new SqlSelect()
                    .addField(TBL_USERS, sys.getIdName(TBL_USERS), "userId")
                    .addFrom(TBL_USERS)
                    .setWhere(SqlUtils.equals(TBL_USERS, COL_COMPANY_PERSON, companyPersonId));

            logger.info("do query: ", select.getQuery());

            SimpleRowSet user = qs.getData(select);

            if (user.isEmpty()) {
              String userLogin = (companyPerson.getValue(0, COL_FIRST_NAME)
                  + companyPerson.getValue(0, COL_LAST_NAME)).toLowerCase();

              SqlInsert insert = new SqlInsert(TBL_USERS)
                  .addFields(COL_LOGIN, COL_COMPANY_PERSON)
                  .addValues(userLogin, companyPersonId);

              logger.info("do query: ", insert.getQuery());

              userId = qs.insertData(insert);

            } else {
              userId = user.getLong(0, 0);
            }
          }

        }

        if (userId > 0 && companyId > 0) {
          SqlInsert insert = new SqlInsert(TBL_COMPANY_USERS)
              .addFields(COL_COMPANY, COL_USER)
              .addValues(companyId, userId);

          logger.info("do query: ", insert.getQuery());
          try {
            qs.insertData(insert);
          } catch (Exception sqlerr) {
            logger.warning("record exists");
          }
        } else {
          logger.warning("No user or company found (created)");
        }

      }
      in.close();
    } catch (Exception e) {
      logger.severe(e);
      return ResponseObject.error(e);
    }

    return ResponseObject.info("Data imported");
  }

  @Deprecated
  private ResponseObject importOsamaTiekejai(RequestInfo reqInfo) {
    String fileId = reqInfo.getContent();

    SqlSelect select = new SqlSelect().addField("Files", "Repository", "Path")
        .addFrom("Files")
        .setWhere(SqlUtils.equals("Files", sys.getIdName("Files"), Long.parseLong(fileId)));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet fd = qs.getData(select);

    String path = fd.getValue(0, fd.getColumnIndex("Path"));

    select = new SqlSelect().addField(TBL_COMPANIES,
        sys.getIdName(TBL_COMPANIES), "oID")
        .addFrom(TBL_COMPANIES)
        .setWhere(SqlUtils.contains(TBL_COMPANIES, COL_COMPANY_NAME, "Osama"));

    logger.info("do query: ", select.getQuery());

    SimpleRowSet osamaCompany = qs.getData(select);

    if (osamaCompany.isEmpty()) {
      return ResponseObject.error("Osama company not found");
    }

    long osamaCompanyId = osamaCompany.getLong(0, 0);

    try {

      FileInputStream fstream = new FileInputStream(path);

      DataInputStream in = new DataInputStream(fstream);
      BufferedReader br = new BufferedReader(new InputStreamReader(in));
      String strLine;

      while ((strLine = br.readLine()) != null) {
        Splitter splitter = Splitter.on(';');
        List<String> lst = Lists.newArrayList(splitter.split(strLine));

        String[] data = ArrayUtils.toArray(lst);

        logger.info("Current data row", data);

        SqlSelect check = new SqlSelect().addAllFields(TBL_COMPANIES)
            .addFrom(TBL_COMPANIES)
            .setWhere(SqlUtils.and(
                SqlUtils.equals(TBL_COMPANIES, COL_COMPANY_CODE, data[0]),
                SqlUtils.equals(TBL_COMPANIES, COL_COMPANY_NAME, data[1])
                ));

        logger.info("do sql", check.getQuery());

        if (qs.getData(check).isEmpty()) {
          SqlInsert insert = null;
          if (!BeeUtils.isEmpty(data[0])) {
            insert = new SqlInsert(TBL_COMPANIES)
                .addFields(COL_COMPANY_CODE, COL_COMPANY_NAME)
                .addValues(data[0], data[1]);
          } else {
            insert = new SqlInsert(TBL_COMPANIES)
                .addFields(COL_COMPANY_NAME)
                .addValues(data[1]);
          }

          logger.info("do sql", insert.getQuery());

          long companyId = qs.insertData(insert);

          if (!BeeUtils.isEmpty(data[2])) {
            insert = new SqlInsert(TBL_CONTACTS)
                .addFields(COL_ADDRESS)
                .addValues(data[2]);

            logger.info("do sql", insert.getQuery());

            long contactId = qs.insertData(insert);

            SqlUpdate update = new SqlUpdate(TBL_COMPANIES)
                .addConstant(COL_CONTACT, contactId)
                .setWhere(SqlUtils.equals(TBL_COMPANIES,
                    sys.getIdName(TBL_COMPANIES), companyId));

            logger.info("do sql", update.getQuery());

            qs.updateData(update);
          }

          select = new SqlSelect().addField(TBL_PERSONS,
              sys.getIdName(TBL_PERSONS), "personId")
              .addFrom(TBL_PERSONS)
              .setWhere(
                  SqlUtils.and(
                      SqlUtils.equals(TBL_PERSONS, COL_FIRST_NAME, data[4]),
                      SqlUtils.equals(TBL_PERSONS, COL_LAST_NAME, data[5])));

          logger.info("do sql", select.getQuery());

          SimpleRowSet person = qs.getData(select);
          long personId = -1;
          long userId = -1;

          if (person.isEmpty()) {
            insert = new SqlInsert(TBL_PERSONS)
                .addFields(COL_FIRST_NAME, COL_LAST_NAME)
                .addValues(data[4], data[5]);

            logger.info("do sql", insert.getQuery());

            personId = qs.insertData(insert);

            insert = new SqlInsert(TBL_COMPANY_PERSONS)
                .addFields(COL_COMPANY, COL_PERSON)
                .addValues(osamaCompanyId, personId);

            logger.info("do sql", insert.getQuery());

            long compPersonId = qs.insertData(insert);

            insert = new SqlInsert(TBL_USERS)
                .addFields(COL_LOGIN, COL_COMPANY_PERSON)
                .addValues((data[4] + data[5]).toLowerCase(), compPersonId);

            logger.info("do sql", insert.getQuery());

            userId = qs.insertData(insert);

          } else {
            select = new SqlSelect().addField(TBL_COMPANY_PERSONS,
                sys.getIdName(TBL_COMPANY_PERSONS), "compPersonId")
                .addFrom(TBL_COMPANY_PERSONS)
                .setWhere(SqlUtils.and(
                    SqlUtils.equals(TBL_COMPANY_PERSONS, COL_COMPANY, osamaCompanyId),
                    SqlUtils.equals(TBL_COMPANY_PERSONS, COL_PERSON, person.getLong(0, 0))));

            logger.info("do sql", select.getQuery());

            SimpleRowSet compPerson = qs.getData(select);

            select = new SqlSelect().addField(TBL_USERS,
                sys.getIdName(TBL_USERS), "userId")
                .addFrom(TBL_USERS)
                .setWhere(SqlUtils.equals(TBL_USERS, COL_COMPANY_PERSON, compPerson.getLong(0, 0)));

            logger.info("do sql", select.getQuery());

            SimpleRowSet user = qs.getData(select);

            if (user.isEmpty()) {

              insert = new SqlInsert(TBL_USERS)
                  .addFields(COL_LOGIN, COL_COMPANY_PERSON)
                  .addValues((data[4] + data[5]).toLowerCase(), compPerson.getLong(0, 0));

              logger.info("do sql", insert.getQuery());

              userId = qs.insertData(insert);
            } else {
              userId = user.getLong(0, 0);
            }
          }

          insert = new SqlInsert("CompanyUsers")
              .addFields(COL_COMPANY, COL_USER)
              .addValues(companyId, userId);

          logger.info("do sql", insert.getQuery());

          userId = qs.insertData(insert);

        } else {
          logger.warning("record", data, "allready exist!");
        }

      }
      in.close();
    } catch (Exception e) {
      logger.severe(e);
    }

    return ResponseObject.info("Data imported");
  }

  private ResponseObject insertRow(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), true);
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
        String progressId = tbls.isEmpty() ? reqInfo.getParameter(Service.VAR_PROGRESS) : null;
        List<Property> resp = sys.checkTables(tbls, progressId);

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
      RightsState state = EnumUtils.getEnumByName(RightsState.class, arr[1]);
      String tbl = arr[2];
      long id = BeeUtils.toLong(arr[3]);
      long[] bits = null;

      if (arr.length > 4) {
        String[] rArr = arr[4].split(" ");
        bits = new long[rArr.length];

        for (int i = 0; i < rArr.length; i++) {
          bits[i] = BeeUtils.toLong(rArr[i]);
        }
      }
      deb.setState(tbl, state, id, bits);
      response.addInfo("Toggle OK");

    } else if (BeeUtils.startsSame(cmd, "schema")) {
      String schema = cmd.substring("schema".length()).trim();

      response = buildDbSchema(Splitter.onPattern("[ ,]").trimResults().omitEmptyStrings()
          .split(schema));

    } else if (BeeUtils.same(cmd, "tecdoc")) {
      tcd.suckTecdoc();
      response = ResponseObject.info("TecDoc SUCKS NOW...");

    } else if (BeeUtils.same(cmd, "motonet")) {
      tcd.suckMotonet();
      response = ResponseObject.info("Motonet...");

    } else if (BeeUtils.same(cmd, "butent")) {
      tcd.suckButent();
      response = ResponseObject.info("Butent...");

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
      return ResponseObject.parameterNotFound(Service.VAR_VIEW_NAME);
    }

    String where = reqInfo.getParameter(Service.VAR_VIEW_WHERE);
    if (BeeUtils.isEmpty(where)) {
      return ResponseObject.parameterNotFound(Service.VAR_VIEW_WHERE);
    }

    String column = reqInfo.getParameter(Service.VAR_COLUMN);
    if (BeeUtils.isEmpty(column)) {
      return ResponseObject.parameterNotFound(Service.VAR_COLUMN);
    }

    String value = reqInfo.getParameter(Service.VAR_VALUE);
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.parameterNotFound(Service.VAR_VALUE);
    }

    BeeView view = sys.getView(viewName);
    if (view.isReadOnly()) {
      return ResponseObject.error("View", BeeUtils.bracket(view.getName()), "is read only.");
    }

    String tblName = view.getSourceName();
    Filter filter = Filter.restore(where);

    return qs.updateDataWithResponse(new SqlUpdate(tblName)
        .setWhere(view.getCondition(filter, sys.getViewFinder()))
        .addConstant(column, Value.restore(value)));
  }

  private ResponseObject updateAutocomplete(RequestInfo reqInfo) {
    String key = BeeUtils.trim(reqInfo.getParameter(COL_AUTOCOMPLETE_KEY));
    if (BeeUtils.isEmpty(key)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_AUTOCOMPLETE_KEY);
    }

    String value = BeeUtils.trim(reqInfo.getParameter(COL_AUTOCOMPLETE_VALUE));
    if (BeeUtils.isEmpty(value)) {
      return ResponseObject.parameterNotFound(reqInfo.getService(), COL_AUTOCOMPLETE_VALUE);
    }

    Long userId = usr.getCurrentUserId();
    if (userId == null) {
      return ResponseObject.warning(reqInfo.getService(), key, value, "user not available");
    }

    Long id = qs.getId(TBL_AUTOCOMPLETE, COL_AUTOCOMPLETE_USER, userId, COL_AUTOCOMPLETE_KEY, key,
        COL_AUTOCOMPLETE_VALUE, value);

    if (DataUtils.isId(id)) {
      SqlUpdate update = new SqlUpdate(TBL_AUTOCOMPLETE)
          .addConstant(COL_AUTOCOMPLETE_VALUE, value)
          .setWhere(sys.idEquals(TBL_AUTOCOMPLETE, id));

      return qs.updateDataWithResponse(update);

    } else {
      SqlInsert insert = new SqlInsert(TBL_AUTOCOMPLETE)
          .addConstant(COL_AUTOCOMPLETE_USER, userId)
          .addConstant(COL_AUTOCOMPLETE_KEY, key)
          .addConstant(COL_AUTOCOMPLETE_VALUE, value);

      return qs.insertDataWithResponse(insert);
    }
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

  private ResponseObject updateRelatedValues(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    if (BeeUtils.isEmpty(viewName)) {
      return ResponseObject.parameterNotFound(Service.VAR_VIEW_NAME);
    }

    Long parentId = BeeUtils.toLongOrNull(reqInfo.getParameter(Service.VAR_VIEW_ROW_ID));
    if (!DataUtils.isId(parentId)) {
      return ResponseObject.parameterNotFound(Service.VAR_VIEW_ROW_ID);
    }

    String serialized = reqInfo.getParameter(Service.VAR_CHILDREN);
    if (BeeUtils.isEmpty(serialized)) {
      return ResponseObject.parameterNotFound(Service.VAR_CHILDREN);
    }

    Collection<RowChildren> children = Lists.newArrayList();

    String[] arr = Codec.beeDeserializeCollection(serialized);
    if (!ArrayUtils.isEmpty(arr)) {
      for (String s : arr) {
        children.add(RowChildren.restore(s));
      }
    }

    if (children.isEmpty()) {
      return ResponseObject.error("cannot restore children");
    }

    ResponseObject response = new ResponseObject();
    deb.commitChildren(parentId, children, response);

    if (!response.hasErrors()) {
      BeeRowSet rowSet = qs.getViewData(viewName, ComparisonFilter.compareId(parentId));

      if (DataUtils.isEmpty(rowSet)) {
        response.addError("could not get parent row:", viewName, parentId);
      } else {
        response.setResponse(rowSet.getRow(0));
      }
    }

    return response;
  }

  private ResponseObject updateRow(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), true);
  }

}
