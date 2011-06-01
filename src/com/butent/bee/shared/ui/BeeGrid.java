package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.GridColumn.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implementation of a grid user interface component.
 */

public class BeeGrid implements BeeSerializable, HasExtendedInfo {

  /**
   * Contains a list of grid parameters.
   */

  private enum SerializationMember {
    NAME, VIEW, CAPTION, READONLY, HAS_HEADERS, HAS_FOOTERS,
    ASYNC_THRESHOLD, PAGING_THRESHOLD, SEARCH_THRESHOLD,
    NEW_ROW_COLUMNS, SHOW_COLUMN_WIDTHS,
    HEADER, BODY, FOOTER,
    ROW_STYLES, ROW_MESSAGE, ROW_EDITABLE,
    MIN_COLUMN_WIDTH, MAX_COLUMN_WIDTH, COLUMNS
  }

  public static BeeGrid restore(String s) {
    BeeGrid grid = new BeeGrid();
    grid.deserialize(s);
    return grid;
  }

  private String name;
  private String viewName;
  private String caption = null;
  private boolean readOnly = false;

  private boolean hasHeaders = true;
  private boolean hasFooters = true;
  private Integer asyncThreshold = null;
  private Integer pagingThreshold = null;
  private Integer searchThreshold = null;
  private String newRowColumns = null;
  private boolean showColumnWidths = true;
  
  private GridComponent header = null;
  private GridComponent body = null;
  private GridComponent footer = null;
  
  private Collection<ConditionalStyle> rowStyles = null;
  
  private Calculation rowMessage = null;
  private Calculation rowEditable = null;

  private Integer minColumnWidth = null;
  private Integer maxColumnWidth = null;
  
  private final Map<String, GridColumn> columns = Maps.newLinkedHashMap();

  public BeeGrid(String name, String viewName, String caption, boolean readOnly) {
    Assert.notEmpty(name);
    Assert.notEmpty(viewName);

    this.name = name;
    this.viewName = viewName;
    this.caption = caption;
    this.readOnly = readOnly;
  }

  private BeeGrid() {
  }

  public void addCalculatedColumn(String colName, String colCaption, Integer width,
      ValueType type, String expr, String func) {
    Assert.notEmpty(expr);
    addColumn(ColType.CALCULATED, colName, colCaption, true, width)
        .setCalc(new Calculation(type, expr, func));
  }

  public void addDataColumn(String colName, String colCaption, boolean isReadOnly, Integer width,
      String source) {
    Assert.notEmpty(source);
    addColumn(ColType.DATA, colName, colCaption, isReadOnly, width)
        .setSource(source);
  }

  public void addIdColumn(String colName, String colCaption, Integer width) {
    addColumn(ColType.ID, colName, colCaption, true, width);
  }

  public void addRelatedColumn(String colName, String colCaption, boolean isReadOnly, Integer width,
      String source, String relSource, String relation) {
    Assert.notEmpty(source);
    Assert.notEmpty(relation);

    GridColumn column = addColumn(ColType.RELATED, colName, colCaption, isReadOnly, width);
    column.setSource(source);
    column.setRelSource(relSource);
    column.setRelation(relation);
  }

  public void addVersionColumn(String colName, String colCaption, Integer width) {
    addColumn(ColType.VERSION, colName, colCaption, true, width);
  }

