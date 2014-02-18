package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.IntegerValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.transport.TransportConstants.AssessmentStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AssessmentsGrid extends AbstractGridInterceptor {

  private final Map<AssessmentStatus, InputBoolean> checks = Maps.newHashMap();

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    AssessmentStatus status = EnumUtils.getEnumByName(AssessmentStatus.class, name);

    if (widget instanceof InputBoolean && status != null) {
      checks.put(status, (InputBoolean) widget);

      checks.get(status).addValueChangeHandler(new ValueChangeHandler<String>() {
        @Override
        public void onValueChange(ValueChangeEvent<String> event) {
          getGridPresenter().handleAction(Action.REFRESH);
        }
      });
    }
  }

  @Override
  public DeleteMode beforeDeleteRow(final GridPresenter presenter, final IsRow activeRow) {
    final String expeditionTrips = "ExpeditionTrips";
    final long orderId = activeRow.getLong(presenter.getGridView().getDataIndex(COL_ORDER));
    long cargoId = activeRow.getLong(presenter.getGridView().getDataIndex(COL_CARGO));

    Queries.getRowSet(TBL_CARGO_TRIPS, Lists.newArrayList(COL_TRIP),
        Filter.equals(COL_CARGO, cargoId), new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet rowSet) {
            Queries.deleteRow(TBL_ORDERS, orderId, 0, new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                RowDeleteEvent.fire(BeeKeeper.getBus(), TBL_ORDERS, orderId);
                RowDeleteEvent.fire(BeeKeeper.getBus(), presenter.getViewName(), activeRow.getId());
              }
            });
            if (!rowSet.isEmpty()) {
              final List<RowInfo> rows = Lists.newArrayList();

              for (int i = 0; i < rowSet.getNumberOfRows(); i++) {
                rows.add(new RowInfo(rowSet.getLong(i, 0), 0, true));
              }
              Queries.deleteRows(expeditionTrips, rows, new IntCallback() {
                @Override
                public void onSuccess(Integer result) {
                  for (RowInfo row : rows) {
                    RowDeleteEvent.fire(BeeKeeper.getBus(), expeditionTrips, row.getId());
                  }
                }
              });
            }
          }
        });
    return DeleteMode.CANCEL;
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
    presenter.getDataProvider().setParentFilter(COL_STATUS, getFilter());
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    GridView gridView = presenter.getGridView();

    boolean primary = !DataUtils.isId(activeRow.getLong(gridView.getDataIndex(COL_ASSESSOR)));
    boolean owner = Objects.equal(activeRow.getLong(gridView.getDataIndex(COL_ASSESSOR_MANAGER)),
        BeeKeeper.getUser().getUserId());
    boolean validState = AssessmentStatus.NEW
        .is(activeRow.getInteger(gridView.getDataIndex(COL_STATUS)));

    if (!primary || !owner || !validState) {
      Global.showError("No way");
      return DeleteMode.CANCEL;
    } else {
      return DeleteMode.SINGLE;
    }
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new AssessmentsGrid();
  }

  @Override
  public boolean onLoad(GridDescription gridDescription) {
    gridDescription.setFilter(Filter.equals(COL_ASSESSOR_MANAGER, BeeKeeper.getUser().getUserId()));
    return true;
  }

  @Override
  public void onShow(GridPresenter presenter) {
    presenter.handleAction(Action.REFRESH);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
    newRow.setValue(gridView.getDataIndex("OrderStatus"), OrderStatus.REQUEST.ordinal());
    return true;
  }

  private Filter getFilter() {
    CompoundFilter filter = Filter.or();

    for (AssessmentStatus check : checks.keySet()) {
      if (BooleanValue.unpack(checks.get(check).getValue())) {
        filter.add(Filter.isEqual(COL_STATUS, IntegerValue.of(check)));
      }
    }
    if (filter.isEmpty()) {
      filter.add(Filter.isFalse());
    }
    return filter;
  }
}
