package com.butent.bee.shared.ui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.HasInfo;
import com.butent.bee.shared.HasItems;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.HasViewName;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.ViewColumn;
import com.butent.bee.shared.menu.MenuConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Relation implements BeeSerializable, HasInfo, HasViewName {

  public enum Caching {
    NONE, QUERY, LOCAL, GLOBAL
  }

  private enum Serial {
    ATTRIBUTES, SELECTOR_COLUMNS, ROW_RENDERER_DESCR, ROW_RENDER
  }

  public static final String TAG_ROW_RENDERER = "rowRenderer";
  public static final String TAG_ROW_RENDER = "rowRender";

  public static final String TAG_SELECTOR_COLUMN = "selectorColumn";

  public static final String ATTR_FILTER = "filter";
  public static final String ATTR_ORDER = "order";

  public static final String ATTR_CACHING = "caching";
  public static final String ATTR_OPERATOR = "operator";

  public static final String ATTR_CHOICE_COLUMNS = "choiceColumns";
  public static final String ATTR_SEARCHABLE_COLUMNS = "searchableColumns";

  public static final String ATTR_ITEM_TYPE = "itemType";

  private static final Predicate<String> RELEVANT_ATTRIBUTE =
      Predicates.in(Sets.newHashSet(UiConstants.ATTR_VIEW_NAME, ATTR_FILTER, ATTR_ORDER,
          ATTR_CACHING, ATTR_OPERATOR, ATTR_CHOICE_COLUMNS, ATTR_SEARCHABLE_COLUMNS,
          ATTR_ITEM_TYPE, HasVisibleLines.ATTR_VISIBLE_LINES, HasItems.ATTR_ITEM_KEY));

  public static Relation create(Map<String, String> attributes,
      List<SelectorColumn> selectorColumns,  RendererDescription rowRendererDescription,
      Calculation rowRender) {
    Relation relation = new Relation();
    relation.setAttributes(attributes);

    if (!BeeUtils.isEmpty(selectorColumns)) {
      relation.setSelectorColumns(selectorColumns);
    }

    if (rowRendererDescription != null) {
      relation.setRowRendererDescription(rowRendererDescription);
    }
    if (rowRender != null) {
      relation.setRowRender(rowRender);
    }
    
    return relation;
  }
  
  public static Relation create(String viewName, List<String> columns) {
    Relation relation = new Relation();

    if (!BeeUtils.isEmpty(viewName)) {
      relation.setViewName(viewName);
    }
    if (!BeeUtils.isEmpty(columns)) {
      relation.getChoiceColumns().addAll(columns);
      relation.getSearchableColumns().addAll(columns);
    }
    
    return relation;
  }

  public static Relation restore(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    }
    Relation relation = new Relation();
    relation.deserialize(s);
    return relation;
  }

  private final Map<String, String> attributes = Maps.newHashMap();

  private String viewName = null;

  private Filter filter = null;
  private Order order = null;

  private Caching caching = null;
  private Operator operator = null;

  private RendererDescription rowRendererDescription = null;
  private Calculation rowRender = null;

  private String itemKey = null;
  
  private final List<SelectorColumn> selectorColumns = Lists.newArrayList();

  private final List<String> choiceColumns = Lists.newArrayList();
  private final List<String> searchableColumns = Lists.newArrayList();

  private MenuConstants.ITEM_TYPE itemType = null;
  private Integer visibleLines = null;
  
  private String originalSource = null;
  private final List<String> originalRenderColumns = Lists.newArrayList();

  private Relation() {
  }

  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];
      if (BeeUtils.isEmpty(value)) {
        continue;
      }

      switch (member) {
        case ATTRIBUTES:
          String[] attr = Codec.beeDeserializeCollection(value);
          if (!BeeUtils.isEmpty(attr)) {
            getAttributes().clear();
            for (int j = 0; j < attr.length; j += 2) {
              getAttributes().put(attr[j], attr[j + 1]);
            }
          }
          break;

        case SELECTOR_COLUMNS:
          String[] cols = Codec.beeDeserializeCollection(value);
          if (!BeeUtils.isEmpty(cols)) {
            getSelectorColumns().clear();
            for (int j = 0; j < cols.length; j++) {
              BeeUtils.addNotNull(getSelectorColumns(), SelectorColumn.restore(cols[j]));
            }
          }
          break;

        case ROW_RENDERER_DESCR:
          setRowRendererDescription(RendererDescription.restore(value));
          break;

        case ROW_RENDER:
          setRowRender(Calculation.restore(value));
          break;
      }
    }
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public Caching getCaching() {
    return caching;
  }

  public List<String> getChoiceColumns() {
    return choiceColumns;
  }

  public Filter getFilter() {
    return filter;
  }

  public List<Property> getInfo() {
    List<Property> info = PropertyUtils.createProperties(getAttributes());
    PropertyUtils.addProperties(info,
        "View Name", getViewName(),
        "Filter", getFilter(),
        "Order", getOrder(),
        "Caching", getCaching(),
        "Operator", getOperator(),
        "Item Type", getItemType(),
        "Visible Lines", getVisibleLines(),
        "Item Key", getItemKey());

    if (!getChoiceColumns().isEmpty()) {
      PropertyUtils.addProperties(info, "Choice Columns", getChoiceColumns());
    }
    if (!getSearchableColumns().isEmpty()) {
      PropertyUtils.addProperties(info, "Searchable Columns", getSearchableColumns());
    }

    if (!getSelectorColumns().isEmpty()) {
      PropertyUtils.appendWithIndex(info, "Selector Columns", null, getSelectorColumns());
    }

    if (getRowRendererDescription() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Row Renderer",
          getRowRendererDescription().getInfo());
    }
    if (getRowRender() != null) {
      PropertyUtils.appendChildrenToProperties(info, "Row Render", getRowRender().getInfo());
    }
    
    return info;
  }

  public String getItemKey() {
    return itemKey;
  }

  public MenuConstants.ITEM_TYPE getItemType() {
    return itemType;
  }

  public Operator getOperator() {
    return operator;
  }

  public Order getOrder() {
    return order;
  }

  public List<String> getOriginalRenderColumns() {
    return Lists.newArrayList(originalRenderColumns);
  }

  public String getOriginalSource() {
    return originalSource;
  }

  public Calculation getRowRender() {
    return rowRender;
  }

  public RendererDescription getRowRendererDescription() {
    return rowRendererDescription;
  }

  public List<String> getSearchableColumns() {
    return searchableColumns;
  }
  public List<SelectorColumn> getSelectorColumns() {
    return selectorColumns;
  }

  public String getViewName() {
    return viewName;
  }

  public Integer getVisibleLines() {
    return visibleLines;
  }

  public boolean hasRowRenderer() {
    return getRowRendererDescription() != null || getRowRender() != null 
        || !BeeUtils.isEmpty(getItemKey());
  }

  public void initialize(DataInfo.Provider provider, String sourceView, Holder<String> source,
      Holder<List<String>> renderColumns) {
    
    setOriginalSource(source.get());
    setOriginalRenderColumns(renderColumns.get());

    String relView = getAttribute(UiConstants.ATTR_VIEW_NAME);
    if (!BeeUtils.isEmpty(relView)) {
      setViewName(relView);
    }

    String cache = getAttribute(ATTR_CACHING);
    if (!BeeUtils.isEmpty(cache)) {
      setCaching(NameUtils.getConstant(Caching.class, cache));
    }
    String op = getAttribute(ATTR_OPERATOR);
    if (!BeeUtils.isEmpty(op)) {
      setOperator(NameUtils.getConstant(Operator.class, op));
    }

    String it = getAttribute(ATTR_ITEM_TYPE);
    if (!BeeUtils.isEmpty(it)) {
      setItemType(NameUtils.getConstant(MenuConstants.ITEM_TYPE.class, it));
    }
    String lines = getAttribute(HasVisibleLines.ATTR_VISIBLE_LINES);
    if (BeeUtils.isPositiveInt(lines)) {
      setVisibleLines(BeeUtils.toInt(lines));
    }

    String key = getAttribute(HasItems.ATTR_ITEM_KEY);
    if (!BeeUtils.isEmpty(key)) {
      setItemKey(key);
    }
    
    String flt = getAttribute(ATTR_FILTER);
    String ord = getAttribute(ATTR_ORDER);

    List<String> displCols = NameUtils.toList(getAttribute(ATTR_CHOICE_COLUMNS));
    List<String> searchCols = NameUtils.toList(getAttribute(ATTR_SEARCHABLE_COLUMNS));
    
    List<String> selectorColumnNames = getSelectorColumnNames();

    DataInfo sourceInfo = BeeUtils.isEmpty(sourceView)
        ? null : provider.getDataInfo(sourceView, true);

    if (BeeUtils.isEmpty(getViewName()) && sourceInfo != null) {
      relView = deduceViewName(provider, sourceInfo, source.get(), renderColumns.get(),
          displCols, searchCols, selectorColumnNames);
      if (!BeeUtils.isEmpty(relView)) {
        setViewName(relView);
      }
    }

    if (sourceInfo != null) {
      String target = resolveSource(sourceInfo, source.get(), renderColumns.get());
      if (BeeUtils.isEmpty(renderColumns.get())) {
        renderColumns.set(deriveRenderColumns(sourceInfo, source.get(), target));
      }
      if (!BeeUtils.isEmpty(target)) {
        source.set(target);
      }
    }

    DataInfo viewInfo = BeeUtils.isEmpty(getViewName())
        ? null : provider.getDataInfo(getViewName(), true);

    if (viewInfo != null && !BeeUtils.isEmpty(flt)) {
      setFilter(DataUtils.parseCondition(flt, viewInfo.getColumns(),
          viewInfo.getIdColumn(), viewInfo.getVersionColumn()));
    }
    if (viewInfo != null && !BeeUtils.isEmpty(ord)) {
      setOrder(Order.parse(ord, viewInfo.getColumnNames()));
    }

    if (!BeeUtils.isEmpty(displCols)) {
      if (viewInfo == null) {
        setChoiceColumns(displCols);
      } else {
        setChoiceColumns(viewInfo.parseColumns(displCols));
      }
    }

    if (!BeeUtils.isEmpty(searchCols)) {
      if (viewInfo == null) {
        setSearchableColumns(searchCols);
      } else {
        setSearchableColumns(viewInfo.parseColumns(searchCols));
      }
    }
    
    if (getChoiceColumns().isEmpty() && !selectorColumnNames.isEmpty()) {
      if (viewInfo == null) {
        setChoiceColumns(selectorColumnNames);
      } else {
        setChoiceColumns(viewInfo.parseColumns(selectorColumnNames));
      }
    }

    if (getChoiceColumns().isEmpty() && getSearchableColumns().isEmpty()) {
      List<String> fields = Lists.newArrayList();

      if (sourceInfo != null && !BeeUtils.isEmpty(renderColumns.get())) {
        for (String columnId : renderColumns.get()) {
          ViewColumn vc = sourceInfo.getViewColumn(columnId);
          if (vc != null) {
            fields.add(vc.getField());
          }
        }
      }

      if (viewInfo != null) {
        if (!fields.isEmpty()) {
          List<String> columns = viewInfo.parseColumns(fields);
          fields.clear();
          if (!BeeUtils.isEmpty(columns)) {
            fields.addAll(columns);
          }
        }

        if (fields.isEmpty()) {
          for (BeeColumn column : viewInfo.getColumns()) {
            if (ValueType.TEXT.equals(column.getType()) && column.getPrecision() <= 100) {
              fields.add(column.getId());
            }
          }
        }

        if (fields.isEmpty()) {
          fields.addAll(DataUtils.getColumnNames(viewInfo.getColumns()));
        }
      }

      setChoiceColumns(fields);
      setSearchableColumns(fields);

    } else if (getChoiceColumns().isEmpty()) {
      setChoiceColumns(getSearchableColumns());
    } else if (getSearchableColumns().isEmpty()) {
      setSearchableColumns(getChoiceColumns());
    }
  }

  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ATTRIBUTES:
          arr[i++] = getAttributes();
          break;
        case SELECTOR_COLUMNS:
          arr[i++] = getSelectorColumns();
          break;
        case ROW_RENDERER_DESCR:
          arr[i++] = getRowRendererDescription();
          break;
        case ROW_RENDER:
          arr[i++] = getRowRender();
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setAttributes(Map<String, String> attributes) {
    getAttributes().clear();
    if (BeeUtils.isEmpty(attributes)) {
      return;
    }
    getAttributes().putAll(Maps.filterKeys(attributes, RELEVANT_ATTRIBUTE));
  }

  public void setCaching(Caching caching) {
    this.caching = caching;
  }

  public void setChoiceColumns(List<String> choiceColumns) {
    getChoiceColumns().clear();
    if (!BeeUtils.isEmpty(choiceColumns)) {
      getChoiceColumns().addAll(choiceColumns);
    }
  }

  public void setFilter(Filter filter) {
    this.filter = filter;
  }

  public void setItemKey(String itemKey) {
    this.itemKey = itemKey;
  }

  public void setItemType(MenuConstants.ITEM_TYPE itemType) {
    this.itemType = itemType;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public void setRowRender(Calculation rowRender) {
    this.rowRender = rowRender;
  }
  
  public void setRowRendererDescription(RendererDescription rowRendererDescription) {
    this.rowRendererDescription = rowRendererDescription;
  }

  public void setSearchableColumns(List<String> searchableColumns) {
    getSearchableColumns().clear();
    if (!BeeUtils.isEmpty(searchableColumns)) {
      getSearchableColumns().addAll(searchableColumns);
    }
  }

  public void setSelectorColumns(List<SelectorColumn> selectorColumns) {
    getSelectorColumns().clear();
    if (selectorColumns != null) {
      getSelectorColumns().addAll(selectorColumns);
    }
  }

  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  public void setVisibleLines(Integer visibleLines) {
    this.visibleLines = visibleLines;
  }

  private String deduceViewName(DataInfo dataInfo, List<String> columns) {
    if (!BeeUtils.isEmpty(columns)) {
      for (String colName : columns) {
        String result = deduceViewName(dataInfo, colName);
        if (!BeeUtils.isEmpty(result)) {
          return result;
        }
      }
    }
    return null;
  }

  private String deduceViewName(DataInfo sourceInfo, String colName) {
    return sourceInfo.getRelationView(colName);
  }

  private String deduceViewName(DataInfo.Provider provider, DataInfo sourceInfo,
      String source, List<String> renderColumns, List<String> displCols, List<String> searchCols,
      List<String> selectorColumnNames) {
    String result = null;

    if (BeeUtils.allEmpty(source, renderColumns, displCols, searchCols, selectorColumnNames)) {
      for (String colName : sourceInfo.getColumnNames()) {
        result = deduceViewName(sourceInfo, colName);
        if (!BeeUtils.isEmpty(result)) {
          break;
        }
      }
      return result;
    }

    if (!BeeUtils.isEmpty(source)) {
      result = deduceViewName(sourceInfo, source);
      return result;
    }

    result = deduceViewName(sourceInfo, renderColumns);
    if (!BeeUtils.isEmpty(result)) {
      return result;
    }

    Set<String> columns = BeeUtils.union(displCols, searchCols, selectorColumnNames);
    if (columns.isEmpty()) {
      return result;
    }

    List<String> tables = sourceInfo.getRelatedTables();
    if (tables.isEmpty()) {
      return result;
    }

    for (String table : tables) {
      DataInfo tableInfo = provider.getDataInfo(table, false);
      if (tableInfo != null && tableInfo.containsAllViewColumns(columns)) {
        result = table;
        break;
      }
    }
    return result;
  }

  private List<String> deriveRenderColumns(DataInfo sourceInfo, String source, String target) {
    List<String> result = Lists.newArrayList();

    if (sourceInfo.containsColumn(source)) {
      ViewColumn vc = sourceInfo.getViewColumn(source);
      if (vc != null && vc.getLevel() > 0) {
        result.add(source);
        return result;
      }
    }

    if (!BeeUtils.isEmpty(target)) {
      Collection<ViewColumn> descendants = sourceInfo.getDescendants(target, false);

      if (!descendants.isEmpty()) {
        List<Integer> columnIndexes = Lists.newArrayList();
        for (ViewColumn vc : descendants) {
          int index = sourceInfo.getColumnIndex(vc.getName());
          if (!BeeConst.isUndef(index)) {
            columnIndexes.add(index);
          }
        }

        if (columnIndexes.size() > 1) {
          Collections.sort(columnIndexes);
        }
        for (int index : columnIndexes) {
          result.add(sourceInfo.getColumnId(index));
        }
      }
    }

    return result;
  }

  private String getAttribute(String name) {
    return getAttributes().get(name);
  }

  private List<String> getSelectorColumnNames() {
    List<String> result = Lists.newArrayList();
    for (SelectorColumn selectorColumn : getSelectorColumns()) {
      BeeUtils.addNotEmpty(result, selectorColumn.getSource());
    }
    return result;
  }

  private String resolveSource(DataInfo sourceInfo, String colName) {
    return sourceInfo.getRelationSource(colName);
  }

  private String resolveSource(DataInfo sourceInfo, String source, List<String> renderColumns) {
    String result = null;

    if (!BeeUtils.isEmpty(source)) {
      result = resolveSource(sourceInfo, source);
      if (!BeeUtils.isEmpty(result)) {
        return result;
      }
    }

    if (!BeeUtils.isEmpty(renderColumns)) {
      for (String colName : renderColumns) {
        result = resolveSource(sourceInfo, colName);
        if (!BeeUtils.isEmpty(result)) {
          break;
        }
      }
    }
    return result;
  }

  private void setOriginalRenderColumns(List<String> originalRenderColumns) {
    if (!this.originalRenderColumns.isEmpty()) {
      this.originalRenderColumns.clear();
    }
    if (!BeeUtils.isEmpty(originalRenderColumns)) {
      this.originalRenderColumns.addAll(originalRenderColumns);
    }
  }
  
  private void setOriginalSource(String originalSource) {
    this.originalSource = originalSource;
  }
}