  @Override
  public void deserialize(String s) {
    Assert.isTrue(isEmpty());

    SerializationMember[] members = SerializationMember.values();
    String[] arr = Codec.beeDeserialize(s);
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      SerializationMember member = members[i];
      String value = arr[i];

      switch (member) {
        case NAME:
          setName(value);
          break;
        case VIEW:
          setViewName(value);
          break;
        case CAPTION:
          setCaption(value);
          break;
        case READONLY:
          setReadOnly(BeeUtils.toBoolean(value));
          break;
        case COLUMNS:
          getColumns().clear();
          if (!BeeUtils.isEmpty(value)) {
            for (String z : Codec.beeDeserialize(value)) {
              addColumn(GridColumn.restore(z));
            }
          }
          break;
        case ASYNC_THRESHOLD:
          setAsyncThreshold(BeeUtils.toIntOrNull(value));
          break;
        case BODY:
          setBody(GridComponent.restore(value));
          break;
        case FOOTER:
          setFooter(GridComponent.restore(value));
          break;
        case HAS_FOOTERS:
          setHasFooters(BeeUtils.toBoolean(value));
          break;
        case HAS_HEADERS:
          setHasHeaders(BeeUtils.toBoolean(value));
          break;
        case HEADER:
          setHeader(GridComponent.restore(value));
          break;
        case MAX_COLUMN_WIDTH:
          setMaxColumnWidth(BeeUtils.toIntOrNull(value));
          break;
        case MIN_COLUMN_WIDTH:
          setMinColumnWidth(BeeUtils.toIntOrNull(value));
          break;
        case NEW_ROW_COLUMNS:
          setNewRowColumns(value);
          break;
        case PAGING_THRESHOLD:
          setPagingThreshold(BeeUtils.toIntOrNull(value));
          break;
        case ROW_EDITABLE:
          setRowEditable(Calculation.restore(value));
          break;
        case ROW_MESSAGE:
          setRowMessage(Calculation.restore(value));
          break;
        case ROW_STYLES:
          if (BeeUtils.isEmpty(value)) {
            setRowStyles(null);
          } else {
            List<ConditionalStyle> lst = Lists.newArrayList();
            for (String cs : Codec.beeDeserialize(value)) {
              lst.add(ConditionalStyle.restore(cs));
            }
            setRowStyles(lst);
          }
          break;
        case SEARCH_THRESHOLD:
          setSearchThreshold(BeeUtils.toIntOrNull(value));
          break;
        case SHOW_COLUMN_WIDTHS:
          setShowColumnWidths(BeeUtils.toBoolean(value));
          break;
      }
    }
  }

  public int getColumnCount() {
    return getColumns().size();
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList(); 
      
    PropertyUtils.addProperties(info, false,
        "Name", getName(),
        "View Name", getViewName(),
        "Caption",  getCaption(),
        "Read Only", isReadOnly(),
        "Has Headers", hasHeaders(),
        "Has Footers", hasFooters(),
        "Async Threshold", getAsyncThreshold(),
        "Paging Threshold", getPagingThreshold(),
        "Search Threshold", getSearchThreshold(),
        "New Row Columns", getNewRowColumns(),
        "Show Column Widths", showColumnWidths(),
        "Min Column Width", getMinColumnWidth(),
        "Max Column Width", getMaxColumnWidth());
    
    if (getHeader() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Header", getHeader().getInfo());
    }
    if (getBody() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Body", getBody().getInfo());
    }
    if (getFooter() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Footer", getFooter().getInfo());
    }
    
    if (getRowStyles() != null && !getRowStyles().isEmpty()) {
      int cnt = getRowStyles().size();
      PropertyUtils.addExtended(info, "Row Styles", BeeUtils.bracket(cnt));
      int i = 0;
      for (ConditionalStyle cs : getRowStyles()) {
        i++;
        if (cs != null) {
          PropertyUtils.appendChildrenToExtended(info, "Row Style " + BeeUtils.progress(i, cnt),
              cs.getInfo());
        }
      }
    }

    if (getRowMessage() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Row Message", getRowMessage().getInfo());
    }
    if (getRowEditable() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Row Editable", getRowEditable().getInfo());
    }
    
    int cc = getColumnCount();
    PropertyUtils.addExtended(info, "Column Count", BeeUtils.bracket(cc));
    
    int i = 0;
    for (GridColumn column : getColumns().values()) {
      i++;
      PropertyUtils.appendChildrenToExtended(info, "Column " + BeeUtils.progress(i, cc),
          column.getInfo());
    }
    return info;
  }

  public String getName() {
    return name;
  }

  public boolean hasColumn(String colName) {
    return getColumns().containsKey(columnKey(colName));
  }

  public boolean isEmpty() {
    return getColumnCount() <= 0;
  }

  @Override
  public String serialize() {
    SerializationMember[] members = SerializationMember.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (SerializationMember member : members) {
      switch (member) {
        case NAME:
          arr[i++] = getName();
          break;
        case VIEW:
          arr[i++] = getViewName();
          break;
        case CAPTION:
          arr[i++] = getCaption();
          break;
        case READONLY:
          arr[i++] = isReadOnly();
          break;
        case COLUMNS:
          arr[i++] = getColumns().values();
          break;
        case ASYNC_THRESHOLD:
          arr[i++] = getAsyncThreshold();
          break;
        case BODY:
          arr[i++] = getBody();
          break;
        case FOOTER:
          arr[i++] = getFooter();
          break;
        case HAS_FOOTERS:
          arr[i++] = hasFooters();
          break;
        case HAS_HEADERS:
          arr[i++] = hasHeaders();
          break;
        case HEADER:
          arr[i++] = getHeader();
          break;
        case MAX_COLUMN_WIDTH:
          arr[i++] = getMaxColumnWidth();
          break;
        case MIN_COLUMN_WIDTH:
          arr[i++] = getMinColumnWidth();
          break;
        case NEW_ROW_COLUMNS:
          arr[i++] = getNewRowColumns();
          break;
        case PAGING_THRESHOLD:
          arr[i++] = getPagingThreshold();
          break;
        case ROW_EDITABLE:
          arr[i++] = getRowEditable();
          break;
        case ROW_MESSAGE:
          arr[i++] = getRowMessage();
          break;
        case ROW_STYLES:
          arr[i++] = getRowStyles();
          break;
        case SEARCH_THRESHOLD:
          arr[i++] = getSearchThreshold();
          break;
        case SHOW_COLUMN_WIDTHS:
          arr[i++] = showColumnWidths(); 
          break;
      }
    }
    return Codec.beeSerializeAll(arr);
  }

  private GridColumn addColumn(ColType type, String colName, String colCaption, boolean isReadOnly,
      Integer width) {
    GridColumn col = new GridColumn(type, colName, colCaption, isReadOnly, width);
    addColumn(col);
    return col;
  }
  
  private void addColumn(GridColumn column) {
    Assert.notNull(column);
    Assert.state(!hasColumn(column.getName()),
        BeeUtils.concat(1, "Dublicate column name:", getName(), column.getName()));

    getColumns().put(columnKey(column.getName()), column);
  }

  private String columnKey(String colName) {
    Assert.notEmpty(colName);
    return colName.trim().toLowerCase();
  }

  private Integer getAsyncThreshold() {
    return asyncThreshold;
  }

  private GridComponent getBody() {
    return body;
  }

  private String getCaption() {
    return caption;
  }

  private Map<String, GridColumn> getColumns() {
    return columns;
  }

  private GridComponent getFooter() {
    return footer;
  }

  private GridComponent getHeader() {
    return header;
  }

  private Integer getMaxColumnWidth() {
    return maxColumnWidth;
  }

  private Integer getMinColumnWidth() {
    return minColumnWidth;
  }

  private String getNewRowColumns() {
    return newRowColumns;
  }

  private Integer getPagingThreshold() {
    return pagingThreshold;
  }

  private Calculation getRowEditable() {
    return rowEditable;
  }

  private Calculation getRowMessage() {
    return rowMessage;
  }

  private Collection<ConditionalStyle> getRowStyles() {
    return rowStyles;
  }

  private Integer getSearchThreshold() {
    return searchThreshold;
  }

  private String getViewName() {
    return viewName;
  }

  private boolean hasFooters() {
    return hasFooters;
  }

  private boolean hasHeaders() {
    return hasHeaders;
  }

  private boolean isReadOnly() {
    return readOnly;
  }

  private void setAsyncThreshold(Integer asyncThreshold) {
    this.asyncThreshold = asyncThreshold;
  }

  private void setBody(GridComponent body) {
    this.body = body;
  }

  private void setCaption(String caption) {
    this.caption = caption;
  }

  private void setFooter(GridComponent footer) {
    this.footer = footer;
  }

  private void setHasFooters(boolean hasFooters) {
    this.hasFooters = hasFooters;
  }

  private void setHasHeaders(boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  private void setHeader(GridComponent header) {
    this.header = header;
  }

  private void setMaxColumnWidth(Integer maxColumnWidth) {
    this.maxColumnWidth = maxColumnWidth;
  }

  private void setMinColumnWidth(Integer minColumnWidth) {
    this.minColumnWidth = minColumnWidth;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setNewRowColumns(String newRowColumns) {
    this.newRowColumns = newRowColumns;
  }

  private void setPagingThreshold(Integer pagingThreshold) {
    this.pagingThreshold = pagingThreshold;
  }

  private void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  private void setRowEditable(Calculation rowEditable) {
    this.rowEditable = rowEditable;
  }

  private void setRowMessage(Calculation rowMessage) {
    this.rowMessage = rowMessage;
  }

  private void setRowStyles(Collection<ConditionalStyle> rowStyles) {
    this.rowStyles = rowStyles;
  }

  private void setSearchThreshold(Integer searchThreshold) {
    this.searchThreshold = searchThreshold;
  }

  private void setShowColumnWidths(boolean showColumnWidths) {
    this.showColumnWidths = showColumnWidths;
  }

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }

  private boolean showColumnWidths() {
    return showColumnWidths;
  }

}
