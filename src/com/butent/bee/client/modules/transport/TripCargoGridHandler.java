package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class TripCargoGridHandler extends CargoPlaceRenderer {

  private static class Action {

    private final GridView gridView;
    private final int cargoIndex;
    private final int tripIndex;
    private final DialogBox dialog;

    public Action(GridView gridView) {
      CellGrid grd = gridView.getGrid();
      this.gridView = gridView;
      this.cargoIndex = DataUtils.getColumnIndex(COL_CARGO, gridView.getDataColumns());
      this.tripIndex = DataUtils.getColumnIndex(COL_TRIP, gridView.getDataColumns());

      this.dialog = DialogBox.create("Priskirti krovinį");
      dialog.setHideOnEscape(true);

      Horizontal container = new Horizontal();
      container.setBorderSpacing(5);

      container.add(new BeeLabel("Pasirinkite krovinį"));

      Relation relation = Relation.create(VIEW_WAITING_CARGO,
          Lists.newArrayList("OrderNo", "CustomerName", "Description", "LoadingDate"));
      relation.disableNewRow();

      CompoundFilter filter = Filter.and();

      for (IsRow row : grd.getRowData()) {
        filter.add(ComparisonFilter.compareId(Operator.NE, row.getLong(cargoIndex)));
      }
      relation.setFilter(filter);

      final UnboundSelector selector = UnboundSelector.create(relation,
          Lists.newArrayList("OrderNo", "Description"));

      selector.addEditStopHandler(new EditStopEvent.Handler() {
        @Override
        public void onEditStop(EditStopEvent event) {
          if (event.isChanged()) {
            addCargo(BeeUtils.toLong(selector.getValue()));
          }
        }
      });
      container.add(selector);
      dialog.setWidget(container);
      dialog.showAt(grd.getAbsoluteLeft(), grd.getAbsoluteTop(), DomUtils.getScrollBarHeight() + 1);
    }

    private void addCargo(final long cargoId) {
      if (!DataUtils.isId(cargoId)) {
        return;
      }
      dialog.close();

      gridView.ensureRelId(new IdCallback() {
        @Override
        public void onSuccess(Long result) {
          insertCargo(result, cargoId);
        }
      });
    }

    private void insertCargo(long tripId, long cargoId) {
      List<BeeColumn> columns = DataUtils.getColumns(gridView.getDataColumns(), tripIndex,
          cargoIndex);
      List<String> values = Queries.asList(tripId, cargoId);

      Queries.insert(gridView.getViewName(), columns, values, null, new RowCallback() {
        @Override
        public void onSuccess(BeeRow row) {
          BeeKeeper.getBus().fireEvent(new RowInsertEvent(gridView.getViewName(), row));
          gridView.getGrid().insertRow(row, false);
        }
      });
    }
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter) {
    Action action = new Action(presenter.getGridView());
    UiHelper.focus(action.dialog.getContent());
    return false;
  }
}