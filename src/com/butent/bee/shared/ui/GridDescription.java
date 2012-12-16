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
import com.butent.bee.shared.utils.ArrayUtils;
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
    NAME, PARENT, CAPTION, VIEW, ID_NAME, VERSION_NAME, FILTER, ORDER, HEADER_MODE, HAS_FOOTERS,
    ASYNC_THRESHOLD, PAGING_THRESHOLD, SEARCH_THRESHOLD, INITIAL_ROW_SET_SIZE, READONLY,
    NEW_ROW_FORM, NEW_ROW_COLUMNS, NEW_ROW_DEFAULTS, NEW_ROW_CAPTION, NEW_ROW_POPUP,
    EDIT_FORM, EDIT_MODE, EDIT_SAVE, EDIT_MESSAGE, EDIT_SHOW_ID, EDIT_IN_PLACE, EDIT_POPUP,
    ENABLED_ACTIONS, DISABLED_ACTIONS, STYLE_SHEETS, HEADER, BODY, FOOTER,
    ROW_STYLES, ROW_MESSAGE, ROW_EDITABLE, ROW_VALIDATION,
    SHOW_COLUMN_WIDTHS, MIN_COLUMN_WIDTH, MAX_COLUMN_WIDTH,
    COLUMNS, WIDGETS, AUTO_FIT, FLEXIBILITY, FAVORITE, CACHE_DATA, CACHE_DESCRIPTION,
    MIN_NUMBER_OF_ROWS, MAX_NUMBER_OF_ROWS, RENDER_MODE, ROW_CHANGE_SENSITIVITY_MILLIS
  }
  
  public static final String HEADER_MODE_ALL = "all";
  public static final String HEADER_MODE_COLUMN = "column";
  public static final String HEADER_MODE_GRID = "grid";
  public static final String HEADER_MODE_NONE = "none";

  public static GridDescription restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    GridDescription grid = new GridDescription();
    grid.deserialize(s);
    return grid;
  }

  private String name = null;
  private String parent = null;
  private String caption = null;

  private String viewName = null;
  private String idName = null;
  private String versionName = null;

  private Filter filter = null;
  private Order order = null;

  private String headerMode = null;
  private Boolean hasFooters = null;

  private Boolean cacheData = null;
  private Boolean cacheDescription = null;

  private Integer asyncThreshold = null;
  private Integer pagingThreshold = null;
  private Integer searchThreshold = null;

  private Integer initialRowSetSize = null;

  private Boolean readOnly = null;

  private String newRowForm = null;
  private String newRowColumns = null;
  private String newRowDefaults = null;
  private String newRowCaption = null;
  private Boolean newRowPopup = null;

  private String editForm = null;
  private Boolean editMode = null;
  private Boolean editSave = null;
  private Calculation editMessage = null;
  private Boolean editShowId = null;
  private String editInPlace = null;
  private Boolean editPopup = null;

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
  private String autoFit = null;
  private Flexibility flexibility = null;

  private final List<ColumnDescription> columns = Lists.newArrayList();

  private Set<Action> enabledActions = Sets.newHashSet();
  private Set<Action> disabledActions = Sets.newHashSet();

  private String favorite = null;
  
  private Integer minNumberOfRows = null;
  private Integer maxNumberOfRows = null;
  
  private String renderMode = null;

  private Integer rowChangeSensitivityMillis = null;
  
  private List<String> widgets = Lists.newArrayList();

  public GridDescription(String name) {
    this(name, null);
  }
  
  public GridDescription(String name, String viewName) {
    super();
    this.name = name;
    this.viewName = viewName;
  }

  public GridDescription(String name, String viewName, String idName, String versionName) {
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
        BeeUtils.joinWords("Dublicate column name:", getName(), column.getName()));

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

          if (!ArrayUtils.isEmpty(items)) {
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
        case HEADER_MODE:
          setHeaderMode(value);
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
        case NEW_ROW_DEFAULTS:
          setNewRowDefaults(value);
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

          if (ArrayUtils.isEmpty(styles)) {
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
        case EDIT_MODE:
          setEditMode(BeeUtils.toBooleanOrNull(value));
          break;
        case EDIT_SAVE:
          setEditSave(BeeUtils.toBooleanOrNull(value));
          break;
        case EDIT_MESSAGE:
          setRowMessage(Calculation.restore(value));
          break;
        case EDIT_SHOW_ID:
          setEditShowId(BeeUtils.toBooleanOrNull(value));
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
        case CACHE_DATA:
          setCacheData(BeeUtils.toBooleanOrNull(value));
          break;
        case CACHE_DESCRIPTION:
          setCacheDescription(BeeUtils.toBooleanOrNull(value));
          break;

        case ENABLED_ACTIONS:
          getEnabledActions().clear();
          items = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(items)) {
            for (String z : items) {
              getEnabledActions().add(Action.restore(z));
            }
          }
          break;
        case DISABLED_ACTIONS:
          getDisabledActions().clear();
          items = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(items)) {
            for (String z : items) {
              getDisabledActions().add(Action.restore(z));
            }
          }
          break;

        case STYLE_SHEETS:
          String[] css = Codec.beeDeserializeCollection(value);

          if (ArrayUtils.isEmpty(css)) {
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

          if (!ArrayUtils.isEmpty(items)) {
            for (String z : items) {
              getWidgets().add(z);
            }
          }
          break;

        case NEW_ROW_POPUP:
          setNewRowPopup(BeeUtils.toBooleanOrNull(value));
          break;
        case EDIT_POPUP:
          setEditPopup(BeeUtils.toBooleanOrNull(value));
          break;
        case PARENT:
          setParent(value);
          break;
        case AUTO_FIT:
          setAutoFit(value);
          break;
        case FLEXIBILITY:
          setFlexibility(Flexibility.restore(value));
          break;
        case FAVORITE:
          setFavorite(value);
          break;
        case MIN_NUMBER_OF_ROWS:
          setMinNumberOfRows(BeeUtils.toIntOrNull(value));
          break;
        case MAX_NUMBER_OF_ROWS:
          setMaxNumberOfRows(BeeUtils.toIntOrNull(value));
          break;
        case RENDER_MODE:
          setRenderMode(value);
          break;
        case ROW_CHANGE_SENSITIVITY_MILLIS:
          setRowChangeSensitivityMillis(BeeUtils.toIntOrNull(value));
          break;
      }
    }
  }

  public Integer getAsyncThreshold() {
    return asyncThreshold;
  }

  public String getAutoFit() {
    return autoFit;
  }

  public GridComponentDescription getBody() {
    return body;
  }

  public Boolean getCacheData() {
    return cacheData;
  }

  public Boolean getCacheDescription() {
    return cacheDescription;
  }

  public CachingPolicy getCachingPolicy(boolean def) {
    return BeeUtils.nvl(getCacheData(), def) ? CachingPolicy.FULL : CachingPolicy.NONE;
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

  public Calculation getEditMessage() {
    return editMessage;
  }

  public Boolean getEditMode() {
    return editMode;
  }

  public Boolean getEditPopup() {
    return editPopup;
  }

  public Boolean getEditSave() {
    return editSave;
  }

  public Boolean getEditShowId() {
    return editShowId;
  }

  public Set<Action> getEnabledActions() {
    return enabledActions;
  }

  public List<ExtendedProperty> getExtendedInfo() {
    List<ExtendedProperty> info = Lists.newArrayList();

    PropertyUtils.addProperties(info, false,
        "Name", getName(),
        "Parent", getParent(),
        "Caption", getCaption(),
        "View Name", getViewName(),
        "Id Name", getIdName(),
        "Version Name", getVersionName(),
        "Filter", getFilter(),
        "Order", getOrder(),
        "Header Mode", getHeaderMode(),
        "Has Footers", hasFooters(),
        "Cache Data", getCacheData(),
        "Cache Description", getCacheDescription(),
        "Async Threshold", getAsyncThreshold(),
        "Paging Threshold", getPagingThreshold(),
        "Search Threshold", getSearchThreshold(),
        "Initial Row Set Size", getInitialRowSetSize(),
        "Read Only", isReadOnly(),
        "New Row Form", getNewRowForm(),
        "New Row Columns", getNewRowColumns(),
        "New Row Defaults", getNewRowDefaults(),
        "New Row Caption", getNewRowCaption(),
        "New Row Popup", getNewRowPopup(),
        "Edit Form", getEditForm(),
        "Edit Mode", getEditMode(),
        "Edit Save", getEditSave(),
        "Edit Show Id", getEditShowId(),
        "Edit In Place", getEditInPlace(),
        "Edit Popup", getEditPopup(),
        "Enabled Actions", getEnabledActions(),
        "Disabled Actions", getDisabledActions(),
        "Show Column Widths", showColumnWidths(),
        "Min Column Width", getMinColumnWidth(),
        "Max Column Width", getMaxColumnWidth(),
        "Auto Fit", getAutoFit(),
        "Favorite", getFavorite(),
        "Min Number Of Rows", getMinNumberOfRows(),
        "Max Number Of Rows", getMaxNumberOfRows(),
        "Render Mode", getRenderMode(),
        "Row Change Sensitivity Millis", getRowChangeSensitivityMillis());
    
    if (getFlexibility() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Flexibility", getFlexibility().getInfo());
    }

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

    if (getEditMessage() != null) {
      PropertyUtils.appendChildrenToExtended(info, "Edit Message", getEditMessage().getInfo());
    }

    int cc = getColumnCount();
    PropertyUtils.addExtended(info, "Column Count", BeeUtils.bracket(cc));

    int i = 0;
    for (ColumnDescription column : getColumns()) {
      i++;
      PropertyUtils.appendChildrenToExtended(info,
          BeeUtils.joinWords("Column", BeeUtils.progress(i, cc), column.getName()),
          column.getInfo());
    }
    return info;
  }

  public String getFavorite() {
    return favorite;
  }

  public Filter getFilter() {
    return filter;
  }

  public Flexibility getFlexibility() {
    return flexibility;
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

  public Integer getInitialRowSetSize() {
    return initialRowSetSize;
  }

  public Integer getMaxColumnWidth() {
    return maxColumnWidth;
  }

  public Integer getMaxNumberOfRows() {
    return maxNumberOfRows;
  }

  public Integer getMinColumnWidth() {
    return minColumnWidth;
  }

  public Integer getMinNumberOfRows() {
    return minNumberOfRows;
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

  public String getNewRowDefaults() {
    return newRowDefaults;
  }

  public String getNewRowForm() {
    return newRowForm;
  }

  public Boolean getNewRowPopup() {
    return newRowPopup;
  }

  public Order getOrder() {
    return order;
  }

  public Integer getPagingThreshold() {
    return pagingThreshold;
  }

  public String getParent() {
    return parent;
  }

  public String getRenderMode() {
    return renderMode;
  }

  public Integer getRowChangeSensitivityMillis() {
    return rowChangeSensitivityMillis;
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

  public boolean hasColumnHeaders() {
    return BeeUtils.isEmpty(getHeaderMode())
        || BeeUtils.inListSame(getHeaderMode(), HEADER_MODE_ALL, HEADER_MODE_COLUMN);
  }

  public Boolean hasFooters() {
    return hasFooters;
  }

  public boolean hasGridHeader() {
    return BeeUtils.isEmpty(getHeaderMode())
        || BeeUtils.inListSame(getHeaderMode(), HEADER_MODE_ALL, HEADER_MODE_GRID);
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
        case HEADER_MODE:
          arr[i++] = getHeaderMode();
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
        case NEW_ROW_DEFAULTS:
          arr[i++] = getNewRowDefaults();
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
        case EDIT_MODE:
          arr[i++] = getEditMode();
          break;
        case EDIT_SAVE:
          arr[i++] = getEditSave();
          break;
        case EDIT_MESSAGE:
          arr[i++] = getEditMessage();
          break;
        case EDIT_SHOW_ID:
          arr[i++] = getEditShowId();
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
        case CACHE_DATA:
          arr[i++] = getCacheData();
          break;
        case CACHE_DESCRIPTION:
          arr[i++] = getCacheDescription();
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
        case NEW_ROW_POPUP:
          arr[i++] = getNewRowPopup();
          break;
        case EDIT_POPUP:
          arr[i++] = getEditPopup();
          break;
        case PARENT:
          arr[i++] = getParent();
          break;
        case AUTO_FIT:
          arr[i++] = getAutoFit();
          break;
        case FLEXIBILITY:
          arr[i++] = getFlexibility();
          break;
        case FAVORITE:
          arr[i++] = getFavorite();
          break;
        case MIN_NUMBER_OF_ROWS:
          arr[i++] = getMinNumberOfRows();
          break;
        case MAX_NUMBER_OF_ROWS:
          arr[i++] = getMaxNumberOfRows();
          break;
        case RENDER_MODE:
          arr[i++] = getRenderMode();
          break;
        case ROW_CHANGE_SENSITIVITY_MILLIS:
          arr[i++] = getRowChangeSensitivityMillis();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAsyncThreshold(Integer asyncThreshold) {
    this.asyncThreshold = asyncThreshold;
  }

  public void setAutoFit(String autoFit) {
    this.autoFit = autoFit;
  }

  public void setBody(GridComponentDescription body) {
    this.body = body;
  }

  public void setCacheData(Boolean cacheData) {
    this.cacheData = cacheData;
  }

  public void setCacheDescription(Boolean cacheDescription) {
    this.cacheDescription = cacheDescription;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public void setDefaults() {
    setHasFooters(true);

    setCacheData(true);
    setCacheDescription(true);

    setSearchThreshold(DataUtils.getDefaultSearchThreshold());
    setPagingThreshold(DataUtils.getDefaultPagingThreshold());
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

  public void setEditMessage(Calculation editMessage) {
    this.editMessage = editMessage;
  }

  public void setEditMode(Boolean editMode) {
    this.editMode = editMode;
  }

  public void setEditPopup(Boolean editPopup) {
    this.editPopup = editPopup;
  }

  public void setEditSave(Boolean editSave) {
    this.editSave = editSave;
  }

  public void setEditShowId(Boolean editShowId) {
    this.editShowId = editShowId;
  }

  public void setEnabledActions(Set<Action> enabledActions) {
    this.enabledActions = enabledActions;
  }

  public void setFavorite(String favorite) {
    this.favorite = favorite;
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setFlexibility(Flexibility flexibility) {
    this.flexibility = flexibility;
  }

  public void setFooter(GridComponentDescription footer) {
    this.footer = footer;
  }

  public void setHasFooters(Boolean hasFooters) {
    this.hasFooters = hasFooters;
  }

  public void setHeader(GridComponentDescription header) {
    this.header = header;
  }

  public void setHeaderMode(String headerMode) {
    this.headerMode = headerMode;
  }
  
  public void setInitialRowSetSize(Integer initialRowSetSize) {
    this.initialRowSetSize = initialRowSetSize;
  }

  public void setMaxColumnWidth(Integer maxColumnWidth) {
    this.maxColumnWidth = maxColumnWidth;
  }

  public void setMaxNumberOfRows(Integer maxNumberOfRows) {
    this.maxNumberOfRows = maxNumberOfRows;
  }

  public void setMinColumnWidth(Integer minColumnWidth) {
    this.minColumnWidth = minColumnWidth;
  }
  
  public void setMinNumberOfRows(Integer minNumberOfRows) {
    this.minNumberOfRows = minNumberOfRows;
  }

  public void setNewRowCaption(String newRowCaption) {
    this.newRowCaption = newRowCaption;
  }

  public void setNewRowColumns(String newRowColumns) {
    this.newRowColumns = newRowColumns;
  }

  public void setNewRowDefaults(String newRowDefaults) {
    this.newRowDefaults = newRowDefaults;
  }

  public void setNewRowForm(String newRowForm) {
    this.newRowForm = newRowForm;
  }

  public void setNewRowPopup(Boolean newRowPopup) {
    this.newRowPopup = newRowPopup;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void setPagingThreshold(Integer pagingThreshold) {
    this.pagingThreshold = pagingThreshold;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
  }

  public void setRenderMode(String renderMode) {
    this.renderMode = renderMode;
  }

  public void setRowChangeSensitivityMillis(Integer rowChangeSensitivityMillis) {
    this.rowChangeSensitivityMillis = rowChangeSensitivityMillis;
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

  private String getHeaderMode() {
    return headerMode;
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
