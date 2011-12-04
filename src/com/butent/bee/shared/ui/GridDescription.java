package com.butent.bee.shared.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a grid user interface component.
 */

public class GridDescription implements BeeSerializable, HasExtendedInfo, HasViewName {

  /**
   * Contains a list of grid parameters.
   */

  private enum Serial {
    NAME, CAPTION, VIEW, ID_NAME, VERSION_NAME, FILTER, ORDER, HAS_HEADERS, HAS_FOOTERS,
    CACHING, ASYNC_THRESHOLD, PAGING_THRESHOLD, SEARCH_THRESHOLD, INITIAL_ROW_SET_SIZE,
    READONLY, NEW_ROW_FORM, NEW_ROW_COLUMNS, NEW_ROW_CAPTION, EDIT_FORM, EDIT_IN_PLACE,
    ENABLED_ACTIONS, DISABLED_ACTIONS, STYLE_SHEETS, HEADER, BODY, FOOTER, ROW_STYLES, ROW_MESSAGE,
    ROW_EDITABLE, ROW_VALIDATION, SHOW_COLUMN_WIDTHS, MIN_COLUMN_WIDTH, MAX_COLUMN_WIDTH,
    COLUMNS, WIDGETS
  }

  public static GridDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    GridDescription grid = new GridDescription();
    grid.deserialize(s);
    return grid;
  }

  private String name;
  private String caption = null;

  private String viewName;
  private String idName;
  private String versionName;

  private Filter filter = null;
  private Order order = null;

  private Boolean hasHeaders = null;
  private Boolean hasFooters = null;

  private Boolean caching = null;

  private Integer asyncThreshold = null;
  private Integer pagingThreshold = null;
  private Integer searchThreshold = null;

  private Integer initialRowSetSize = null;

  private Boolean readOnly = null;

  private String newRowForm = null;
  private String newRowColumns = null;
  private String newRowCaption = null;
  private String editForm = null;
  private String editInPlace = null;
  
  private Map<String, String> styleSheets = null;

  private GridComponentDescription header = null;
  private GridComponentDescription body = null;
  private GridComponentDescription footer = null;

  private Collection<ConditionalStyleDeclaration> rowStyles = null;

  private Calculation rowMessage = null;
  private Calculation rowEditable = null;
  private Calculation rowValidation = null;

  private Boolean showColumnWidths = null;
  private Integer minColumnWidth = null;
  private Integer maxColumnWidth = null;

  private final List<ColumnDescription> columns = Lists.newArrayList();

  private Set<Action> enabledActions = Sets.newHashSet();
  private Set<Action> disabledActions = Sets.newHashSet();
  
  private List<String> widgets = Lists.newArrayList();

  public GridDescription(String name, String viewName, String idName, String versionName) {
    Assert.notEmpty(name);
    Assert.notEmpty(viewName);

    this.name = name;
    this.viewName = viewName;
    this.idName = idName;
    this.versionName = versionName;
  }

  private GridDescription() {
  }

  public void addColumn(ColumnDescription column) {
    Assert.notNull(column);
    Assert.state(!hasColumn(column.getName()),
        BeeUtils.concat(1, "Dublicate column name:", getName(), column.getName()));

    getColumns().add(column);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);
    
    String[] items;

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
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
          items = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(items)) {
            for (String z : items) {
              addColumn(ColumnDescription.restore(z));
            }
          }
          break;

        case ASYNC_THRESHOLD:
          setAsyncThreshold(BeeUtils.toIntOrNull(value));
          break;
        case BODY:
          setBody(GridComponentDescription.restore(value));
          break;
        case FOOTER:
          setFooter(GridComponentDescription.restore(value));
          break;
        case HAS_FOOTERS:
          setHasFooters(BeeUtils.toBooleanOrNull(value));
          break;
        case HAS_HEADERS:
          setHasHeaders(BeeUtils.toBooleanOrNull(value));
          break;
        case HEADER:
          setHeader(GridComponentDescription.restore(value));
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
        case NEW_ROW_CAPTION:
          setNewRowCaption(value);
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
        case ROW_VALIDATION:
          setRowValidation(Calculation.restore(value));
          break;

        case ROW_STYLES:
          String[] styles = Codec.beeDeserializeCollection(value);

          if (BeeUtils.isEmpty(styles)) {
            setRowStyles(null);
          } else {
            List<ConditionalStyleDeclaration> lst = Lists.newArrayList();
            for (String cs : styles) {
              lst.add(ConditionalStyleDeclaration.restore(cs));
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
        case NEW_ROW_FORM:
          setNewRowForm(value);
          break;
        case EDIT_FORM:
          setEditForm(value);
          break;
        case EDIT_IN_PLACE:
          setEditInPlace(value);
          break;
        case INITIAL_ROW_SET_SIZE:
          setInitialRowSetSize(BeeUtils.toIntOrNull(value));
          break;
        case FILTER:
          setFilter(Filter.restore(value));
          break;
        case ORDER:
          setOrder(Order.restore(value));
          break;
        case ID_NAME:
          setIdName(value);
          break;
        case VERSION_NAME:
          setVersionName(value);
          break;
        case CACHING:
          setCaching(BeeUtils.toBooleanOrNull(value));
          break;

        case ENABLED_ACTIONS:
          getEnabledActions().clear();
          items = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(items)) {
            for (String z : items) {
              getEnabledActions().add(Action.restore(z));
            }
          }
          break;
        case DISABLED_ACTIONS:
          getDisabledActions().clear();
          items = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(items)) {
            for (String z : items) {
              getDisabledActions().add(Action.restore(z));
            }
          }
          break;

        case STYLE_SHEETS:
          String[] css = Codec.beeDeserializeCollection(value);

          if (BeeUtils.isEmpty(css)) {
            setStyleSheets(null);
          } else {
            Map<String, String> map = Maps.newHashMap();
            for (int j = 0; j < css.length - 1; j += 2) {
              map.put(css[j], css[j + 1]);
            }
            setStyleSheets(map);
          }
          break;

        case WIDGETS:
          getWidgets().clear();
          items = Codec.beeDeserializeCollection(value);

          if (!BeeUtils.isEmpty(items)) {
            for (String z : items) {
              getWidgets().add(z);
            }
          }
          break;
      }
    }
  }

  public Integer getAsyncThreshold() {
    return asyncThreshold;
  }

  public GridComponentDescription getBody() {
    return body;
  }

  public CachingPolicy getCachingPolicy() {
    return BeeUtils.isTrue(getCaching()) ? CachingPolicy.FULL : CachingPolicy.NONE;
  }

  public String getCaption() {
    return caption;
  }

  public int getColumnCount() {
    return getColumns().size();
  }

  public List<ColumnDescription> getColumns() {
    return columns;
  }

  public Set<Action> getDisabledActions() {
    return disabledActions;
  }

  public String getEditForm() {
    return editForm;
  }

  public String getEditInPlace() {
    return editInPlace;
  }

  public Set<Action> getEnabledActions() {
    return enabledActions;
  }

  public Filter getFilter() {
    return filter;
  }

  public GridComponentDescription getFooter() {
    return footer;
  }

  public GridComponentDescription getHeader() {
    return header;
  }

  public String getIdName() {
    return idName;
  }

  public List<ExtendedProperty> getInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();

    PropertyUtils.addProperties(info, false,
        "Name", getName(),
        "Caption", getCaption(),
        "View Name", getViewName(),
        "Id Name", getIdName(),
        "Version Name", getVersionName(),
        "Filter", getFilter(),
        "Order", getOrder(),
        "Has Headers", hasHeaders(),
        "Has Footers", hasFooters(),
        "Caching", getCaching(),
        "Async Threshold", getAsyncThreshold(),
        "Paging Threshold", getPagingThreshold(),
        "Search Threshold", getSearchThreshold(),
        "Initial Row Set Size", getInitialRowSetSize(),
        "Read Only", isReadOnly(),
        "New Row Form", getNewRowForm(),
        "New Row Columns", getNewRowColumns(),
        "New Row Caption", getNewRowCaption(),
        "Edit Form", getEditForm(),
        "Edit In Place", getEditInPlace(),
        "Enabled Actions", getEnabledActions(),
        "Disabled Actions", getDisabledActions(),
        "Show Column Widths", showColumnWidths(),
        "Min Column Width", getMinColumnWidth(),
        "Max Column Width", getMaxColumnWidth());
    
    if (getStyleSheets() != null && !getStyleSheets().isEmpty()) {
      int cnt = getStyleSheets().size();
      PropertyUtils.addExtended(info, "Style Sheets", BeeUtils.bracket(cnt));
      int i = 0;
      for (Map.Entry<String, String> entry : getStyleSheets().entrySet()) {
        i++;
        PropertyUtils.addExtended(info, "Style Sheet " + BeeUtils.progress(i, cnt),
            entry.getKey(), entry.getValue());
      }
    }

    if (getWidgets() != null && !getWidgets().isEmpty()) {
      int cnt = getWidgets().size();
      PropertyUtils.addExtended(info, "Widgets", BeeUtils.bracket(cnt));
      int i = 0;
      for (String w : getWidgets()) {
        PropertyUtils.addExtended(info, "Widget", BeeUtils.progress(++i, cnt), w);
      }
    }
    
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
      for (ConditionalStyleDeclaration cs : getRowStyles()) {
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
    if (getRowValidation() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Row Validation", getRowValidation().getInfo());
    }

    int cc = getColumnCount();
    PropertyUtils.addExtended(info, "Column Count", BeeUtils.bracket(cc));

    int i = 0;
    for (ColumnDescription column : getColumns()) {
      i++;
      PropertyUtils.appendChildrenToExtended(info,
          BeeUtils.concat(1, "Column", BeeUtils.progress(i, cc), column.getName()),
          column.getInfo());
    }
    return info;
  }

  public Integer getInitialRowSetSize() {
    return initialRowSetSize;
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

  public String getNewRowCaption() {
    return newRowCaption;
  }

  public String getNewRowColumns() {
    return newRowColumns;
  }

  public String getNewRowForm() {
    return newRowForm;
  }

  public Order getOrder() {
    return order;
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

  public Collection<ConditionalStyleDeclaration> getRowStyles() {
    return rowStyles;
  }

  public Calculation getRowValidation() {
    return rowValidation;
  }

  public Integer getSearchThreshold() {
    return searchThreshold;
  }

  public Map<String, String> getStyleSheets() {
    return styleSheets;
  }

  public String getVersionName() {
    return versionName;
  }

  public String getViewName() {
    return viewName;
  }

  public List<ColumnDescription> getVisibleColumns() {
    List<ColumnDescription> result = Lists.newArrayList();
    for (ColumnDescription column : getColumns()) {
      if (column.isVisible()) {
        result.add(column);
      }
    }
    return result;
  }

  public List<String> getWidgets() {
    return widgets;
  }

  public boolean hasColumn(String colName) {
    Assert.notNull(colName);
    for (ColumnDescription column : getColumns()) {
      if (BeeUtils.same(column.getName(), colName)) {
        return true;
      }
    }
    return false;
  }

  public Boolean hasFooters() {
    return hasFooters;
  }

  public Boolean hasHeaders() {
    return hasHeaders;
  }
  
  public boolean hasWidgets() {
    return getWidgets() != null && !getWidgets().isEmpty();
  }

  public boolean isEmpty() {
    return getColumnCount() <= 0;
  }

  public Boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
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
          arr[i++] = getColumns();
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
        case NEW_ROW_CAPTION:
          arr[i++] = getNewRowCaption();
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
        case ROW_VALIDATION:
          arr[i++] = getRowValidation();
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
        case NEW_ROW_FORM:
          arr[i++] = getNewRowForm();
          break;
        case EDIT_FORM:
          arr[i++] = getEditForm();
          break;
        case EDIT_IN_PLACE:
          arr[i++] = getEditInPlace();
          break;
        case INITIAL_ROW_SET_SIZE:
          arr[i++] = getInitialRowSetSize();
          break;
        case FILTER:
          arr[i++] = getFilter();
          break;
        case ORDER:
          arr[i++] = getOrder();
          break;
        case ID_NAME:
          arr[i++] = getIdName();
          break;
        case VERSION_NAME:
          arr[i++] = getVersionName();
          break;
        case CACHING:
          arr[i++] = getCaching();
          break;
        case ENABLED_ACTIONS:
          arr[i++] = getEnabledActions();
          break;
        case DISABLED_ACTIONS:
          arr[i++] = getDisabledActions();
          break;
        case STYLE_SHEETS:
          arr[i++] = getStyleSheets();
          break;
        case WIDGETS:
          arr[i++] = getWidgets();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAsyncThreshold(Integer asyncThreshold) {
    this.asyncThreshold = asyncThreshold;
  }

  public void setBody(GridComponentDescription body) {
    this.body = body;
  }

  public void setCaching(Boolean caching) {
    this.caching = caching;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setDefaults() {
    setHasHeaders(true);
    setHasFooters(true);

    setCaching(true);

    setAsyncThreshold(DataUtils.getDefaultAsyncThreshold());
    setSearchThreshold(DataUtils.getDefaultSearchThreshold());
    setPagingThreshold(DataUtils.getDefaultPagingThreshold());

    setInitialRowSetSize(DataUtils.getMaxInitialRowSetSize());
  }

  public void setDisabledActions(Set<Action> disabledActions) {
    this.disabledActions = disabledActions;
  }

  public void setEditForm(String editForm) {
    this.editForm = editForm;
  }

  public void setEditInPlace(String editInPlace) {
    this.editInPlace = editInPlace;
  }

  public void setEnabledActions(Set<Action> enabledActions) {
    this.enabledActions = enabledActions;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setFooter(GridComponentDescription footer) {
    this.footer = footer;
  }

  public void setHasFooters(Boolean hasFooters) {
    this.hasFooters = hasFooters;
  }

  public void setHasHeaders(Boolean hasHeaders) {
    this.hasHeaders = hasHeaders;
  }

  public void setHeader(GridComponentDescription header) {
    this.header = header;
  }

  public void setInitialRowSetSize(Integer initialRowSetSize) {
    this.initialRowSetSize = initialRowSetSize;
  }

  public void setMaxColumnWidth(Integer maxColumnWidth) {
    this.maxColumnWidth = maxColumnWidth;
  }

  public void setMinColumnWidth(Integer minColumnWidth) {
    this.minColumnWidth = minColumnWidth;
  }

  public void setNewRowCaption(String newRowCaption) {
    this.newRowCaption = newRowCaption;
  }

  public void setNewRowColumns(String newRowColumns) {
    this.newRowColumns = newRowColumns;
  }

  public void setNewRowForm(String newRowForm) {
    this.newRowForm = newRowForm;
  }

  public void setOrder(Order order) {
    this.order = order;
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

  public void setRowStyles(Collection<ConditionalStyleDeclaration> rowStyles) {
    this.rowStyles = rowStyles;
  }

  public void setRowValidation(Calculation rowValidation) {
    this.rowValidation = rowValidation;
  }

  public void setSearchThreshold(Integer searchThreshold) {
    this.searchThreshold = searchThreshold;
  }

  public void setShowColumnWidths(Boolean showColumnWidths) {
    this.showColumnWidths = showColumnWidths;
  }

  public void setStyleSheets(Map<String, String> styleSheets) {
    this.styleSheets = styleSheets;
  }

  public void setWidgets(List<String> widgets) {
    this.widgets = widgets;
  }

  public Boolean showColumnWidths() {
    return showColumnWidths;
  }

  private Boolean getCaching() {
    return caching;
  }

  private void setIdName(String idName) {
    this.idName = idName;
  }

  private void setName(String name) {
    this.name = name;
  }

  private void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  private void setViewName(String viewName) {
    this.viewName = viewName;
  }
}
