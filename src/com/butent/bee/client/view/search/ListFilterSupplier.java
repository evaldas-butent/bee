package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.utils.JsonUtils;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.List;

public class ListFilterSupplier extends AbstractFilterSupplier {

  private final List<String> renderColumns = Lists.newArrayList();
  private final List<String> orderColumns = Lists.newArrayList();

  private final int renderCount;
  private final int relIndex;
  private final int countIndex;

  private SimpleRowSet data = null;

  public ListFilterSupplier(String viewName, BeeColumn column, List<String> renderColumns,
      List<String> orderColumns, Relation relation, String options) {
    super(viewName, column, options);

    if (relation == null) {
      this.renderColumns.add(column.getId());
      this.orderColumns.add(column.getId());

      this.relIndex = BeeConst.UNDEF;
      this.countIndex = 1;

    } else {
      if (BeeUtils.isEmpty(options)) {
        this.renderColumns.addAll(renderColumns);
      } else {
        this.renderColumns.addAll(NameUtils.toList(options));
      }
      
      this.orderColumns.addAll(orderColumns);

      this.relIndex = this.renderColumns.size();
      this.countIndex = this.relIndex + 1;
    }

    this.renderCount = this.renderColumns.size();
  }

  @Override
  public String getDisplayHtml() {
    List<String> values = Lists.newArrayList();

    for (int row : getSelectedItems()) {
      values.add(getCaption(row));
    }
    return BeeUtils.join(BeeConst.STRING_COMMA, values);
  }

  @Override
  public void onRequest(final Element target, final NotificationListener notificationListener,
      final Callback<Boolean> callback) {
    getHistogram(new Callback<SimpleRowSet>() {
      @Override
      public void onFailure(String... reason) {
        super.onFailure(reason);
        callback.onFailure(reason);
      }

      @Override
      public void onSuccess(SimpleRowSet result) {
        setData(result);

        if (result.getNumberOfRows() <= 0) {
          notificationListener.notifyInfo(messageAllEmpty(null));
          callback.onSuccess(reset());

        } else if (result.getNumberOfRows() == 1) {
          notificationListener.notifyInfo(messageOneValue(getCaption(0), getCount(0)));
          callback.onSuccess(reset());

        } else {
          clearSelection();
          openDialog(target, createWidget(), callback);
        }
      }
    });
  }

  @Override
  public Filter parse(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    } else {
      return buildFilter(JsonUtils.toList(JSONParser.parseStrict(value)));
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
      update(null);
      return;
    }

    List<String> values = Lists.newArrayList();

    int colIndex = (relIndex > 0) ? relIndex : 0;
    for (int row : getSelectedItems()) {
      values.add(data.getValue(row, colIndex));
    }

    update(buildFilter(values));
  }
  
  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR);
  }

  @Override
  protected List<String> getHistogramColumns() {
    List<String> columns = Lists.newArrayList(renderColumns);
    if (relIndex > 0) {
      columns.add(getColumnId());
    }
    return columns;
  }

  @Override
  protected List<String> getHistogramOrder() {
    return orderColumns;
  }
  
  private Filter buildFilter(Collection<String> values) {
    if (BeeUtils.isEmpty(values)) {
      return null;
    }

    CompoundFilter filter = Filter.or();

    for (String value : values) {
      if (value == null || value.isEmpty()) {
        filter.add(Filter.isEmpty(getColumnId()));
      } else if (relIndex >= 0) {
        filter.add(ComparisonFilter.isEqual(getColumnId(),
            new LongValue(BeeUtils.toLongOrNull(value))));
      } else {
        filter.add(ComparisonFilter.isEqual(getColumnId(),
            Value.parseValue(getColumnType(), value, false)));
      }
    }

    if (filter.isEmpty()) {
      return null;
    } else if (filter.size() == 1) {
      return filter.getSubFilters().get(0);
    } else {
      return filter;
    }
  }

  private Widget createWidget() {
    HtmlTable display = createDisplay(true);

    int row = 0;
    for (String[] dataItem : getData().getRows()) {
      int col = 0;

      if (dataItem[Math.max(relIndex, 0)] == null) {
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

  private String getCaption(int row) {
    if (isNull(row)) {
      return NULL_VALUE_LABEL;
    } else if (renderCount == 1) {
      return data.getValue(row, 0);
    } else {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < renderCount; i++) {
        String value = data.getValue(row, i);
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

  private String getCount(int row) {
    return data.getValue(row, countIndex);
  }

  private SimpleRowSet getData() {
    return data;
  }

  private boolean isNull(int row) {
    return data.getValue(row, Math.max(relIndex, 0)) == null;
  }

  private void setData(SimpleRowSet data) {
    this.data = data;
  }
}
