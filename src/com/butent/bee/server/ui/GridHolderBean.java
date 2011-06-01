package com.butent.bee.server.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.BeeView;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.ui.BeeGrid;
import com.butent.bee.shared.ui.Calculation;
import com.butent.bee.shared.ui.ConditionalStyle;
import com.butent.bee.shared.ui.GridColumn;
import com.butent.bee.shared.ui.GridComponent;
import com.butent.bee.shared.ui.GridColumn.ColType;
import com.butent.bee.shared.ui.Style;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.LogUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

/**
 * Reads grids structure from xml files and stores them into cache.
 */

@Singleton
@Lock(LockType.READ)
public class GridHolderBean {

  public static final String GRID_SCHEMA = "grid.xsd";
  public static final String GRID_PATH = "grids/";
  
  private static final String TAG_GRID = "BeeGrid";
  private static final String TAG_COLUMNS = "columns";
  
  private static final String TAG_SETTINGS = "settings";
  private static final String TAG_HEADER = "header";
  private static final String TAG_BODY = "body";
  private static final String TAG_FOOTER = "footer";
  private static final String TAG_ROW_STYLE = "rowStyle";
  private static final String TAG_ROW_MESSAGE = "rowMessage";
  private static final String TAG_ROW_EDITABLE = "rowEditable";

  private static final String TAG_STYLE = "style";
  private static final String TAG_HEADER_STYLE = "headerStyle";
  private static final String TAG_BODY_STYLE = "bodyStyle";
  private static final String TAG_FOOTER_STYLE = "footerStyle";
  private static final String TAG_DYN_STYLE = "dynStyle";

  private static final String TAG_VALIDATION = "validation";
  private static final String TAG_EDITABLE = "editable";
  private static final String TAG_CARRY = "carry";
  
  private static final String ATTR_VIEW_NAME = "viewName";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_CAPTION = "caption";
  private static final String ATTR_READ_ONLY = "readOnly";
  private static final String ATTR_MIN_COLUMN_WIDTH = "minColumnWidth";
  private static final String ATTR_MAX_COLUMN_WIDTH = "maxColumnWidth";

  private static final String ATTR_HAS_HEADERS = "hasHeaders";
  private static final String ATTR_HAS_FOOTERS = "hasFooters";
  private static final String ATTR_ASYNC_THRESHOLD = "asyncThreshold";
  private static final String ATTR_PAGING_THRESHOLD = "pagingThreshold";
  private static final String ATTR_SEARCH_THRESHOLD = "searchThreshold";
  private static final String ATTR_NEW_ROW_COLUMNS = "newRowColumns";
  private static final String ATTR_SHOW_COLUMN_WIDTHS = "showColumnWidths";
  
  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_MIN_WIDTH = "minWidth";
  private static final String ATTR_MAX_WIDTH = "maxWidth";
  private static final String ATTR_SORTABLE = "sortable";
  private static final String ATTR_VISIBLE = "visible";
  private static final String ATTR_FORMAT = "format";

  private static final String ATTR_HAS_FOOTER = "hasFooter";
  private static final String ATTR_SHOW_WIDTH = "showWidth";
  
  private static final String ATTR_SOURCE = "source";
  private static final String ATTR_EDITOR = "editor";

  private static final String ATTR_MIN_VALUE = "minValue";
  private static final String ATTR_MAX_VALUE = "maxValue";
  private static final String ATTR_STEP_VALUE = "stepValue";
  
  private static final String ATTR_RELATION = "relation";

  private static Logger logger = Logger.getLogger(GridHolderBean.class.getName());

  @EJB
  SystemBean sys;

  private Map<String, BeeGrid> gridCache = Maps.newHashMap();

  public BeeGrid getGrid(String gridName) {
    Assert.state(isGrid(gridName), "Not a grid: " + gridName);
    return gridCache.get(gridKey(gridName));
  }

  public void initGrids() {
    gridCache.clear();
  }

  public boolean isGrid(String gridName) {
    if (BeeUtils.isEmpty(gridName)) {
      return false;
    }
    if (!gridCache.containsKey(gridKey(gridName))) {
      initGrid(gridName);
    }
    return gridCache.containsKey(gridKey(gridName));
  }

  private GridComponent getComponent(Element parent, String tagName) {
    Assert.notNull(parent);
    Assert.notEmpty(tagName);
    
    Element element = XmlUtils.getFirstChildElement(parent, tagName);
    if (element == null) {
      return null;
    }
    
    Style style = XmlUtils.getStyle(element, GridComponent.TAG_STYLE);
    Map<String, String> attributes = XmlUtils.getAttributes(element);
    
    if (style == null && (attributes == null || attributes.isEmpty())) {
      return null;
    }
    return new GridComponent(style, attributes);
  }

  private String gridKey(String gridName) {
    Assert.notEmpty(gridName);
    return gridName.trim().toLowerCase();
  }

  @SuppressWarnings("unused")
  @PostConstruct
  private void init() {
    initGrids();
  }

