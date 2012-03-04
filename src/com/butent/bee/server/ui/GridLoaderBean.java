package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.CellType;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;
import com.butent.bee.shared.ui.EditorDescription;
import com.butent.bee.shared.ui.EditorType;
import com.butent.bee.shared.ui.GridComponentDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.StyleDeclaration;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Reads grids structure from xml files.
 */

@Stateless
public class GridLoaderBean {

  private static final String TAG_GRID = "BeeGrid";
  private static final String TAG_COLUMNS = "columns";

  private static final String TAG_CSS = "css";

  private static final String TAG_HEADER = "header";
  private static final String TAG_BODY = "body";
  private static final String TAG_FOOTER = "footer";

  private static final String TAG_ROW_STYLE = "rowStyle";
  private static final String TAG_ROW_MESSAGE = "rowMessage";
  private static final String TAG_ROW_EDITABLE = "rowEditable";
  private static final String TAG_ROW_VALIDATION = "rowValidation";
  private static final String TAG_EDIT_MESSAGE = "editMessage";

  private static final String TAG_STYLE = "style";
  private static final String TAG_HEADER_STYLE = "headerStyle";
  private static final String TAG_BODY_STYLE = "bodyStyle";
  private static final String TAG_FOOTER_STYLE = "footerStyle";
  private static final String TAG_DYN_STYLE = "dynStyle";

  private static final String TAG_VALIDATION = "validation";
  private static final String TAG_EDITABLE = "editable";
  private static final String TAG_CARRY = "carry";

  private static final String TAG_CALC = "calc";

  private static final String TAG_EDITOR = "editor";
  private static final String TAG_ITEM = "item";

  private static final Set<String> WIDGET_TAGS = Sets.newHashSet("north", "south", "west", "east");

  private static final String ATTR_NAME = "name";
  private static final String ATTR_CAPTION = "caption";

  private static final String ATTR_VIEW_NAME = "viewName";
  private static final String ATTR_FILTER = "filter";
  private static final String ATTR_ORDER = "order";

  private static final String ATTR_SHOW_COLUMN_WIDTHS = "showColumnWidths";
  private static final String ATTR_MIN_COLUMN_WIDTH = "minColumnWidth";
  private static final String ATTR_MAX_COLUMN_WIDTH = "maxColumnWidth";

  private static final String ATTR_HAS_HEADERS = "hasHeaders";
  private static final String ATTR_HAS_FOOTERS = "hasFooters";
  private static final String ATTR_FOOTER_EVENTS = "footerEvents";

  private static final String ATTR_CACHING = "caching";
  private static final String ATTR_ASYNC_THRESHOLD = "asyncThreshold";
  private static final String ATTR_PAGING_THRESHOLD = "pagingThreshold";
  private static final String ATTR_SEARCH_THRESHOLD = "searchThreshold";
  private static final String ATTR_INITIAL_ROW_SET_SIZE = "initialRowSetSize";

  private static final String ATTR_READ_ONLY = "readOnly";
  private static final String ATTR_ENABLED_ACTIONS = "enabledActions";
  private static final String ATTR_DISABLED_ACTIONS = "disabledActions";

  private static final String ATTR_NEW_ROW_FORM = "newRowForm";
  private static final String ATTR_NEW_ROW_COLUMNS = "newRowColumns";
  private static final String ATTR_NEW_ROW_CAPTION = "newRowCaption";

  private static final String ATTR_EDIT_FORM = "editForm";
  private static final String ATTR_EDIT_MODE = "editMode";
  private static final String ATTR_EDIT_SAVE = "editSave";
  private static final String ATTR_EDIT_SHOW_ID = "editShowId";
  private static final String ATTR_EDIT_IN_PLACE = "editInPlace";
  private static final String ATTR_EDIT_NEW_ROW = "editNewRow";

  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_MIN_WIDTH = "minWidth";
  private static final String ATTR_MAX_WIDTH = "maxWidth";
  private static final String ATTR_SORTABLE = "sortable";
  private static final String ATTR_VISIBLE = "visible";
  private static final String ATTR_FORMAT = "format";
  private static final String ATTR_HORIZONTAL_ALIGNMENT = "horizontalAlignment";

