package com.butent.bee.client.modules.trade;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.StringList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AnalogsOfItemGrid extends AbstractGridInterceptor {

  private Long pendingId;

  AnalogsOfItemGrid() {
  }

  @Override
  public GridInterceptor getInstance() {
    return new AnalogsOfItemGrid();
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    maybeRefresh(presenter, getPendingId());
    setPendingId(null);
  }

  @Override
  public boolean beforeAction(Action action, GridPresenter presenter) {
    if (action == Action.DELETE) {
      if (presenter.getMainView().isEnabled() && presenter.getActiveRow() != null) {
        onDelete(ViewHelper.getFormRowId(presenter.getMainView()), presenter.getActiveRowId());
      }
      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    GridView gridView = presenter.getGridView();

    Long parentId = ViewHelper.getFormRowId(gridView);
    if (!DataUtils.isId(parentId)) {
      return false;
    }

    List<String> columns = StringList.of(COL_ITEM_NAME, COL_ITEM_ARTICLE);
    Relation relation = Relation.create(VIEW_ITEMS, columns);

    relation.disableNewRow();

    Set<Long> rowIds = new HashSet<>(DataUtils.getRowIds(gridView.getRowData()));
    rowIds.add(parentId);

    relation.setFilter(Filter.and(Filter.isNull(COL_ITEM_IS_SERVICE), Filter.idNotIn(rowIds)));

    MultiSelector selector = MultiSelector.autonomous(relation, columns);

    int width = Math.max(gridView.asWidget().getOffsetWidth() - 50, 300);
    StyleUtils.setWidth(selector, width);

    Global.inputWidget(Localized.dictionary().itemAnalogsNew(), selector, () -> {
      List<Long> input = DataUtils.parseIdList(selector.getValue());
      if (!input.isEmpty()) {
        addAnalogs(parentId, input);
      }
    }, null, presenter.getHeader().getElement());

    return false;
  }

  @Override
  public Map<String, Filter> getInitialParentFilters(Collection<UiOption> uiOptions) {
    return Provider.createDefaultParentFilters(getFilter(getPendingId()));
  }

  private static Filter getFilter(Long parentId) {
    if (DataUtils.isId(parentId)) {
      return Filter.custom(FILTER_ITEM_ANALOGS, parentId);
    } else {
      return Filter.isFalse();
    }
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (getGridPresenter() == null) {
      setPendingId(event.getRowId());
    } else {
      maybeRefresh(getGridPresenter(), event.getRowId());
    }
  }

  @Override
  public boolean previewRowInsert(RowInsertEvent event) {
    return false;
  }

  private void addAnalogs(Long item, Collection<Long> analogs) {
    List<BeeColumn> columns = Data.getColumns(VIEW_ITEM_ANALOGS,
        COL_ITEM_ANALOG_1, COL_ITEM_ANALOG_2);
    BeeRowSet rowSet = new BeeRowSet(VIEW_ITEM_ANALOGS, columns);

    for (Long analog : analogs) {
      rowSet.addRow(DataUtils.NEW_ROW_ID, DataUtils.NEW_ROW_VERSION, Queries.asList(item, analog));
    }

    Queries.insertRows(rowSet, result -> getGridPresenter().refresh(false, false));
  }

  private Long getPendingId() {
    return pendingId;
  }

  private static void maybeRefresh(GridPresenter presenter, Long parentId) {
    if (presenter != null) {
      Filter filter = getFilter(parentId);
      boolean changed = presenter.getDataProvider().setDefaultParentFilter(filter);

      if (changed) {
        presenter.handleAction(Action.REFRESH);
      }
    }
  }

  private void onDelete(Long item, Long analog) {
    if (DataUtils.isId(item) && DataUtils.isId(analog)) {
      Set<Long> values = new HashSet<>();
      values.add(item);
      values.add(analog);

      Filter filter = Filter.and(Filter.any(COL_ITEM_ANALOG_1, values),
          Filter.any(COL_ITEM_ANALOG_2, values));

      Queries.hasAnyRows(VIEW_ITEM_ANALOGS, filter, has -> {
        if (getGridView().isInteractive() && DataUtils.idEquals(getActiveRow(), analog)) {
          if (has) {
            List<String> messages = StringList.of(getStringValue(COL_ITEM_NAME),
                getStringValue(COL_ITEM_ARTICLE), Localized.dictionary().deleteQuestion());

            Global.confirmDelete(Localized.dictionary().itemAnalogsShort(), Icon.WARNING,
                messages, () -> Queries.delete(VIEW_ITEM_ANALOGS, filter, count -> {
                  if (BeeUtils.isPositive(count) && getGridView().isInteractive()) {
                    getGridPresenter().refresh(false, false);
                  }
                }));

          } else {
            getGridView().notifyWarning(Localized.dictionary().rowIsNotRemovable());
          }
        }
      });
    }
  }

  private void setPendingId(Long pendingId) {
    this.pendingId = pendingId;
  }
}