  private boolean initColumn(BeeView view, GridColumn column) {
    Assert.notNull(view);
    Assert.notNull(column);
    boolean ok = false;

    String viewName = view.getName();
    String source = column.getSource();
    
    switch (column.getType()) {
      case DATA:
        if (view.hasColumn(source)) {
          ok = true;
        } else {
          LogUtils.warning(logger, viewName, "unrecognized view column:", source);
        }
        break;

      case RELATED:
        if (view.hasColumn(source)) {
          String relTable = sys.getRelation(view.getTable(source), view.getField(source));

          if (!BeeUtils.isEmpty(relTable)) {
            String relField = column.getRelField();

            if (sys.hasField(relTable, relField)) {
              column.setRelTable(relTable);
              ok = true;
            } else {
              LogUtils.warning(logger, viewName, "unrecognized relation field:", relTable, relField);
            }
          } else {
            LogUtils.warning(logger, viewName, "not a relation column:", source);
          }
        } else {
          LogUtils.warning(logger, viewName, "unrecognized view column:", source);
        }
        break;
        
      case CALCULATED:
        if (column.getCalc() == null || column.getCalc().isEmpty()) {
          LogUtils.warning(logger, viewName, "column", column.getName(),
              "calculation not specified");
        } else {
          ok = true;
        }
        break;
      
      default:
        ok = true;
    }
    
    return ok;
  }
  
  @Lock(LockType.WRITE)
  private void initGrid(String gridName) {
    String resource = Config.getPath(GRID_PATH + gridName + ".xml");

    if (!BeeUtils.isEmpty(resource)) {
      boolean loaded = false;
      Collection<BeeGrid> grids = loadGrids(resource, Config.getSchemaPath(GRID_SCHEMA));

      if (!BeeUtils.isEmpty(grids)) {
        for (BeeGrid grid : grids) {
          if (BeeUtils.same(grid.getName(), gridName)) {
            if (loaded) {
              LogUtils.warning(logger, resource, "Dublicate grid name:", gridName);
            } else if (grid.isEmpty()) {
              LogUtils.warning(logger, resource, "Grid has no columns defined:", gridName);
            } else {
              loaded = true;
              registerGrid(grid);
            }
          }
        }
      }
      if (loaded) {
        LogUtils.info(logger, "Loaded grid [", gridName, "] description from", resource);
      } else {
        LogUtils.warning(logger, resource, "Grid description not found:", gridName);
      }
    }
  }
  
  @Lock(LockType.WRITE)
  private Collection<BeeGrid> loadGrids(String resource, String schema) {
    Document xml = XmlUtils.getXmlResource(resource, schema);
    if (BeeUtils.isEmpty(xml)) {
      return null;
    }

    Collection<BeeGrid> grids = Lists.newArrayList();
    Element root = xml.getDocumentElement();
    NodeList gridNodes = root.getElementsByTagName(TAG_GRID);

    for (int i = 0; i < gridNodes.getLength(); i++) {
      Element gridElement = (Element) gridNodes.item(i);

      String gridName = gridElement.getAttribute(ATTR_NAME);
      String viewName = gridElement.getAttribute(ATTR_VIEW_NAME);

      if (BeeUtils.isEmpty(gridName)) {
        LogUtils.warning(logger, "Grid attribute", ATTR_NAME, "not found");
        continue;
      }
      if (!sys.isView(viewName)) {
        LogUtils.warning(logger, "Grid", gridName, "unrecongized view name:", viewName);
        continue;
      }
      
      BeeGrid grid = new BeeGrid(gridName, viewName);
      xmlToGrid(gridElement, grid);
      
      Element container = XmlUtils.getFirstChildElement(gridElement, TAG_COLUMNS);
      if (container == null) {
        LogUtils.warning(logger, "Grid", gridName, "tag", TAG_COLUMNS, "not found");
        continue;
      }
      List<Element> columns = XmlUtils.getChildrenElements(container);
      if (columns == null || columns.isEmpty()) {
        LogUtils.warning(logger, "Grid", gridName, "tag", TAG_COLUMNS, "has no children");
        continue;
      }

      BeeView view = sys.getView(viewName);

      for (int j = 0; j < columns.size(); j++) {
        Element columnElement = columns.get(j);
        
        String colTag = columnElement.getTagName();
        ColType colType = ColType.getColType(colTag);
        String colName = columnElement.getAttribute(ATTR_NAME);

        if (colType == null) {
          LogUtils.warning(logger, "Grid", gridName, "column", j, colName,
              "type", colTag, "not recognized");
          continue;
        }
        if (BeeUtils.isEmpty(colName)) {
          LogUtils.warning(logger, "Grid", gridName, "column", j, colTag, "name not specified");
          continue;
        }
        if (grid.hasColumn(colName)) {
          LogUtils.warning(logger, "Grid", gridName, "column", j, colTag,
            "duplicate column name:", colName);
          continue;
        }
        
        GridColumn column = new GridColumn(colType, colName);
        xmlToColumn(columnElement, column);
        
        if (initColumn(view, column)) {
          grid.addColumn(column);
        }
      }

      grids.add(grid);
    }
    return grids;
  }