  private static final String ATTR_HAS_FOOTER = "hasFooter";
  private static final String ATTR_SHOW_WIDTH = "showWidth";

  private static final String ATTR_SOURCE = "source";
  private static final String ATTR_REQUIRED = "required";

  private static final String ATTR_MIN_VALUE = "minValue";
  private static final String ATTR_MAX_VALUE = "maxValue";

  private static final String ATTR_REL_SOURCE = "relSource";
  private static final String ATTR_REL_VIEW = "relView";
  private static final String ATTR_REL_COLUMN = "relColumn";

  private static final String ATTR_TYPE = "type";
  private static final String ATTR_PRECISION = "precision";
  private static final String ATTR_SCALE = "scale";

  private static final String ATTR_SEARCH_BY = "searchBy";
  private static final String ATTR_SORT_BY = "sortBy";

  private static final String ATTR_CELL = "cell";

  private static final String ATTR_ID = "id";

  private static Logger logger = Logger.getLogger(GridLoaderBean.class.getName());

  @EJB
  SystemBean sys;

  public GridDescription getDefaultGrid(BeeView view) {
    Assert.notNull(view);
    String name = view.getName();

    GridDescription gridDescription = new GridDescription(name, name, view.getSourceIdName(),
        view.getSourceVersionName());
    gridDescription.setDefaults();
    if (view.isReadOnly()) {
      gridDescription.setReadOnly(true);
    }

    gridDescription.addColumn(new ColumnDescription(ColType.ID, view.getSourceIdName()));

    Map<String, ColumnDescription> columns = Maps.newLinkedHashMap();
    Set<String> relSources = Sets.newHashSet();

    ColumnDescription columnDescription;
    for (String colName : view.getColumnNames()) {
      String relSource = view.getColumnParent(colName);

      if (BeeUtils.isEmpty(relSource)) {
        columnDescription = new ColumnDescription(ColType.DATA, colName);
      } else {
        columnDescription = new ColumnDescription(ColType.RELATED, colName);
        relSources.add(relSource);

        columnDescription.setRelSource(relSource);
        columnDescription.setRelView(view.getColumnTable(colName));
        columnDescription.setRelColumn(view.getColumnField(colName));
      }
      columnDescription.setSource(colName);

      columns.put(BeeUtils.normalize(colName), columnDescription);
    }

    if (!relSources.isEmpty()) {
      for (String relSource : relSources) {
        columnDescription = columns.get(BeeUtils.normalize(relSource));
        if (columnDescription != null) {
          columnDescription.setReadOnly(true);
        }
      }
    }

    gridDescription.getColumns().addAll(columns.values());

    gridDescription.addColumn(new ColumnDescription(ColType.VERSION, view.getSourceVersionName()));

    for (ColumnDescription cd : gridDescription.getColumns()) {
      cd.setSortable(true);
      cd.setHasFooter(true);
    }
    return gridDescription;
  }

