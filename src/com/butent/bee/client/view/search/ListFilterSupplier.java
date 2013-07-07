package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ListFilterSupplier extends AbstractFilterSupplier {

  private final List<String> renderColumns = Lists.newArrayList();
  private final List<String> orderColumns = Lists.newArrayList();

  private final boolean foreign;

  private final int valueIndex;
  private final int countIndex;

  private final int renderCount;

  private SimpleRowSet data;

  private final List<String> values = Lists.newArrayList();

  public ListFilterSupplier(String viewName, BeeColumn sourceColumn, BeeColumn filterColumn,
      String label, List<String> renderColumns, List<String> orderColumns, Relation relation,
      String options) {
    super(viewName, (relation == null) ? filterColumn : sourceColumn, label, options);

    if (relation == null) {
      this.renderColumns.add(filterColumn.getId());
      this.orderColumns.add(filterColumn.getId());

      this.foreign = false;

      this.valueIndex = 0;
      this.countIndex = 1;

    } else {
      if (BeeUtils.isEmpty(options)) {
        this.renderColumns.addAll(renderColumns);
      } else {
        this.renderColumns.addAll(NameUtils.toList(options));
      }

      this.orderColumns.addAll(orderColumns);

      this.foreign = true;

      this.valueIndex = this.renderColumns.size();
      this.countIndex = this.valueIndex + 1;
    }

    this.renderCount = this.renderColumns.size();
  }

  @Override
  public void ensureData() {
    if (getData() == null || getEffectiveFilter() != null) {
      setEffectiveFilter(null);
      getHistogram(new Callback<SimpleRowSet>() {
        @Override
        public void onSuccess(SimpleRowSet result) {
          setData(result);
        }
      });
    }
  }

  @Override
  public FilterValue getFilterValue() {
    if (values.isEmpty()) {
      return null;

    } else {
      JSONArray arr = new JSONArray();

      for (int i = 0; i < values.size(); i++) {
        String value = values.get(i);

        if (value == null) {
          arr.set(i, JSONNull.getInstance());
        } else {
          arr.set(i, new JSONString(value));
        }
      }

      return FilterValue.of(arr.toString());
    }
  }

  @Override
  public String getLabel() {
    if (values.isEmpty()) {
      return null;

    } else if (!isForeign() || DataUtils.isEmpty(getData())) {
      return BeeUtils.join(BeeConst.STRING_COMMA, values);

    } else {
      List<String> labels = Lists.newArrayList();
      Set<String> ids = Sets.newHashSet(values);

      for (SimpleRow row : data) {
        String value = row.getValue(valueIndex);
        if (ids.remove(value)) {
          labels.add(getCaption(row));
        }
      }

      if (!ids.isEmpty()) {
        labels.addAll(ids);
      }

      return BeeUtils.join(BeeConst.STRING_COMMA, labels);
    }
  }

  @Override
  public void onRequest(final Element target, final Scheduler.ScheduledCommand onChange) {
    getHistogram(new Callback<SimpleRowSet>() {
      @Override
      public void onSuccess(SimpleRowSet result) {
        setData(result);

        if (result.getNumberOfRows() <= 0) {
          notifyInfo(messageAllEmpty(null));

        } else if (result.getNumberOfRows() == 1) {
          SimpleRow row = result.getRow(0);
          notifyInfo(messageOneValue(getCaption(row), row.getValue(countIndex)));

        } else {
          clearSelection();
          openDialog(target, createWidget(), onChange);
        }
      }
    });
  }

  @Override
  public Filter parse(FilterValue input) {
    if (input != null && input.hasValue()) {
      return buildFilter(JsonUtils.toList(JSONParser.parseStrict(input.getValue())));
    } else {
      return null;
    }
  }

  @Override
  public void setFilterValue(FilterValue filterValue) {
    values.clear();
    if (filterValue != null && filterValue.hasValue()) {
      values.addAll(JsonUtils.toList(JSONParser.parseStrict(filterValue.getValue())));
    }
  }

  @Override
  protected void doClear() {
    clearDisplay();
    super.doClear();
  }
  
  @Override
  protected void doCommit() {
    if (isSelectionEmpty()) {
      boolean changed = !values.isEmpty();
      values.clear();
      update(changed);

    } else {
      List<String> newValues = Lists.newArrayList();
      for (int row : getSelectedItems()) {
        newValues.add(data.getValue(row, valueIndex));
      }

      boolean changed = !BeeUtils.sameElements(values, newValues);

      BeeUtils.overwrite(values, newValues);
      update(changed);
    }
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR, SupplierAction.CANCEL);
  }

  @Override
  protected List<String> getHistogramColumns() {
    List<String> columns = Lists.newArrayList(renderColumns);
    if (isForeign()) {
      columns.add(getColumnId());
    }
    return columns;
  }

  @Override
  protected List<String> getHistogramOrder() {
    return orderColumns;
  }

  private Filter buildFilter(Collection<String> input) {
    if (BeeUtils.isEmpty(input)) {
      return null;
    }

    List<Filter> filters = Lists.newArrayList();
    String columnId = getColumnId();

    for (String value : input) {
      if (value == null || value.isEmpty()) {
        filters.add(Filter.isEmpty(columnId));

      } else if (isForeign()) {
        filters.add(ComparisonFilter.isEqual(columnId, 
            new LongValue(BeeUtils.toLongOrNull(value))));

      } else {
        filters.add(ComparisonFilter.isEqual(columnId, 
            Value.parseValue(getColumnType(), value, false)));
      }
    }
    
    return Filter.or(filters);
  }

  private Widget createWidget() {
    HtmlTable display = createDisplay(true);

    int row = 0;
    for (String[] dataItem : getData().getRows()) {
      int col = 0;

      if (dataItem[valueIndex] == null) {
        display.setText(row, col, NULL_VALUE_LABEL);
        col = renderCount;

      } else {
        for (int i = 0; i < renderCount; i++) {
          display.setText(row, col++, dataItem[i]);
        }
      }

      addBinSize(display, row, col, dataItem[countIndex]);
      row++;
    }

    return wrapDisplay(display, true);
  }

  private String getCaption(SimpleRow row) {
    if (isNull(row)) {
      return NULL_VALUE_LABEL;

    } else if (renderCount == 1) {
      return row.getValue(0);

    } else {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < renderCount; i++) {
        String value = row.getValue(i);
        if (value != null) {
          if (sb.length() > 0) {
            sb.append(BeeConst.CHAR_SPACE);
          }
          sb.append(value);
        }
      }
      return sb.toString();
    }
  }

  private SimpleRowSet getData() {
    return data;
  }

  private boolean isForeign() {
    return foreign;
  }

  private boolean isNull(SimpleRow row) {
    return row.getValue(valueIndex) == null;
  }

  private void setData(SimpleRowSet data) {
    this.data = data;
  }
}
