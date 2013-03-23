package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.grid.HtmlTable;
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

    CompoundFilter compoundFilter = Filter.or();

    int colIndex = (relIndex > 0) ? relIndex : 0;
    for (int row : getSelectedItems()) {
      String value = data.getValue(row, colIndex);
      Filter rowFilter;

      if (value == null || value.isEmpty()) {
        rowFilter = Filter.isEmpty(getColumnId());
      } else if (colIndex == relIndex) {
        rowFilter = ComparisonFilter.isEqual(getColumnId(),
            new LongValue(BeeUtils.toLongOrNull(value)));
      } else {
        rowFilter = ComparisonFilter.isEqual(getColumnId(),
            Value.parseValue(getColumnType(), value, false));
      }
      compoundFilter.add(rowFilter);
    }

    Filter newFilter;
    if (compoundFilter.isEmpty()) {
      newFilter = null;
    } else if (compoundFilter.size() == 1) {
      newFilter = compoundFilter.getSubFilters().get(0);
    } else {
      newFilter = compoundFilter;
    }

    update(newFilter);
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