  public GridDescription loadGrid(String resource, String schema) {
    Document doc = XmlUtils.getXmlResource(resource, schema);
    if (doc == null) {
      return null;
    }

    Element gridElement = doc.getDocumentElement();
    if (gridElement == null) {
      return null;
    }
    if (!BeeUtils.same(gridElement.getTagName(), TAG_GRID)) {
      LogUtils.warning(logger, "unrecognized grid element tag name", gridElement.getTagName());
      return null;
    }

    String gridName = gridElement.getAttribute(ATTR_NAME);
    String viewName = gridElement.getAttribute(ATTR_VIEW_NAME);

    if (BeeUtils.isEmpty(gridName)) {
      LogUtils.warning(logger, "Grid attribute", ATTR_NAME, "not found");
      return null;
    }
    
    BeeView view;
    GridDescription grid;
    
    if (BeeUtils.isEmpty(viewName)) {
      view = null;
      grid = new GridDescription(gridName);      
    } else {
      if (!sys.isView(viewName)) {
        LogUtils.warning(logger, "Grid", gridName, "unrecognized view name:", viewName);
        return null;
      }

      view = sys.getView(viewName);
      grid = new GridDescription(gridName, viewName, view.getSourceIdName(),
        view.getSourceVersionName());
    }
    xmlToGrid(gridElement, grid, view);

    NodeList columnGroups = gridElement.getElementsByTagName(TAG_COLUMNS);
    if (XmlUtils.isEmpty(columnGroups)) {
      LogUtils.warning(logger, "Grid", gridName, "tag", TAG_COLUMNS, "not found");
      return null;
    }
    List<Element> columns = Lists.newArrayList();
    for (int i = 0; i < columnGroups.getLength(); i++) {
      columns.addAll(XmlUtils.getChildrenElements(columnGroups.item(i)));
    }
    if (columns.isEmpty()) {
      LogUtils.warning(logger, "Grid", gridName, "has no columns");
      return null;
    }

    for (int i = 0; i < columns.size(); i++) {
      Element columnElement = columns.get(i);

      String colTag = columnElement.getTagName();
      ColType colType = ColType.getColType(colTag);
      String colName = columnElement.getAttribute(ATTR_NAME);

      if (colType == null) {
        LogUtils.warning(logger, "Grid", gridName, "column", i, colName,
            "type", colTag, "not recognized");
        continue;
      }
      if (BeeUtils.isEmpty(colName)) {
        LogUtils.warning(logger, "Grid", gridName, "column", i, colTag, "name not specified");
        continue;
      }
      if (grid.hasColumn(colName)) {
        LogUtils.warning(logger, "Grid", gridName, "column", i, colTag,
            "duplicate column name:", colName);
        continue;
      }

      ColumnDescription column = new ColumnDescription(colType, colName);
      xmlToColumn(columnElement, column);

      if (initColumn(view, column)) {
        grid.addColumn(column);
      }
    }

    if (grid.isEmpty()) {
      LogUtils.warning(logger, "Grid", gridName, "has no columns");
      return null;
    }
    return grid;
  }

  private GridComponentDescription getComponent(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);

