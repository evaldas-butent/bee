package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.crm.CrmConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class CargoRequestsGrid extends AbstractGridInterceptor {

  private InputBoolean owned;
  private InputBoolean finished;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof InputBoolean) {
      InputBoolean w = (InputBoolean) widget;

      if (BeeUtils.same(name, "Owned")) {
        owned = w;
      } else if (BeeUtils.same(name, "Finished")) {
        finished = w;
      } else {
        w = null;
      }
      if (w != null) {
        w.addValueChangeHandler(new ValueChangeHandler<String>() {
          @Override
          public void onValueChange(ValueChangeEvent<String> event) {
            getGridPresenter().handleAction(Action.REFRESH);
          }
        });
      }
    }
  }

  @Override
  public DeleteMode beforeDeleteRow(final GridPresenter presenter, final IsRow activeRow) {
    final Long cargoId;
    final long requestId = activeRow.getLong(presenter.getGridView()
        .getDataIndex(CrmConstants.COL_REQUEST));

    if (DataUtils.isId(activeRow.getLong(presenter.getGridView().getDataIndex(COL_ORDER)))) {
      cargoId = null;
    } else {
      cargoId = activeRow.getLong(presenter.getGridView().getDataIndex(COL_CARGO));
    }
    Queries.deleteRow(CrmConstants.TBL_REQUESTS, requestId, 0, new IntCallback() {
      @Override
      public void onSuccess(Integer result) {
        if (DataUtils.isId(cargoId)) {
          Queries.deleteRow(TBL_ORDER_CARGO, cargoId, 0);
        }
        BeeKeeper.getBus().fireEvent(new RowDeleteEvent(CrmConstants.TBL_REQUESTS, requestId));
        BeeKeeper.getBus().fireEvent(new RowDeleteEvent(presenter.getViewName(),
            activeRow.getId()));
      }
    });
    return DeleteMode.CANCEL;
  }

  @Override
  public void beforeRefresh(GridPresenter presenter) {
    presenter.getDataProvider().setParentFilter("CustomFilter", getFilter());
  }

  @Override
  public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
      Collection<RowInfo> selectedRows, DeleteMode defMode) {

    GridView gridView = presenter.getGridView();

    boolean enabled =
        activeRow.getDateTime(gridView.getDataIndex(CrmConstants.COL_REQUEST_FINISHED)) == null;

    if (enabled) {
      Long author = activeRow.getLong(gridView.getDataIndex("Creator"));
      Long manager = activeRow.getLong(gridView.getDataIndex("Manager"));

      enabled = (author == null && manager == null)
          || Objects.equal(author, BeeKeeper.getUser().getUserId())
          || Objects.equal(manager, BeeKeeper.getUser().getUserId());
    }
    if (enabled) {
      return DeleteMode.SINGLE;
    } else {
      Global.showError("No way");
      return DeleteMode.CANCEL;
    }
  }

  @Override
  public BeeRowSet getInitialRowSet(GridDescription gridDescription) {
    return Data.createRowSet(gridDescription.getViewName());
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoRequestsGrid();
  }

  @Override
  public void onShow(GridPresenter presenter) {
    presenter.handleAction(Action.REFRESH);
  }

  private Filter getFilter() {
    Filter filter = null;

    if (owned != null && BooleanValue.unpack(owned.getValue())) {
      filter = ComparisonFilter.isEqual(CrmConstants.COL_REQUEST_MANAGER,
          new LongValue(BeeKeeper.getUser().getUserId()));
    }
    if (finished == null || !BooleanValue.unpack(finished.getValue())) {
      filter = Filter.and(filter, Filter.isEmpty(CrmConstants.COL_REQUEST_FINISHED));
    }
    return filter;
  }
}
