package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

class CargoTripsGrid extends AbstractGridInterceptor {

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    final GridView gridView = presenter.getGridView();

    gridView.ensureRelId(cargoId ->
        getTripFilter(filter -> TripSelector.select(new String[] {BeeUtils.toString(cargoId)},
            filter, gridView.getElement())));

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoTripsGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    if (!event.isReadOnly() && Objects.equals(event.getColumnId(), COL_TRIP_PERCENT)) {
      TripCargoGrid.updateFreight(getGridPresenter(), event.getRowValue().getId());
      event.consume();
      return;
    }
    super.onEditStart(event);
  }

  @Override
  public void onLoad(GridView gridView) {
    gridView.getViewPresenter().getHeader().addCommandItem(new MessageBuilder(gridView));
  }

  protected Filter getExclusionFilter() {
    GridView gridView = getGridView();

    if (gridView == null) {
      return Filter.isFalse();

    } else if (gridView.isEmpty()) {
      return null;

    } else {
      int tripIndex = gridView.getDataIndex(COL_TRIP);
      CompoundFilter tripFilter = Filter.and();

      for (IsRow row : gridView.getGrid().getRowData()) {
        tripFilter.add(Filter.compareId(Operator.NE, row.getLong(tripIndex)));
      }

      return tripFilter;
    }
  }

  protected void getTripFilter(Consumer<Filter> consumer) {
    consumer.accept(getExclusionFilter());
  }
}