  private void registerGrid(BeeGrid grid) {
    if (!BeeUtils.isEmpty(grid)) {
      gridCache.put(gridKey(grid.getName()), grid);
    }
  }
  
  private void xmlToColumn(Element src, GridColumn dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    
    Map<String, String> attributes = XmlUtils.getAttributes(src);
    Element settings = XmlUtils.getFirstChildElement(src, TAG_SETTINGS);
    if (settings != null) {
      attributes.putAll(XmlUtils.getAttributes(settings));
    }

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

        } else if (BeeUtils.same(key, ATTR_HAS_FOOTER)) {
          dst.setHasFooter(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SHOW_WIDTH)) {
          dst.setShowWidth(BeeUtils.toBooleanOrNull(value));

        } else if (BeeUtils.same(key, ATTR_SOURCE)) {
          dst.setSource(value.trim());
        } else if (BeeUtils.same(key, ATTR_EDITOR)) {
          dst.setEditor(value.trim());

        } else if (BeeUtils.same(key, ATTR_MIN_VALUE)) {
          dst.setMinValue(value.trim());
        } else if (BeeUtils.same(key, ATTR_MAX_VALUE)) {
          dst.setMaxValue(value.trim());
        } else if (BeeUtils.same(key, ATTR_STEP_VALUE)) {
          dst.setStepValue(value.trim());

        } else if (BeeUtils.same(key, ATTR_RELATION)) {
          dst.setRelField(value.trim());
        }
      }
    }
    
    Element styleElement = XmlUtils.getFirstChildElement(src, TAG_STYLE);
    if (styleElement != null) {
      Style headerStyle = XmlUtils.getStyle(styleElement, TAG_HEADER_STYLE);
      if (headerStyle != null) {
        dst.setHeaderStyle(headerStyle);
      }
      Style bodyStyle = XmlUtils.getStyle(styleElement, TAG_BODY_STYLE);
      if (bodyStyle != null) {
        dst.setBodyStyle(bodyStyle);
      }
      Style footerStyle = XmlUtils.getStyle(styleElement, TAG_FOOTER_STYLE);
      if (footerStyle != null) {
        dst.setFooterStyle(footerStyle);
      }
    
      NodeList dynStyleNodes = styleElement.getElementsByTagName(TAG_DYN_STYLE);
      if (dynStyleNodes != null && dynStyleNodes.getLength() > 0) {
        List<ConditionalStyle> dynStyles = Lists.newArrayList();
        for (int i = 0; i < dynStyleNodes.getLength(); i++) {
          ConditionalStyle cs = XmlUtils.getConditionalStyle((Element) dynStyleNodes.item(i));
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
  }
  
  private void xmlToGrid(Element src, BeeGrid dst) {
    Assert.notNull(src);
    Assert.notNull(dst);
    
    Map<String, String> attributes = XmlUtils.getAttributes(src);
    Element settings = XmlUtils.getFirstChildElement(src, TAG_SETTINGS);
    if (settings != null) {
      attributes.putAll(XmlUtils.getAttributes(settings));
    }

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
        } else if (BeeUtils.same(key, ATTR_MIN_COLUMN_WIDTH)) {
          dst.setMinColumnWidth(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_MAX_COLUMN_WIDTH)) {
          dst.setMaxColumnWidth(BeeUtils.toIntOrNull(value));

        } else if (BeeUtils.same(key, ATTR_HAS_HEADERS)) {
          dst.setHasHeaders(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_HAS_FOOTERS)) {
          dst.setHasFooters(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SHOW_COLUMN_WIDTHS)) {
          dst.setShowColumnWidths(BeeUtils.toBooleanOrNull(value));
        } else if (BeeUtils.same(key, ATTR_ASYNC_THRESHOLD)) {
          dst.setAsyncThreshold(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_PAGING_THRESHOLD)) {
          dst.setPagingThreshold(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_SEARCH_THRESHOLD)) {
          dst.setSearchThreshold(BeeUtils.toIntOrNull(value));
        } else if (BeeUtils.same(key, ATTR_NEW_ROW_COLUMNS)) {
          dst.setNewRowColumns(value.trim());
        }
      }
    }
    
    GridComponent header = getComponent(src, TAG_HEADER);
    if (header != null) {
      dst.setHeader(header);
    }
    GridComponent body = getComponent(src, TAG_BODY);
    if (body != null) {
      dst.setBody(body);
    }
    GridComponent footer = getComponent(src, TAG_FOOTER);
    if (footer != null) {
      dst.setFooter(footer);
    }
    
    NodeList rowStyleNodes = src.getElementsByTagName(TAG_ROW_STYLE);
    if (rowStyleNodes != null && rowStyleNodes.getLength() > 0) {
      List<ConditionalStyle> rowStyles = Lists.newArrayList();
      for (int i = 0; i < rowStyleNodes.getLength(); i++) {
        ConditionalStyle cs = XmlUtils.getConditionalStyle((Element) rowStyleNodes.item(i));
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
  }
}
