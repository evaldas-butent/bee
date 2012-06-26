package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.HasService;
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
import com.butent.bee.shared.ui.RefreshType;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.ui.RenderableToken;
import com.butent.bee.shared.ui.RendererDescription;
import com.butent.bee.shared.ui.RendererType;
import com.butent.bee.shared.ui.SelectorColumn;
import com.butent.bee.shared.ui.StyleDeclaration;
import com.butent.bee.shared.ui.UiConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

  private static final String TAG_GRID = "Grid";
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

  private static final String TAG_VALIDATION = "validation";
  private static final String TAG_EDITABLE = "editable";
  private static final String TAG_CARRY = "carry";

  private static final String TAG_EDITOR = "editor";

  private static final Set<String> WIDGET_TAGS = Sets.newHashSet("north", "south", "west", "east");

  private static final String ATTR_PARENT = "parent";

  private static final String ATTR_FILTER = "filter";
  private static final String ATTR_ORDER = "order";

  private static final String ATTR_SHOW_COLUMN_WIDTHS = "showColumnWidths";
  private static final String ATTR_MIN_COLUMN_WIDTH = "minColumnWidth";
  private static final String ATTR_MAX_COLUMN_WIDTH = "maxColumnWidth";

  private static final String ATTR_HAS_HEADERS = "hasHeaders";
  private static final String ATTR_HAS_FOOTERS = "hasFooters";
  private static final String ATTR_FOOTER_EVENTS = "footerEvents";

  private static final String ATTR_CACHE_DATA = "cacheData";

  private static final String ATTR_ASYNC_THRESHOLD = "asyncThreshold";
  private static final String ATTR_PAGING_THRESHOLD = "pagingThreshold";
  private static final String ATTR_SEARCH_THRESHOLD = "searchThreshold";
  private static final String ATTR_INITIAL_ROW_SET_SIZE = "initialRowSetSize";

  private static final String ATTR_ENABLED_ACTIONS = "enabledActions";
  private static final String ATTR_DISABLED_ACTIONS = "disabledActions";

  private static final String ATTR_NEW_ROW_DEFAULTS = "newRowDefaults";
  private static final String ATTR_NEW_ROW_POPUP = "newRowPopup";

  private static final String ATTR_EDIT_FORM = "editForm";
  private static final String ATTR_EDIT_MODE = "editMode";
  private static final String ATTR_EDIT_SAVE = "editSave";
  private static final String ATTR_EDIT_SHOW_ID = "editShowId";
  private static final String ATTR_EDIT_IN_PLACE = "editInPlace";
  private static final String ATTR_EDIT_NEW_ROW = "editNewRow";
  private static final String ATTR_EDIT_POPUP = "editPopup";

  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_MIN_WIDTH = "minWidth";
  private static final String ATTR_MAX_WIDTH = "maxWidth";
  private static final String ATTR_AUTO_FIT = "autoFit";

  private static final String ATTR_SORTABLE = "sortable";
  private static final String ATTR_VISIBLE = "visible";

  private static final String ATTR_HAS_FOOTER = "hasFooter";
  private static final String ATTR_SHOW_WIDTH = "showWidth";

  private static final String ATTR_REQUIRED = "required";

  private static final String ATTR_MIN_VALUE = "minValue";
  private static final String ATTR_MAX_VALUE = "maxValue";

  private static final String ATTR_TYPE = "type";
  private static final String ATTR_PRECISION = "precision";

  private static final String ATTR_SORT_BY = "sortBy";
  private static final String ATTR_SEARCH_BY = "searchBy";

  private static final String ATTR_CELL = "cell";
  private static final String ATTR_ELEMENT = "element";

  private static final String ATTR_ID = "id";

  private static final String ATTR_FAVORITE = "favorite";

  private static final String ATTR_MIN_NUMBER_OF_ROWS = "minNumberOfRows";
  private static final String ATTR_MAX_NUMBER_OF_ROWS = "maxNumberOfRows";

  private static final String ATTR_RENDER_MODE = "renderMode";

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

    ColumnDescription columnDescription;
    for (String colName : view.getColumnNames()) {
      int level = view.getColumnLevel(colName);
      boolean readOnly = view.isColReadOnly(colName);

      ColType colType = (level <= 0 || readOnly) ? ColType.DATA : ColType.RELATED;
      columnDescription = new ColumnDescription(colType, colName);
      columnDescription.setSource(colName);

      if (readOnly) {
        columnDescription.setReadOnly(true);
      } else if (ColType.RELATED.equals(colType)) {
        columnDescription.setRelation(Relation.create(null, null, null, null, null));
      }

      gridDescription.addColumn(columnDescription);
    }

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
    if (!BeeUtils.same(XmlUtils.getLocalName(gridElement), TAG_GRID)) {
      LogUtils.warning(logger, "unrecognized grid element tag name", gridElement.getTagName());
      return null;
    }

    String gridName = gridElement.getAttribute(UiConstants.ATTR_NAME);
    String viewName = gridElement.getAttribute(UiConstants.ATTR_VIEW_NAME);

    if (BeeUtils.isEmpty(gridName)) {
      LogUtils.warning(logger, "Grid attribute", UiConstants.ATTR_NAME, "not found");
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

    List<Element> columnGroups = XmlUtils.getElementsByLocalName(gridElement, TAG_COLUMNS);
    if (columnGroups.isEmpty()) {
      LogUtils.warning(logger, "Grid", gridName, "tag", TAG_COLUMNS, "not found");
      return null;
    }

    List<Element> columns = Lists.newArrayList();
    for (int i = 0; i < columnGroups.size(); i++) {
      columns.addAll(XmlUtils.getChildrenElements(columnGroups.get(i)));
    }
    if (columns.isEmpty()) {
      LogUtils.warning(logger, "Grid", gridName, "has no columns");
      return null;
    }

    for (int i = 0; i < columns.size(); i++) {
      Element columnElement = columns.get(i);

      String colTag = XmlUtils.getLocalName(columnElement);
      ColType colType = ColType.getColType(colTag);
      String colName = columnElement.getAttribute(UiConstants.ATTR_NAME);

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

      if (ColType.RELATED.equals(colType)) {
        column.setRelation(getRelation(columnElement));
      }

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

    List<Element> itemNodes = XmlUtils.getElementsByLocalName(element, HasItems.TAG_ITEM);
    if (!itemNodes.isEmpty()) {
      List<String> items = Lists.newArrayList();
      for (int i = 0; i < itemNodes.size(); i++) {
        String item = itemNodes.get(i).getTextContent();
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

  private Relation getRelation(Element element) {
    Assert.notNull(element);

    RendererDescription rowRenderer = null;
    Calculation rowRender = null;
    List<RenderableToken> rowRenderTokens = null;

    List<SelectorColumn> selectorColumns = Lists.newArrayList();

    for (Element child : XmlUtils.getChildrenElements(element)) {
      String tagName = XmlUtils.getLocalName(child);

      if (BeeUtils.same(tagName, Relation.TAG_ROW_RENDERER)) {
        rowRenderer = getRenderer(child, null);

      } else if (BeeUtils.same(tagName, Relation.TAG_ROW_RENDER)) {
        rowRender = XmlUtils.getCalculation(child);

      } else if (BeeUtils.same(tagName, Relation.TAG_ROW_RENDER_TOKEN)) {
        RenderableToken token = RenderableToken.create(XmlUtils.getAttributes(child));
        if (token != null) {
          if (rowRenderTokens == null) {
            rowRenderTokens = Lists.newArrayList(token);
          } else {
            rowRenderTokens.add(token);
          }
        }

      } else if (BeeUtils.same(tagName, Relation.TAG_SELECTOR_COLUMN)) {
        RendererDescription renderer = getRenderer(child, RendererDescription.TAG_RENDERER, null);
        Calculation render = XmlUtils.getCalculation(child, RendererDescription.TAG_RENDER);
        List<RenderableToken> tokens = getRenderTokens(child, RenderableToken.TAG_RENDER_TOKEN);

        selectorColumns.add(SelectorColumn.create(XmlUtils.getAttributes(child),
            renderer, render, tokens));
      }
    }
    return Relation.create(XmlUtils.getAttributes(element), selectorColumns, rowRenderer,
        rowRender, rowRenderTokens);
  }

  private RendererDescription getRenderer(Element parent, String tagName,
      EditorDescription editor) {
    Assert.notNull(parent);

    Element element = XmlUtils.getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    } else {
      return getRenderer(element, editor);
    }
  }

  private RendererDescription getRenderer(Element element, EditorDescription editor) {
    if (element == null) {
      return null;
    }
    String typeCode = element.getAttribute(RendererDescription.ATTR_TYPE);
    if (BeeUtils.isEmpty(typeCode)) {
      return null;
    }
    RendererType type = RendererType.getByTypeCode(typeCode);
    if (type == null) {
      return null;
    }

    RendererDescription renderer = new RendererDescription(type);
    renderer.setAttributes(XmlUtils.getAttributes(element));

    List<String> items = Lists.newArrayList();
    List<Element> itemNodes = XmlUtils.getElementsByLocalName(element, HasItems.TAG_ITEM);
    if (!itemNodes.isEmpty()) {
      for (int i = 0; i < itemNodes.size(); i++) {
        String item = itemNodes.get(i).getTextContent();
        if (!BeeUtils.isEmpty(item)) {
          items.add(item);
        }
      }
    }

    if (items.isEmpty() && editor != null && editor.getItems() != null) {
      items.addAll(editor.getItems());
      if (renderer.getValueStartIndex() == null && editor.getValueStartIndex() != null) {
        renderer.setValueStartIndex(editor.getValueStartIndex());
      }
    }
    if (!items.isEmpty()) {
      renderer.setItems(items);
    }

    return renderer;
  }

  private List<RenderableToken> getRenderTokens(Element parent, String tagName) {
    if (parent == null) {
      return null;
    }
    List<Element> tokens = XmlUtils.getElementsByLocalName(parent, tagName);
    if (tokens.isEmpty()) {
      return null;
    }

    List<RenderableToken> result = Lists.newArrayList();
    for (Element token : tokens) {
      RenderableToken renderableToken = RenderableToken.create(XmlUtils.getAttributes(token));
      if (renderableToken != null) {
        result.add(renderableToken);
      }
    }
    return result;
  }

  private boolean initColumn(BeeView view, ColumnDescription columnDescription) {
    Assert.notNull(columnDescription);

    ColType colType = columnDescription.getColType();
    String source = columnDescription.getSource();

    if (!colType.isReadOnly() && BeeUtils.isEmpty(source)) {
      source = columnDescription.getName();
      columnDescription.setSource(source);
    }

    if (view == null) {
      return true;
    }

    boolean ok = false;
    String viewName = view.getName();

    switch (colType) {
      case ID:
        columnDescription.setSource(view.getSourceIdName());
        ok = true;
        break;

      case VERSION:
        columnDescription.setSource(view.getSourceVersionName());
        ok = true;
        break;

      case DATA:
      case RELATED:
        if (view.hasColumn(source)) {
          if (view.isColReadOnly(source)
              || colType.equals(ColType.DATA) && view.getColumnLevel(source) > 0) {
            columnDescription.setReadOnly(true);
          }
          ok = true;
        } else {
          LogUtils.warning(logger, viewName, "unrecognized view column:", source);
        }
        break;

      case CALCULATED:
      case SELECTION:
        ok = true;
        break;

      case ACTION:
        if (BeeUtils.isEmpty(source) && view.hasColumn(columnDescription.getName())) {
          columnDescription.setSource(columnDescription.getName());
        }
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

        if (BeeUtils.same(key, UiConstants.ATTR_CAPTION)) {
          dst.setCaption(value.trim());
        } else if (BeeUtils.same(key, UiConstants.ATTR_READ_ONLY)) {
          dst.setReadOnly(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, ATTR_WIDTH)) {
          dst.setWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_MIN_WIDTH)) {
          dst.setMinWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_MAX_WIDTH)) {
          dst.setMaxWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_AUTO_FIT)) {
          dst.setAutoFit(value.trim());

        } else if (BeeUtils.same(key, ATTR_SORTABLE)) {
          dst.setSortable(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_VISIBLE)) {
          dst.setVisible(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, UiConstants.ATTR_FORMAT)) {
          dst.setFormat(value.trim());
        } else if (BeeUtils.same(key, UiConstants.ATTR_HORIZONTAL_ALIGNMENT)) {
          dst.setHorAlign(value.trim());

        } else if (BeeUtils.same(key, ATTR_HAS_FOOTER)) {
          dst.setHasFooter(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SHOW_WIDTH)) {
          dst.setShowWidth(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, UiConstants.ATTR_SOURCE)) {
          dst.setSource(value.trim());
        } else if (BeeUtils.same(key, ATTR_REQUIRED)) {
          dst.setRequired(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, ATTR_MIN_VALUE)) {
          dst.setMinValue(value.trim());
        } else if (BeeUtils.same(key, ATTR_MAX_VALUE)) {
          dst.setMaxValue(value.trim());

        } else if (BeeUtils.same(key, ATTR_TYPE)) {
          dst.setValueType(ValueType.getByTypeCode(value));
        } else if (BeeUtils.same(key, ATTR_PRECISION)) {
          dst.setPrecision(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, UiConstants.ATTR_SCALE)) {
          dst.setScale(BeeUtils.toIntOrNull(value));

        } else if (BeeUtils.same(key, RendererDescription.ATTR_RENDER_COLUMNS)) {
          dst.setRenderColumns(NameUtils.toList(value.trim()));
        } else if (BeeUtils.same(key, ATTR_SORT_BY)) {
          dst.setSortBy(value.trim());
        } else if (BeeUtils.same(key, ATTR_SEARCH_BY)) {
          dst.setSearchBy(value.trim());

        } else if (BeeUtils.same(key, ATTR_CELL)) {
          dst.setCellType(CellType.getByCode(value));
        } else if (BeeUtils.same(key, ATTR_ELEMENT)) {
          dst.setElementType(value.trim());

        } else if (BeeUtils.same(key, RefreshType.ATTR_UPDATE_MODE)) {
          dst.setUpdateMode(RefreshType.getByCode(value));

        } else if (BeeUtils.same(key, HasItems.ATTR_ITEM_KEY)) {
          dst.setItemKey(value.trim());

        } else if (BeeUtils.same(key, HasService.ATTR_SERVICE)) {
          dst.setService(value.trim());
        } else if (BeeUtils.same(key, HasOptions.ATTR_OPTIONS)) {
          dst.setOptions(value.trim());
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
    }

    List<Element> dynStyleNodes = XmlUtils.getElementsByLocalName(src,
        ConditionalStyleDeclaration.TAG_DYN_STYLE);
    if (!dynStyleNodes.isEmpty()) {
      List<ConditionalStyleDeclaration> dynStyles = Lists.newArrayList();
      for (int i = 0; i < dynStyleNodes.size(); i++) {
        ConditionalStyleDeclaration cs = XmlUtils.getConditionalStyle(dynStyleNodes.get(i));
        if (cs != null) {
          dynStyles.add(cs);
        }
      }
      if (!dynStyles.isEmpty()) {
        dst.setDynStyles(dynStyles);
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

    EditorDescription editor = getEditor(src);
    if (editor != null) {
      dst.setEditor(editor);
    }

    RendererDescription renderer = getRenderer(src, RendererDescription.TAG_RENDERER, editor);
    if (renderer != null) {
      dst.setRendererDescription(renderer);
    }
    Calculation render = XmlUtils.getCalculation(src, RendererDescription.TAG_RENDER);
    if (render != null) {
      dst.setRender(render);
    }
    List<RenderableToken> renderTokens = getRenderTokens(src, RenderableToken.TAG_RENDER_TOKEN);
    if (!BeeUtils.isEmpty(renderTokens)) {
      dst.setRenderTokens(renderTokens);
    }
  }

  private void xmlToGrid(Element src, GridDescription dst, BeeView view) {
    Assert.notNull(src);
    Assert.notNull(dst);

    String parent = src.getAttribute(ATTR_PARENT);
    if (!BeeUtils.isEmpty(parent)) {
      dst.setParent(parent.trim());
    }

    String caption = src.getAttribute(UiConstants.ATTR_CAPTION);
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
    String autoFit = src.getAttribute(ATTR_AUTO_FIT);
    if (!BeeUtils.isEmpty(autoFit)) {
      dst.setAutoFit(autoFit);
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

    Boolean cacheData = XmlUtils.getAttributeBoolean(src, ATTR_CACHE_DATA);
    if (cacheData != null) {
      dst.setCacheData(cacheData);
    }
    Boolean cacheDescription = XmlUtils.getAttributeBoolean(src,
        UiConstants.ATTR_CACHE_DESCRIPTION);
    if (cacheDescription != null) {
      dst.setCacheDescription(cacheDescription);
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

    Boolean readOnly = XmlUtils.getAttributeBoolean(src, UiConstants.ATTR_READ_ONLY);
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

    String favorite = src.getAttribute(ATTR_FAVORITE);
    if (!BeeUtils.isEmpty(favorite)) {
      dst.setFavorite(favorite.trim());
    }

    Integer minNumberOfRows = XmlUtils.getAttributeInteger(src, ATTR_MIN_NUMBER_OF_ROWS);
    if (minNumberOfRows != null) {
      dst.setMinNumberOfRows(minNumberOfRows);
    }
    Integer maxNumberOfRows = XmlUtils.getAttributeInteger(src, ATTR_MAX_NUMBER_OF_ROWS);
    if (maxNumberOfRows != null) {
      dst.setMaxNumberOfRows(maxNumberOfRows);
    }

    String renderMode = src.getAttribute(ATTR_RENDER_MODE);
    if (!BeeUtils.isEmpty(renderMode)) {
      dst.setRenderMode(renderMode.trim());
    }
    
    String newRowForm = src.getAttribute(UiConstants.ATTR_NEW_ROW_FORM);
    if (!BeeUtils.isEmpty(newRowForm)) {
      dst.setNewRowForm(newRowForm);
    }

    String newRowColumns = src.getAttribute(UiConstants.ATTR_NEW_ROW_COLUMNS);
    if (!BeeUtils.isEmpty(newRowColumns)) {
      dst.setNewRowColumns(newRowColumns.trim());
    }
    String newRowDefaults = src.getAttribute(ATTR_NEW_ROW_DEFAULTS);
    if (!BeeUtils.isEmpty(newRowDefaults)) {
      dst.setNewRowDefaults(newRowDefaults.trim());
    }

    String newRowCaption = src.getAttribute(UiConstants.ATTR_NEW_ROW_CAPTION);
    if (!BeeUtils.isEmpty(newRowCaption)) {
      dst.setNewRowCaption(newRowCaption.trim());
    }
    Boolean newRowPopup = XmlUtils.getAttributeBoolean(src, ATTR_NEW_ROW_POPUP);
    if (newRowPopup != null) {
      dst.setNewRowPopup(newRowPopup);
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
    Boolean editPopup = XmlUtils.getAttributeBoolean(src, ATTR_EDIT_POPUP);
    if (editPopup != null) {
      dst.setEditPopup(editPopup);
    }

    List<Element> cssNodes = XmlUtils.getElementsByLocalName(src, TAG_CSS);
    if (!cssNodes.isEmpty()) {
      Map<String, String> styleSheets = Maps.newHashMap();
      for (int i = 0; i < cssNodes.size(); i++) {
        String name = cssNodes.get(i).getAttribute(ATTR_ID);
        String text = cssNodes.get(i).getTextContent();
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

    List<Element> rowStyleNodes = XmlUtils.getElementsByLocalName(src, TAG_ROW_STYLE);
    if (!rowStyleNodes.isEmpty()) {
      List<ConditionalStyleDeclaration> rowStyles = Lists.newArrayList();
      for (int i = 0; i < rowStyleNodes.size(); i++) {
        ConditionalStyleDeclaration cs = XmlUtils.getConditionalStyle(rowStyleNodes.get(i));
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
