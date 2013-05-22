package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class CargoTripsGridHandler extends CargoPlaceRenderer {

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

      this.dialog = DialogBox.create("Priskirti reisą");
      dialog.setHideOnEscape(true);

      HtmlTable container = new HtmlTable();
      container.setBorderSpacing(5);

      container.setText(0, 0, "Pasirinkite reisą");

      Relation relation = Relation.create(VIEW_ACTIVE_TRIPS,
          Lists.newArrayList("TripNo", "VehicleNumber", "DriverFirstName", "DriverLastName",
              "ExpeditionType", "ForwarderName"));
      relation.disableNewRow();

      CompoundFilter filter = Filter.and();

      for (IsRow row : grd.getRowData()) {
        filter.add(ComparisonFilter.compareId(Operator.NE, row.getLong(tripIndex)));
      }
      relation.setFilter(filter);

      final UnboundSelector selector = UnboundSelector.create(relation,
          Lists.newArrayList("TripNo"));
      selector.addEditStopHandler(new EditStopEvent.Handler() {
        @Override
        public void onEditStop(EditStopEvent event) {
          if (event.isChanged()) {
            addTrip(BeeUtils.toLong(selector.getValue()));
          }
        }
      });
      container.setWidget(0, 1, selector);
      container.setWidget(1, 0, new BeeButton("Naujas reisas", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          createNewTrip(VIEW_TRIPS);
        }
      }));
      container.setWidget(1, 1, new BeeButton("Nauja ekspedicija", new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          createNewTrip(VIEW_EXPEDITION_TRIPS);
        }
      }));
      dialog.setWidget(container);
      dialog.showAt(grd.getAbsoluteLeft(), grd.getAbsoluteTop(), DomUtils.getScrollBarHeight() + 1);
    }

    private void addTrip(long tripId) {
      if (!DataUtils.isId(tripId)) {
        return;
      }
      dialog.close();

      List<BeeColumn> columns =
          DataUtils.getColumns(gridView.getDataColumns(), cargoIndex, tripIndex);
      List<String> values = Lists.newArrayList(BeeUtils.toString(gridView.getRelId()),
          BeeUtils.toString(tripId));

      Queries.insert(gridView.getViewName(), columns, values, null, new RowCallback() {
        @Override
        public void onSuccess(BeeRow row) {
          BeeKeeper.getBus().fireEvent(new RowInsertEvent(gridView.getViewName(), row));
          gridView.getGrid().insertRow(row, false);
        }
      });
    }

    private void createNewTrip(String viewName) {
      DataInfo dataInfo = Data.getDataInfo(viewName);

      RowFactory.createRow(dataInfo.getEditForm(), dataInfo.getNewRowCaption(),
          dataInfo, RowFactory.createEmptyRow(dataInfo, true), new RowCallback() {
            @Override
            public void onSuccess(BeeRow row) {
              addTrip(row.getId());
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