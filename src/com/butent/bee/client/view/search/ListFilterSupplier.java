package com.butent.bee.client.view.search;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.dialog.NotificationListener;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.Binder;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ListFilterSupplier extends AbstractFilterSupplier {

  private final List<String> renderColumns = Lists.newArrayList();
  private final List<String> orderColumns = Lists.newArrayList();

  private final int renderCount;
  private final int relIndex;
  private final int countIndex;

  private SimpleRowSet data = null;

  private final List<Integer> selected = Lists.newArrayList();

  private String displayId = null;

  public ListFilterSupplier(String viewName, BeeColumn column, List<String> renderColumns,
      List<String> orderColumns, Relation relation, String options) {
    super(viewName, column, options);

    if (relation == null) {
      this.renderColumns.add(column.getId());
      this.orderColumns.add(column.getId());

      this.relIndex = BeeConst.UNDEF;
      this.countIndex = 1;

    } else {
      this.renderColumns.addAll(renderColumns);
      this.orderColumns.addAll(orderColumns);

      this.relIndex = renderColumns.size();
      this.countIndex = renderColumns.size() + 1;
    }

    this.renderCount = this.renderColumns.size();
  }

  @Override
  public String getDisplayHtml() {
    List<String> values = Lists.newArrayList();

    for (int row : selected) {
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
          selected.clear();
          openDialog(target, createWidget(), callback);
        }
      }
    });
  }

  @Override
  public boolean reset() {
    selected.clear();
    return super.reset();
  }

  @Override
  protected void doFilterCommand() {
    if (selected.isEmpty()) {
      update(null);
      return;
    }

    CompoundFilter compoundFilter = Filter.or();

    int colIndex = (relIndex > 0) ? relIndex : 0;
    for (int row : selected) {
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
  protected void doResetCommand() {
    HtmlTable display = getDisplay();
    if (display == null) {
      return;
    }

    for (int row : selected) {
      display.getRowFormatter().removeStyleName(row, getStyleSelected());
    }
    selected.clear();
    setCounter(0);
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

  @Override
  protected String getStylePrefix() {
    return super.getStylePrefix() + "List-";
  }

  private Widget createWidget() {
    final HtmlTable display = new HtmlTable();
    display.addStyleName(getStylePrefix() + "display");

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

      display.setText(row, col, dataItem[countIndex]);
      row++;
    }

    Binder.addClickHandler(display, new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Element target = EventUtils.getEventTargetElement(event);
        TableRowElement rowElement = DomUtils.getParentRow(target, true);

        if (rowElement != null && display.getElement().isOrHasChild(rowElement)) {
          onMouseClick(rowElement, EventUtils.hasModifierKey(event.getNativeEvent()));
        }
      }
    });

    Flow container = new Flow();
    container.addStyleName(getStylePrefix() + "container");

    Flow panel = new Flow();
    panel.addStyleName(getStylePrefix() + "panel");
    panel.add(display);

    container.add(panel);
    container.add(getCommandWidgets(true));

    setDisplayId(display.getId());

    return container;
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

  private HtmlTable getDisplay() {
    Widget widget = getDialogChild(getDisplayId());
    if (widget instanceof HtmlTable) {
      return (HtmlTable) widget;
    } else {
      return null;
    }
  }

  private String getDisplayId() {
    return displayId;
  }

  private String getStyleSelected() {
    return getStylePrefix() + "selected";
  }

  private boolean isNull(int row) {
    return data.getValue(row, Math.max(relIndex, 0)) == null;
  }

  private void onMouseClick(TableRowElement rowElement, boolean hasModifiers) {
    int row = rowElement.getRowIndex();
    boolean wasSelected = selected.contains(row);

    if (wasSelected) {
      rowElement.removeClassName(getStyleSelected());
      selected.remove((Integer) row);
    } else {
      rowElement.addClassName(getStyleSelected());
      selected.add(row);
    }
    
    setCounter(selected.size());
  }

  private void setData(SimpleRowSet data) {
    this.data = data;
  }

  private void setDisplayId(String displayId) {
    this.displayId = displayId;
  }
}
