package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
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
    ASYNC_THRESHOLD, PAGING_THRESHOLD, SEARCH_THRESHOLD, PAGE_SIZE,
    NEW_ROW_COLUMNS, SHOW_COLUMN_WIDTHS,
    HEADER, BODY, FOOTER,
    ROW_STYLES, ROW_MESSAGE, ROW_EDITABLE,
    MIN_COLUMN_WIDTH, MAX_COLUMN_WIDTH, COLUMNS
  }

  public static BeeGrid restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    BeeGrid grid = new BeeGrid();
    grid.deserialize(s);
    return grid;
  }

  private String name;
  private String viewName;
  private String caption = null;
  private Boolean readOnly = null;

  private Boolean hasHeaders = null;
  private Boolean hasFooters = null;
  
  private Integer asyncThreshold = null;
  private Integer pagingThreshold = null;
  private Integer searchThreshold = null;
  
  private Integer pageSize = null;

  private String newRowColumns = null;
  private Boolean showColumnWidths = null;

  private GridComponent header = null;
  private GridComponent body = null;
  private GridComponent footer = null;

  private Collection<ConditionalStyle> rowStyles = null;

  private Calculation rowMessage = null;
  private Calculation rowEditable = null;

  private Integer minColumnWidth = null;
  private Integer maxColumnWidth = null;

  private final Map<String, GridColumn> columns = Maps.newLinkedHashMap();

  public BeeGrid(String name, String viewName) {
    Assert.notEmpty(name);
    Assert.notEmpty(viewName);

    this.name = name;
    this.viewName = viewName;
  }

  private BeeGrid() {
  }

  public void addColumn(GridColumn column) {
    Assert.notNull(column);
    Assert.state(!hasColumn(column.getName()),
        BeeUtils.concat(1, "Dublicate column name:", getName(), column.getName()));

    getColumns().put(columnKey(column.getName()), column);
  }

  @Override
  public void deserialize(String s) {
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
          setReadOnly(BeeUtils.toBooleanOrNull(value));
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
          setHasFooters(BeeUtils.toBooleanOrNull(value));
          break;
        case HAS_HEADERS:
          setHasHeaders(BeeUtils.toBooleanOrNull(value));
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
        case PAGE_SIZE:
          setPageSize(BeeUtils.toIntOrNull(value));
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
          setShowColumnWidths(BeeUtils.toBooleanOrNull(value));
          break;
      }
    }
  }

  public Integer getAsyncThreshold() {
    return asyncThreshold;
  }

  public GridComponent getBody() {
    return body;
  }

  public String getCaption() {
    return caption;
  }

  public int getColumnCount() {
    return getColumns().size();
  }

  public Map<String, GridColumn> getColumns() {
    return columns;
  }

  public GridComponent getFooter() {
    return footer;
  }

  public GridComponent getHeader() {
    return header;
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();

    PropertyUtils.addProperties(info, false,
        "Name", getName(),
        "View Name", getViewName(),
        "Caption", getCaption(),
        "Read Only", isReadOnly(),
        "Has Headers", hasHeaders(),
        "Has Footers", hasFooters(),
        "Async Threshold", getAsyncThreshold(),
        "Paging Threshold", getPagingThreshold(),
        "Search Threshold", getSearchThreshold(),
        "Page Size", getPageSize(),
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

  public Integer getMaxColumnWidth() {
    return maxColumnWidth;
  }

  public Integer getMinColumnWidth() {
    return minColumnWidth;
  }

  public String getName() {
    return name;
  }

  public String getNewRowColumns() {
    return newRowColumns;
  }

  public Integer getPageSize() {
    return pageSize;
  }

  public Integer getPagingThreshold() {
    return pagingThreshold;
  }

  public Calculation getRowEditable() {
    return rowEditable;
  }

  public Calculation getRowMessage() {
    return rowMessage;
  }

  public Collection<ConditionalStyle> getRowStyles() {
    return rowStyles;
  }

  public Integer getSearchThreshold() {
    return searchThreshold;
  }

  public boolean hasColumn(String colName) {
    return getColumns().containsKey(columnKey(colName));
  }

  public Boolean hasFooters() {
    return hasFooters;
  }

  public Boolean hasHeaders() {
    return hasHeaders;
  }

  public boolean isEmpty() {
    return getColumnCount() <= 0;
  }

  public Boolean isReadOnly() {
    return readOnly;
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
        case PAGE_SIZE:
          arr[i++] = getPageSize();
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

  public void setAsyncThreshold(Integer asyncThreshold) {
    this.asyncThreshold = asyncThreshold;
  }

  public void setBody(GridComponent body) {
    this.body = body;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setFooter(GridComponent footer) {
    this.footer = footer;
  }

  public void setHasFooters(Boolean hasFooters) {
    this.hasFooters = hasFooters;
  }

  public void setHasHeaders(Boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  public void setHeader(GridComponent header) {
    this.header = header;
  }

  public void setMaxColumnWidth(Integer maxColumnWidth) {
    this.maxColumnWidth = maxColumnWidth;
  }

  public void setMinColumnWidth(Integer minColumnWidth) {
    this.minColumnWidth = minColumnWidth;
  }

  public void setNewRowColumns(String newRowColumns) {
    this.newRowColumns = newRowColumns;
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize;
  }

  public void setPagingThreshold(Integer pagingThreshold) {
    this.pagingThreshold = pagingThreshold;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRowEditable(Calculation rowEditable) {
    this.rowEditable = rowEditable;
  }

  public void setRowMessage(Calculation rowMessage) {
    this.rowMessage = rowMessage;
  }

  public void setRowStyles(Collection<ConditionalStyle> rowStyles) {
    this.rowStyles = rowStyles;
  }

  public void setSearchThreshold(Integer searchThreshold) {
    this.searchThreshold = searchThreshold;
  }

  public void setShowColumnWidths(Boolean showColumnWidths) {
    this.showColumnWidths = showColumnWidths;
  }

  public Boolean showColumnWidths() {
    return showColumnWidths;
  }

  private String columnKey(String colName) {
    Assert.notEmpty(colName);
    return colName.trim().toLowerCase();
  }

  private String getViewName() {
    return viewName;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }
}