    Element element = XmlUtils.getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    }

    StyleDeclaration style = XmlUtils.getStyle(element, GridComponentDescription.TAG_STYLE);
    Map<String, String> attributes = XmlUtils.getAttributes(element);

    if (style == null && (attributes == null || attributes.isEmpty())) {
      return null;
    }
    return new GridComponentDescription(style, attributes);
  }

  private EditorDescription getEditor(Element parent) {
    Assert.notNull(parent);

    Element element = XmlUtils.getFirstChildElement(parent, TAG_EDITOR);
    if (element == null) {
      return null;
    }
    String typeCode = element.getAttribute(ATTR_TYPE);
    if (BeeUtils.isEmpty(typeCode)) {
      return null;
    }
    EditorType editorType = EditorType.getByTypeCode(typeCode);
    if (editorType == null) {
      return null;
    }

    EditorDescription editor = new EditorDescription(editorType);
    editor.setAttributes(XmlUtils.getAttributes(element));

    NodeList itemNodes = element.getElementsByTagName(TAG_ITEM);
    if (itemNodes != null && itemNodes.getLength() > 0) {
      List<String> items = Lists.newArrayList();
      for (int i = 0; i < itemNodes.getLength(); i++) {
        String item = itemNodes.item(i).getTextContent();
        if (!BeeUtils.isEmpty(item)) {
          items.add(item);
        }
      }
      if (!items.isEmpty()) {
        editor.setItems(items);
      }
    }
    return editor;
  }

  private boolean initColumn(BeeView view, ColumnDescription column) {
    Assert.notNull(column);
    if (view == null) {
      return true;
    }

    boolean ok = false;
    String viewName = view.getName();
    String source = column.getSource();

    switch (column.getColType()) {
      case ID:
        column.setSource(view.getSourceIdName());
        ok = true;
        break;

      case VERSION:
        column.setSource(view.getSourceVersionName());
        ok = true;
        break;

      case DATA:
        if (view.hasColumn(source)) {
          ok = true;
        } else {
          LogUtils.warning(logger, viewName, "unrecognized view column:", source);
        }
        break;

      case RELATED:
        if (!view.hasColumn(source)) {
          LogUtils.warning(logger, viewName, "unrecognized view column:", source);
          return ok;
        }
        String relSource = column.getRelSource();
        if (!view.hasColumn(relSource)) {
          LogUtils.warning(logger, viewName, "unrecognized relation column:", relSource);
          return ok;
        }
        String relTable =
            sys.getRelation(view.getColumnTable(relSource), view.getColumnField(relSource));
        if (BeeUtils.isEmpty(relTable)) {
          LogUtils.warning(logger, viewName, "not a relation column:", relSource);
          return ok;
        }
        String relView = column.getRelView();
        if (BeeUtils.isEmpty(relView)) {
          relView = relTable;
          column.setRelView(relView);

        } else if (!sys.isView(relView)) {
          LogUtils.warning(logger, "unrecognized relation view name:", relView);
          return ok;

        } else {
          String table = sys.getView(relView).getSourceName();

          if (!BeeUtils.same(table, relTable)) {
            LogUtils.warning(logger, "relation column and relation view sources doesn't match:",
                relTable, "!=", table);
            return ok;
          }
        }
        String relColumn = column.getRelColumn();
        if (!sys.getView(relView).hasColumn(relColumn)) {
          LogUtils.warning(logger, relView, "unrecognized related column:", relColumn);
          return ok;
        }
        ok = true;
        break;

      case CALCULATED:
        if (column.getCalc() == null) {
          LogUtils.warning(logger, viewName, "column", column.getName(),
              "calculation not specified");
        } else {
          ok = true;
        }
        break;

      case SELECTION:
        ok = true;
        break;
    }

    return ok;
  }

  private void xmlToColumn(Element src, ColumnDescription dst) {
    Assert.notNull(src);
    Assert.notNull(dst);

    Map<String, String> attributes = XmlUtils.getAttributes(src);

    if (!attributes.isEmpty()) {
      for (Map.Entry<String, String> attribute : attributes.entrySet()) {
        String key = attribute.getKey();
        String value = attribute.getValue();
        if (BeeUtils.isEmpty(value)) {
          continue;
        }

        if (BeeUtils.same(key, ATTR_CAPTION)) {
          dst.setCaption(value.trim());
        } else if (BeeUtils.same(key, ATTR_READ_ONLY)) {
          dst.setReadOnly(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_WIDTH)) {
          dst.setWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_MIN_WIDTH)) {
          dst.setMinWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_MAX_WIDTH)) {
          dst.setMaxWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SORTABLE)) {
          dst.setSortable(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_VISIBLE)) {
          dst.setVisible(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_FORMAT)) {
          dst.setFormat(value.trim());
        } else if (BeeUtils.same(key, ATTR_HORIZONTAL_ALIGNMENT)) {
          dst.setHorAlign(value.trim());

        } else if (BeeUtils.same(key, ATTR_HAS_FOOTER)) {
          dst.setHasFooter(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SHOW_WIDTH)) {
          dst.setShowWidth(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, ATTR_SOURCE)) {
          dst.setSource(value.trim());
        } else if (BeeUtils.same(key, ATTR_REQUIRED)) {
          dst.setRequired(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, ATTR_MIN_VALUE)) {
          dst.setMinValue(value.trim());
        } else if (BeeUtils.same(key, ATTR_MAX_VALUE)) {
          dst.setMaxValue(value.trim());

        } else if (BeeUtils.same(key, ATTR_REL_SOURCE)) {
          dst.setRelSource(value.trim());
        } else if (BeeUtils.same(key, ATTR_REL_VIEW)) {
          dst.setRelView(value.trim());
        } else if (BeeUtils.same(key, ATTR_REL_COLUMN)) {
          dst.setRelColumn(value.trim());

        } else if (BeeUtils.same(key, ATTR_TYPE)) {
          dst.setValueType(ValueType.getByTypeCode(value));
        } else if (BeeUtils.same(key, ATTR_PRECISION)) {
          dst.setPrecision(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SCALE)) {
          dst.setScale(BeeUtils.toIntOrNull(value));

        } else if (BeeUtils.same(key, ATTR_SEARCH_BY)) {
          dst.setSearchBy(value.trim());
        } else if (BeeUtils.same(key, ATTR_SORT_BY)) {
          dst.setSortBy(value.trim());

        } else if (BeeUtils.same(key, ATTR_CELL)) {
          dst.setCellType(CellType.getByCode(value));
        }
      }
    }

    Element styleElement = XmlUtils.getFirstChildElement(src, TAG_STYLE);
    if (styleElement != null) {
      StyleDeclaration headerStyle = XmlUtils.getStyle(styleElement, TAG_HEADER_STYLE);
      if (headerStyle != null) {
        dst.setHeaderStyle(headerStyle);
      }
      StyleDeclaration bodyStyle = XmlUtils.getStyle(styleElement, TAG_BODY_STYLE);
      if (bodyStyle != null) {
        dst.setBodyStyle(bodyStyle);
      }
      StyleDeclaration footerStyle = XmlUtils.getStyle(styleElement, TAG_FOOTER_STYLE);
      if (footerStyle != null) {
        dst.setFooterStyle(footerStyle);
      }

      NodeList dynStyleNodes = styleElement.getElementsByTagName(TAG_DYN_STYLE);
      if (dynStyleNodes != null && dynStyleNodes.getLength() > 0) {
        List<ConditionalStyleDeclaration> dynStyles = Lists.newArrayList();
        for (int i = 0; i < dynStyleNodes.getLength(); i++) {
          ConditionalStyleDeclaration cs =
              XmlUtils.getConditionalStyle((Element) dynStyleNodes.item(i));
          if (cs != null) {
            dynStyles.add(cs);
          }
        }
        if (!dynStyles.isEmpty()) {
          dst.setDynStyles(dynStyles);
        }
      }
    }

    Calculation validation = XmlUtils.getCalculation(src, TAG_VALIDATION);
    if (validation != null) {
      dst.setValidation(validation);
    }
    Calculation editable = XmlUtils.getCalculation(src, TAG_EDITABLE);
    if (editable != null) {
      dst.setEditable(editable);
    }
    Calculation carry = XmlUtils.getCalculation(src, TAG_CARRY);
    if (carry != null) {
      dst.setCarry(carry);
    }

    Calculation calc = XmlUtils.getCalculation(src, TAG_CALC);
    if (calc != null) {
      dst.setCalc(calc);
    }

    EditorDescription editor = getEditor(src);
    if (editor != null) {
      dst.setEditor(editor);
    }
  }

  private void xmlToGrid(Element src, GridDescription dst, BeeView view) {
    Assert.notNull(src);
    Assert.notNull(dst);

    String caption = src.getAttribute(ATTR_CAPTION);
    if (!BeeUtils.isEmpty(caption)) {
      dst.setCaption(caption.trim());
    }
    
    if (view != null) {
      String filter = src.getAttribute(ATTR_FILTER);
      if (!BeeUtils.isEmpty(filter)) {
        dst.setFilter(view.parseFilter(filter.trim()));
      }
      String order = src.getAttribute(ATTR_ORDER);
      if (!BeeUtils.isEmpty(order)) {
        dst.setOrder(view.parseOrder(order.trim()));
      }
    }

    Integer minColumnWidth = XmlUtils.getAttributeInteger(src, ATTR_MIN_COLUMN_WIDTH);
    if (minColumnWidth != null) {
      dst.setMinColumnWidth(minColumnWidth);
    }
    Integer maxColumnWidth = XmlUtils.getAttributeInteger(src, ATTR_MAX_COLUMN_WIDTH);
    if (maxColumnWidth != null) {
      dst.setMaxColumnWidth(maxColumnWidth);
    }

    Boolean hasHeaders = XmlUtils.getAttributeBoolean(src, ATTR_HAS_HEADERS);
    if (hasHeaders != null) {
      dst.setHasHeaders(hasHeaders);
    }
    Boolean hasFooters = XmlUtils.getAttributeBoolean(src, ATTR_HAS_FOOTERS);
    if (hasFooters != null) {
      dst.setHasFooters(hasFooters);
    }
    String footerEvents = src.getAttribute(ATTR_FOOTER_EVENTS);
    if (!BeeUtils.isEmpty(footerEvents)) {
      dst.setFooterEvents(footerEvents);
    }
    
    Boolean showColumnWidths = XmlUtils.getAttributeBoolean(src, ATTR_SHOW_COLUMN_WIDTHS);
    if (showColumnWidths != null) {
      dst.setShowColumnWidths(showColumnWidths);
    }

    Boolean caching = XmlUtils.getAttributeBoolean(src, ATTR_CACHING);
    if (caching != null) {
      dst.setCaching(caching);
    }

    Integer asyncThreshold = XmlUtils.getAttributeInteger(src, ATTR_ASYNC_THRESHOLD);
    if (asyncThreshold != null) {
      dst.setAsyncThreshold(asyncThreshold);
    }
    Integer pagingThreshold = XmlUtils.getAttributeInteger(src, ATTR_PAGING_THRESHOLD);
    if (pagingThreshold != null) {
      dst.setPagingThreshold(pagingThreshold);
    }
    Integer searchThreshold = XmlUtils.getAttributeInteger(src, ATTR_SEARCH_THRESHOLD);
    if (searchThreshold != null) {
      dst.setSearchThreshold(searchThreshold);
    }

    Integer initialRowSetSize = XmlUtils.getAttributeInteger(src, ATTR_INITIAL_ROW_SET_SIZE);
    if (initialRowSetSize != null) {
      dst.setInitialRowSetSize(initialRowSetSize);
    }

    Boolean readOnly = XmlUtils.getAttributeBoolean(src, ATTR_READ_ONLY);
    if (readOnly != null) {
      dst.setReadOnly(readOnly);
    }
    String enabledActions = src.getAttribute(ATTR_ENABLED_ACTIONS);
    if (!BeeUtils.isEmpty(enabledActions)) {
      dst.setEnabledActions(Action.parse(enabledActions));
    }
    String disabledActions = src.getAttribute(ATTR_DISABLED_ACTIONS);
    if (!BeeUtils.isEmpty(disabledActions)) {
      dst.setDisabledActions(Action.parse(disabledActions));
    }

    String newRowForm = src.getAttribute(ATTR_NEW_ROW_FORM);
    if (!BeeUtils.isEmpty(newRowForm)) {
      dst.setNewRowForm(newRowForm);
    }
    String newRowColumns = src.getAttribute(ATTR_NEW_ROW_COLUMNS);
    if (!BeeUtils.isEmpty(newRowColumns)) {
      dst.setNewRowColumns(newRowColumns.trim());
    }
    String newRowCaption = src.getAttribute(ATTR_NEW_ROW_CAPTION);
    if (!BeeUtils.isEmpty(newRowCaption)) {
      dst.setNewRowCaption(newRowCaption.trim());
    }

    String editForm = src.getAttribute(ATTR_EDIT_FORM);
    if (!BeeUtils.isEmpty(editForm)) {
      dst.setEditForm(editForm);
    }
    Boolean editMode = XmlUtils.getAttributeBoolean(src, ATTR_EDIT_MODE);
    if (editMode != null) {
      dst.setEditMode(editMode);
    }
    Boolean editSave = XmlUtils.getAttributeBoolean(src, ATTR_EDIT_SAVE);
    if (editSave != null) {
      dst.setEditSave(editSave);
    }
    Calculation editMessage = XmlUtils.getCalculation(src, TAG_EDIT_MESSAGE);
    if (editMessage != null) {
      dst.setEditMessage(editMessage);
    }
    Boolean editShowId = XmlUtils.getAttributeBoolean(src, ATTR_EDIT_SHOW_ID);
    if (editShowId != null) {
      dst.setEditShowId(editShowId);
    }
    String editInPlace = src.getAttribute(ATTR_EDIT_IN_PLACE);
    if (!BeeUtils.isEmpty(editInPlace)) {
      dst.setEditInPlace(editInPlace.trim());
    }
    Boolean editNewRow = XmlUtils.getAttributeBoolean(src, ATTR_EDIT_NEW_ROW);
    if (editNewRow != null) {
      dst.setEditNewRow(editNewRow);
    }

    NodeList cssNodes = src.getElementsByTagName(TAG_CSS);
    if (cssNodes != null && cssNodes.getLength() > 0) {
      Map<String, String> styleSheets = Maps.newHashMap();
      for (int i = 0; i < cssNodes.getLength(); i++) {
        String name = ((Element) cssNodes.item(i)).getAttribute(ATTR_ID);
        String text = cssNodes.item(i).getTextContent();
        if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(text)) {
          styleSheets.put(name.trim(), text.trim());
        }
      }
      if (!styleSheets.isEmpty()) {
        dst.setStyleSheets(styleSheets);
      }
    }

    List<Element> widgetElements = XmlUtils.getChildrenElements(src, WIDGET_TAGS);
    if (!widgetElements.isEmpty()) {
      List<String> widgets = Lists.newArrayList();
      for (Element widgetElement : widgetElements) {
        widgets.add(XmlUtils.toString(widgetElement, false));
      }
      dst.setWidgets(widgets);
    }

    GridComponentDescription header = getComponent(src, TAG_HEADER);
    if (header != null) {
      dst.setHeader(header);
    }
    GridComponentDescription body = getComponent(src, TAG_BODY);
    if (body != null) {
      dst.setBody(body);
    }
    GridComponentDescription footer = getComponent(src, TAG_FOOTER);
    if (footer != null) {
      dst.setFooter(footer);
    }

    NodeList rowStyleNodes = src.getElementsByTagName(TAG_ROW_STYLE);
    if (rowStyleNodes != null && rowStyleNodes.getLength() > 0) {
      List<ConditionalStyleDeclaration> rowStyles = Lists.newArrayList();
      for (int i = 0; i < rowStyleNodes.getLength(); i++) {
        ConditionalStyleDeclaration cs =
            XmlUtils.getConditionalStyle((Element) rowStyleNodes.item(i));
        if (cs != null) {
          rowStyles.add(cs);
        }
      }
      if (!rowStyles.isEmpty()) {
        dst.setRowStyles(rowStyles);
      }
    }

    Calculation rowMessage = XmlUtils.getCalculation(src, TAG_ROW_MESSAGE);
    if (rowMessage != null) {
      dst.setRowMessage(rowMessage);
    }
    Calculation rowEditable = XmlUtils.getCalculation(src, TAG_ROW_EDITABLE);
    if (rowEditable != null) {
      dst.setRowEditable(rowEditable);
    }
    Calculation rowValidation = XmlUtils.getCalculation(src, TAG_ROW_VALIDATION);
    if (rowValidation != null) {
      dst.setRowValidation(rowValidation);
    }
  }
}
