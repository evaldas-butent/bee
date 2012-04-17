package com.butent.bee.server.ui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.client.ui.DsnService;
import com.butent.bee.client.ui.StateService;
import com.butent.bee.server.Config;
import com.butent.bee.server.DataSourceBean;
import com.butent.bee.server.data.BeeTable.BeeField;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.DataEditorBean;
import com.butent.bee.server.data.IdGeneratorBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.data.SystemBean.SysObject;
import com.butent.bee.server.data.UserServiceBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.ExtensionFilter;
import com.butent.bee.server.io.FileNameUtils;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.sql.SqlConstants.SqlDataType;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.ui.XmlSqlDesigner.DataType;
import com.butent.bee.server.ui.XmlSqlDesigner.DataTypeGroup;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.XmlState;
import com.butent.bee.shared.data.XmlTable;
import com.butent.bee.shared.data.XmlTable.XmlField;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.ui.DecoratorConstants;
import com.butent.bee.shared.ui.UiComponent;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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

  private static Logger logger = Logger.getLogger(UiServiceBean.class.getName());

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
    } else if (BeeUtils.same(svc, Service.GET_DECORATORS)) {
      response = getDecorators();

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
    } else if (BeeUtils.same(svc, Service.UPDATE_CELL)) {
      response = updateCell(reqInfo);
    } else if (BeeUtils.same(svc, Service.UPDATE_ROW)) {
      response = updateRow(reqInfo);
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

    } else if (BeeUtils.same(svc, StateService.SVC_GET_STATES)) {
      response = getStates();
    } else if (BeeUtils.same(svc, StateService.SVC_SAVE_STATES)) {
      response = saveStates(reqInfo.getContent());

    } else {
      String msg = BeeUtils.concat(1, "data service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  public ResponseObject importForm(String formName, String design) {
    if (BeeUtils.isEmpty(formName)) {
      return ResponseObject.error("form name not specified");
    }
    Document srcDoc = XmlUtils.fromString(design);
    if (srcDoc == null) {
      return ResponseObject.error("error parsing design xml");
    }

    NodeList nodes = srcDoc.getElementsByTagNameNS("*", "control");
    if (nodes == null || nodes.getLength() <= 0) {
      return ResponseObject.error("no controls found in design xml");
    }

    Document dstDoc = XmlUtils.createDocument();
    Element formElement = dstDoc.createElement("Form");
    formElement.setAttribute("xmlns", "http://www.butent.com/bee");
    formElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    formElement.setAttribute("xsi:schemaLocation",
        "http://www.butent.com/bee ../../schemas/form.xsd");
    dstDoc.appendChild(formElement);

    Element rootElement = dstDoc.createElement("AbsolutePanel");
    formElement.appendChild(rootElement);

    BeeView view = null;

    int minLeft = -1;
    int minTop = -1;

    for (int i = 0; i < nodes.getLength(); i++) {
      Element control = (Element) nodes.item(i);
      String srcType = control.getAttribute("controlTypeID");
      if (BeeUtils.context("TitleWindow", srcType)) {
        continue;
      }

      int left = BeeUtils.toInt(control.getAttribute("x"));
      int top = BeeUtils.toInt(control.getAttribute("y"));

      if (left >= 0) {
        minLeft = (minLeft < 0) ? left : Math.min(minLeft, left);
      }
      if (top >= 0) {
        minTop = (minTop < 0) ? top : Math.min(minTop, top);
      }
    }

    int shiftLeft = minLeft - 10;
    int shiftTop = minTop - 10;

    List<Element> layers = Lists.newArrayList();

    for (int i = 0; i < nodes.getLength(); i++) {
      Element control = (Element) nodes.item(i);

      String srcType = control.getAttribute("controlTypeID");
      if (BeeUtils.isEmpty(srcType)) {
        continue;
      }
      String dstType = BeeUtils.getSuffix(srcType, "::");
      if (BeeUtils.isEmpty(dstType)) {
        continue;
      }

      String srcLeft = control.getAttribute("x");
      String srcTop = control.getAttribute("y");
      String srcWidth = control.getAttribute("w");
      String srcHeight = control.getAttribute("h");

      String text = null;
      Element propElement = XmlUtils.getFirstChildElement(control, "controlProperties");
      if (propElement != null) {
        Element textElement = XmlUtils.getFirstChildElement(propElement, "text");
        if (textElement != null) {
          text = XmlUtils.getTextQuietly(textElement);
        }
      }
      if (BeeUtils.isEmpty(text)) {
        continue;
      }

      text = BeeUtils.replace(text, "%20", " ");
      text = BeeUtils.replace(text, "%3A", ":");
      text = BeeUtils.replace(text, "%2C", ",");

      int p = text.indexOf("%u");
      while (p >= 0) {
        text = text.substring(0, p) + (char) Integer.parseInt(text.substring(p + 2, p + 6), 16)
            + text.substring(p + 6);
        p = text.indexOf("%u");
      }

      String source;
      String label;
      if (BeeUtils.context(":", text)) {
        source = BeeUtils.getPrefix(text, ":");
        label = BeeUtils.getSuffix(text, ":");
      } else if (BeeUtils.inListSame(dstType, "Label", "Button")) {
        source = null;
        label = text;
      } else {
        source = text;
        label = null;
      }

      if (BeeUtils.same(dstType, "TitleWindow")) {
        if (!BeeUtils.isEmpty(source)) {
          formElement.setAttribute("name", source);
          if (sys.isView(source)) {
            formElement.setAttribute("viewName", source);
            view = sys.getView(source);
          } else {
            LogUtils.warning(logger, "import form", formName,
                "view name", source, "not recognized");
          }
        }
        if (!BeeUtils.isEmpty(label)) {
          formElement.setAttribute("caption", label);
        }
        continue;
      }

      if (BeeUtils.same(dstType, "TextInput")) {
        if (BeeUtils.context(",", label)) {
          dstType = "ListBox";
        } else {
          SqlDataType type = null;
          if (view != null && view.hasColumn(source)) {
            type = view.getColumnType(source);
          }
          if (type != null) {
            switch (type) {
              case DATE:
                dstType = "InputDate";
                break;
              case DATETIME:
                dstType = "InputDateTime";
                break;
              case DECIMAL:
                dstType = "InputDecimal";
                break;
              case DOUBLE:
                dstType = "InputDouble";
                break;
              case INTEGER:
                dstType = "InputInteger";
                break;
              case LONG:
                dstType = "InputLong";
                break;
              default:
                dstType = "InputText";
            }
          } else {
            dstType = "InputText";
          }
        }

      } else if (BeeUtils.same(dstType, "TextArea")) {
        dstType = "InputArea";

      } else if (BeeUtils.same(dstType, "RadioButton")) {
        dstType = "Radio";
        srcWidth = null;
        srcHeight = null;

      } else if (BeeUtils.same(dstType, "ComboBox")) {
        if (text.contains(".")) {
          dstType = "DataSelector";
        } else {
          dstType = "ListBox";
          srcWidth = null;
          srcHeight = null;
        }

      } else if (BeeUtils.same(dstType, "DataGrid")) {
        dstType = "ChildGrid";
      }

      Element layerElement = dstDoc.createElement("layer");

      if (BeeUtils.isDigit(srcLeft)) {
        if (shiftLeft > 0) {
          layerElement.setAttribute("left", BeeUtils.toString(BeeUtils.toInt(srcLeft) - shiftLeft));
        } else {
          layerElement.setAttribute("left", srcLeft);
        }
      }
      if (BeeUtils.isDigit(srcTop)) {
        if (shiftTop > 0) {
          layerElement.setAttribute("top", BeeUtils.toString(BeeUtils.toInt(srcTop) - shiftTop));
        } else {
          layerElement.setAttribute("top", srcTop);
        }
      }

      if (BeeUtils.isDigit(srcWidth)) {
        layerElement.setAttribute("width", srcWidth);
      }
      if (BeeUtils.isDigit(srcHeight)) {
        layerElement.setAttribute("height", srcHeight);
      }

      layers.add(layerElement);

      Element widgetElement = dstDoc.createElement(dstType.trim());
      boolean isColumn = false;

      if (!BeeUtils.isEmpty(source)) {
        if (BeeUtils.same(dstType, "DataSelector") && view != null) {
          String relSource = BeeUtils.getPrefix(source, '.');
          String relColumn = BeeUtils.getSuffix(source, '.');

          widgetElement.setAttribute("source", relSource + relColumn);
          widgetElement.setAttribute("relSource", relSource);

          String relView = null;
          if (view.hasColumn(relSource)) {
            relView =
                sys.getRelation(view.getColumnTable(relSource), view.getColumnField(relSource));
            if (!BeeUtils.isEmpty(relView)) {
              widgetElement.setAttribute("relView", relView);
              isColumn = true;
            } else {
              LogUtils.warning(logger, "import form", formName, "widget", dstType,
                  "source", source, "relSource", relSource, "has no relation");
            }
          } else {
            LogUtils.warning(logger, "import form", formName, "widget", dstType,
                "source", source, "relSource", relSource, "not a view column");
          }

          widgetElement.setAttribute("relColumn", relColumn);
          if (sys.isView(relView) && !sys.getView(relView).hasColumn(relColumn)) {
            LogUtils.warning(logger, "import form", formName, "widget", dstType,
                "source", source, "relView", relView, "relColumn", relColumn, "not a view column");
          }

        } else if (BeeUtils.same(dstType, "ChildGrid") && view != null) {
          String relColumn = view.getSourceIdName();
          widgetElement.setAttribute("name", source);
          widgetElement.setAttribute("relColumn", relColumn);

          if (ui.isGrid(source)) {
            if (sys.getView(ui.getGrid(source).getViewName()).hasColumn(relColumn)) {
              isColumn = true;
            } else {
              LogUtils.warning(logger, "import form", formName, "widget", dstType,
                  "grid", source, "relColumn", relColumn, "not a view column");
            }
          } else if (!sys.isView(source)) {
            LogUtils.warning(logger, "import form", formName, "widget", dstType,
                "source", source, "not a view");
          } else if (!sys.getView(source).hasColumn(relColumn)) {
            LogUtils.warning(logger, "import form", formName, "widget", dstType,
                "view", source, "relColumn", relColumn, "not a view column");
          } else {
            isColumn = true;
          }

        } else {
          widgetElement.setAttribute("source", source);
          if (view != null) {
            if (!view.hasColumn(source)) {
              LogUtils.warning(logger, "import form", formName, "widget", dstType,
                  "source", source, "not a view column");
            } else {
              isColumn = true;
            }
          }
        }
      }

      if (isColumn && BeeUtils.same(dstType, "InputDecimal")) {
        int scale = sys.getScale(view.getColumnTable(source), view.getColumnField(source));
        if (scale > 0) {
          widgetElement.setAttribute("scale", BeeUtils.toString(scale));
        }
      }

      if (!BeeUtils.isEmpty(label)) {
        if (BeeUtils.same(dstType, "ListBox")) {
          for (String item : BeeUtils.split(label, ",")) {
            Element itemElement = dstDoc.createElement("item");
            itemElement.setTextContent(item);
            widgetElement.appendChild(itemElement);
          }

        } else if (BeeUtils.same(dstType, "Radio")) {
          for (String item : BeeUtils.split(label, ",")) {
            Element itemElement = dstDoc.createElement("option");
            itemElement.setTextContent(item);
            widgetElement.appendChild(itemElement);
          }

        } else {
          widgetElement.setAttribute("html", label);
        }
      }

      layerElement.appendChild(widgetElement);
    }

    if (layers.size() > 1) {
      Collections.sort(layers, new Comparator<Element>() {
        public int compare(Element o1, Element o2) {
          if (o1 == null) {
            return (o2 == null) ? BeeConst.COMPARE_EQUAL : BeeConst.COMPARE_LESS;
          }
          if (o2 == null) {
            return BeeConst.COMPARE_MORE;
          }

          int t1 = BeeUtils.toInt(o1.getAttribute("top"));
          int t2 = BeeUtils.toInt(o2.getAttribute("top"));

          int res = (Math.abs(t1 - t2) < 5) ? BeeConst.COMPARE_EQUAL : BeeUtils.compare(t1, t2);
          if (res == BeeConst.COMPARE_EQUAL) {
            res = BeeUtils.compare(BeeUtils.toInt(o1.getAttribute("left")),
                BeeUtils.toInt(o2.getAttribute("left")));
          }
          return res;
        }
      });
    }

    for (Element layer : layers) {
      rootElement.appendChild(layer);
    }

    String result = XmlUtils.toString(dstDoc, true);
    if (BeeUtils.isEmpty(result)) {
      return ResponseObject.error("xml problem");
    }

    String path = new File(new File(Config.USER_DIR, "forms"),
        FileNameUtils.defaultExtension(formName, XmlUtils.defaultXmlExtension)).getPath();
    LogUtils.infoNow(logger, "saving", path);
    FileUtils.saveToFile(result, path);

    return ResponseObject.response(result);
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
          if (BeeUtils.same(field.getRelation(), root)) {
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
      typeGroup.label = BeeUtils.concat(1, "SQL", extMode ? "extended" : "", "types");
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
          if (!BeeUtils.isEmpty(xmlField.relation)) {
            xmlField.relationField = sys.getIdName(xmlField.relation);
          }
        }
        designer.tables.add(xmlTable);
      }
    }
    return ResponseObject.response(new BeeResource(null, XmlUtils.marshal(designer, null)));
  }

  private ResponseObject deleteRows(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    Assert.notEmpty(viewName);
    String[] entries = Codec.beeDeserializeCollection(reqInfo.getParameter(Service.VAR_VIEW_ROWS));
    Assert.notEmpty(entries);
    RowInfo[] rows = new RowInfo[entries.length];

    for (int i = 0; i < entries.length; i++) {
      rows[i] = RowInfo.restore(entries[i]);
    }
    return deb.deleteRows(viewName, rows);
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

    UiComponent form = ui.getUiForm(fName);

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
      response = deb.generateData(tableName, rowCount);
    }
    return response;
  }

  private ResponseObject getDataInfo(RequestInfo reqInfo) {
    String viewName = reqInfo.getParameter(Service.VAR_VIEW_NAME);
    if (BeeUtils.isEmpty(viewName)) {
      return ResponseObject.response(sys.getDataInfo());
    } else {
      return ResponseObject.response(sys.getDataInfo(viewName));
    }
  }

  private ResponseObject getDecorators() {
    File dir = new File(Config.WEB_INF_DIR, DecoratorConstants.DIRECTORY);
    List<File> files = FileUtils.findFiles(dir, Lists.newArrayList(FileUtils.INPUT_FILTER,
        new ExtensionFilter(XmlUtils.defaultXmlExtension)));
    if (files.isEmpty()) {
      return ResponseObject.error("getDecorators: no xml found in", dir.getPath());
    }

    Document dstDoc = XmlUtils.createDocument();
    Element dstRoot = dstDoc.createElement(DecoratorConstants.TAG_DECORATORS);
    dstRoot.setAttribute("xmlns", DecoratorConstants.NAMESPACE);
    dstDoc.appendChild(dstRoot);

    for (File file : files) {
      String path = file.getPath();

      Document srcDoc =
          XmlUtils.getXmlResource(path, Config.getSchemaPath(DecoratorConstants.SCHEMA));
      if (srcDoc == null) {
        return ResponseObject.error("getDecorators: cannot load xml:", path);
      }

      List<Element> elements =
          XmlUtils.getElementsByLocalName(srcDoc, DecoratorConstants.TAG_DECORATOR);
      if (elements.isEmpty()) {
        LogUtils.warning(logger, "no decorators found in", path);
      }

      for (Element decorator : elements) {
        dstRoot.appendChild(dstDoc.importNode(decorator, true));
      }
      LogUtils.infoNow(logger, elements.size(), "decorators loaded from", path);
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

  private ResponseObject getStates() {
    List<XmlState> states = Lists.newArrayList();

    for (String stateName : sys.getStateNames()) {
      XmlState xmlState = sys.getXmlState(sys.getState(stateName).getModuleName(), stateName);

      if (xmlState != null) {
        states.add(xmlState);
      }
    }
    return ResponseObject.response(states);
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

    String size = reqInfo.getParameter(Service.VAR_VIEW_SIZE);
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
    String[] cols = new String[0];
    if (!BeeUtils.isEmpty(columns)) {
      cols = columns.split(Service.VIEW_COLUMN_SEPARATOR);
    }
    int cnt = BeeConst.UNDEF;
    if (!BeeUtils.isEmpty(size)) {
      cnt = sys.getViewSize(viewName, filter);
      if (cnt < BeeUtils.toInt(size)) {
        limit = BeeConst.UNDEF;
      }
    }
    BeeRowSet res = sys.getViewData(viewName, filter, order, limit, offset, cols);
    if (cnt >= 0 && res != null) {
      res.setTableProperty(Service.VAR_VIEW_SIZE, BeeUtils.toString(cnt));
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
    return ResponseObject.response(sys.getViewSize(viewName, filter));
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
        rs.addColumn(new BeeColumn(ValueType.TEXT, colName, NameUtils.createUniqueName("col")));
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

  private ResponseObject menuInfo(RequestInfo reqInfo) {
    ResponseObject response = new ResponseObject();
    String mName = reqInfo.getParameter("menu_name");
    String lRoot = reqInfo.getParameter("root_layout");
    String lItem = reqInfo.getParameter("item_layout");

    UiComponent menu = ui.getUiMenu(mName, lRoot, lItem, Config.getPath("menu.xml"));

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
      ui.initGrids();
      response.addInfo("Grids OK");

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
          err = BeeUtils.concat(1, "Unknown table:", w);
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
        String opt = NameUtils.getWord(cmd, 1);
        sys.rebuildTable(tbl, !BeeConst.STRING_MINUS.equals(opt));
        response.addInfo("Rebuild", tbl, opt, "OK");
      } else {
        response.addError("Unknown table:", tbl);
      }

    } else {
      response.addError("Rebuild what?");
    }
    return response;
  }

  private ResponseObject saveDbSchema(String dbSchema) {
    String schemaPath = SysObject.TABLE.getSchemaPath();
    StringBuilder xmlResponse = new StringBuilder();
    XmlSqlDesigner designer = null;
    ResponseObject response = new ResponseObject();
    Map<String, XmlTable> updates = Maps.newHashMap();

    try {
      designer = XmlUtils.unmarshal(XmlSqlDesigner.class, dbSchema, null);
    } catch (BeeRuntimeException e) {
      response.addError(e);
    }
    if (designer == null || BeeUtils.isEmpty(designer.tables)) {
      response.addError("No tables defined");
    } else {
      for (XmlTable xmlTable : designer.tables) {
        String tblName = xmlTable.name;

        if (BeeUtils.isEmpty(xmlTable.idName)) {
          response.addError(BeeUtils.bracket(tblName), "Primary key is missing/invalid");
        } else {
          try {
            String xml = XmlUtils.marshal(xmlTable, schemaPath);
            xmlResponse.append(xml).append("\n");
            XmlTable userTable = sys.loadXmlTable(xml);
            XmlTable configTable =
                sys.getXmlTable(sys.getTable(tblName).getModuleName(), tblName, false);
            XmlTable diffTable = null;

            if (configTable == null) {
              diffTable = userTable;
            } else {
              diffTable = configTable.protect().getChanges(userTable);
            }
            updates.put(BeeUtils.normalize(tblName), diffTable);

          } catch (BeeRuntimeException e) {
            response.addError(BeeUtils.bracket(tblName), e);
          }
        }
      }
    }
    if (!response.hasErrors()) {
      for (String tbl : sys.getTableNames()) {
        String tblName = BeeUtils.normalize(tbl);

        if (!updates.containsKey(tblName)) {
          updates.put(tblName, null);
        }
      }
      for (String tblName : updates.keySet()) {
        String path = new File(Config.USER_DIR, SysObject.TABLE.getPath() + "/"
            + SysObject.TABLE.getFileName(tblName)).getPath();
        XmlTable diffTable = updates.get(tblName);

        if (diffTable == null) {
          if (!FileUtils.deleteFile(path)) {
            response.addError("Can't delete file:", path);
          }
        } else {
          boolean ok = false;
          try {
            ok = FileUtils.saveToFile(XmlUtils.marshal(diffTable, schemaPath), path);
          } catch (BeeRuntimeException e) {
            response.addError(BeeUtils.bracket(tblName), e);
          }
          if (!ok) {
            response.addError("Can't save file:", path);
          }
        }
      }
      sys.initTables();
      response.setResponse(new BeeResource(null, xmlResponse.toString()));
    }
    return response;
  }

  private ResponseObject saveStates(String data) {
    String schemaPath = SysObject.STATE.getSchemaPath();
    ResponseObject response = new ResponseObject();
    Map<String, XmlState> updates = Maps.newHashMap();
    String[] arr = Codec.beeDeserializeCollection(data);

    if (!BeeUtils.isEmpty(arr)) {
      for (String state : arr) {
        XmlState xmlState = XmlState.restore(state);
        String stateName = xmlState.name;

        if (updates.containsKey(BeeUtils.normalize(stateName))) {
          response.addError("Dublicate state name:", BeeUtils.bracket(stateName));
        } else {
          try {
            XmlState userState = sys.loadXmlState(XmlUtils.marshal(xmlState, schemaPath));
            XmlState configState =
                sys.getXmlState(sys.getState(stateName).getModuleName(), stateName, false);
            XmlState diffState = null;

            if (configState == null) {
              diffState = userState;
            } else {
              diffState = configState.protect().getChanges(userState);
            }
            updates.put(BeeUtils.normalize(stateName), diffState);

          } catch (BeeRuntimeException e) {
            response.addError(BeeUtils.bracket(stateName), e);
          }
        }
      }
    }
    if (!response.hasErrors()) {
      for (String tbl : sys.getTableNames()) {
        for (String tblState : sys.getTableStates(tbl)) {
          if (!updates.containsKey(BeeUtils.normalize(tblState))) {
            response.addError("State", BeeUtils.bracket(tblState),
                "is used in table", BeeUtils.bracket(tbl));
          }
        }
      }
    }
    if (!response.hasErrors()) {
      for (String state : sys.getStateNames()) {
        String stateName = BeeUtils.normalize(state);

        if (!updates.containsKey(stateName)) {
          updates.put(stateName, null);
        }
      }
      for (String stateName : updates.keySet()) {
        String path = new File(Config.USER_DIR, SysObject.STATE.getPath() + "/"
            + SysObject.STATE.getFileName(stateName)).getPath();
        XmlState diffState = updates.get(stateName);

        if (diffState == null) {
          if (!FileUtils.deleteFile(path)) {
            response.addError("Can't delete file:", path);
          }
        } else {
          boolean ok = false;
          try {
            ok = FileUtils.saveToFile(XmlUtils.marshal(diffState, schemaPath), path);
          } catch (BeeRuntimeException e) {
            response.addError(BeeUtils.bracket(stateName), e);
          }
          if (!ok) {
            response.addError("Can't save file:", path);
          }
        }
      }
      sys.initStates();
      response.addInfo("States OK");
    }
    return response;
  }

  private ResponseObject switchDsn(String dsn) {
    if (!BeeUtils.isEmpty(dsn)) {
      ig.destroy();
      sys.initDatabase(dsn);
      return ResponseObject.response(dsn);
    }
    return ResponseObject.error("DSN not specified");
  }

  private ResponseObject updateCell(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), false);
  }

  private ResponseObject updateRow(RequestInfo reqInfo) {
    return deb.commitRow(BeeRowSet.restore(reqInfo.getContent()), true);
  }
}
