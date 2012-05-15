package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.List;

public class TransportHandler {

  private static class Profit implements ClickHandler {
    private final String idName;

    private Profit(String idName) {
      this.idName = idName;
    }

    @Override
    public void onClick(ClickEvent event) {
      ParameterList args = TransportHandler.createArgs(TransportConstants.SVC_GET_PROFIT);
      final FormView form = UiHelper.getForm((Widget) event.getSource());
      args.addDataItem(idName, BeeUtils.transform(form.getActiveRow().getId()));

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            Global.showError((Object[]) response.getErrors());

          } else if (response.hasArrayResponse(String.class)) {
            form.notifyInfo(Codec.beeDeserializeCollection((String) response.getResponse()));

          } else {
            Global.showError("Unknown response");
          }
        }
      });
    }
  }

  private static class CargoFormHandler extends AbstractFormCallback {
    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Profit(TransportConstants.VAR_CARGO_ID));
      }
    }
  }

  private static class CargoTripsGridHandler extends AbstractGridCallback {
    @Override
    public boolean beforeAddRow(final GridPresenter presenter) {
      CompoundFilter filter = Filter.and();
      filter.add(ComparisonFilter.isEmpty("DateTo"));
      int index = presenter.getDataProvider().getColumnIndex("Trip");

      for (IsRow row : presenter.getView().getContent().getGrid().getRowData()) {
        filter.add(ComparisonFilter.compareId(Operator.NE, row.getLong(index)));
      }
      Queries.getRowSet("Trips", null, filter, null, new RowSetCallback() {
        @Override
        public void onSuccess(BeeRowSet result) {
          if (result.isEmpty()) {
            presenter.getView().getContent().notifyWarning("No trips available");
            return;
          }
          MultiSelector selector = new MultiSelector("Galimi reisai", result,
              Lists.newArrayList("TripNo", "ForwarderName", "ForwarderVehicle", "VehicleNumber",
                  "DateFrom", "DateTo"),
              new MultiSelector.SelectionCallback() {
                @Override
                public void onSelection(List<IsRow> rows) {
                  String cargo = BeeUtils.toString(presenter.getView().getContent().getRelId());

                  List<BeeColumn> columns =
                      Lists.newArrayList(new BeeColumn(ValueType.LONG, "Cargo"),
                          new BeeColumn(ValueType.LONG, "Trip"));
                  BeeRowSet rowSet = new BeeRowSet("CargoTrips", columns);

                  for (IsRow row : rows) {
                    rowSet.addRow(new BeeRow(DataUtils.NEW_ROW_ID,
                        new String[] {cargo, BeeUtils.toString(row.getId())}));
                  }

                  Queries.insertRowSet(rowSet, new RowSetCallback() {
                    public void onSuccess(BeeRowSet res) {
                      for (BeeRow row : res.getRows()) {
                        BeeKeeper.getBus().fireEvent(new RowInsertEvent(res.getViewName(), row));
                        presenter.getView().getContent().getGrid().insertRow(row);
                      }
                    }
                  });
                }
              });
          selector.center();
        }
      });
      return false;
    }
  }

  private static class OrderFormHandler extends AbstractFormCallback {
    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Profit(TransportConstants.VAR_ORDER_ID));
      }
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex(TransportConstants.COL_STATUS),
          OrderStatus.CREATED.ordinal());
    }
  }

  private static class SparePartsGridHandler extends AbstractGridCallback
      implements SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedType = null;
    private TreePresenter typeTree = null;

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof TreeView && BeeUtils.same(name, "SparePartTypes")) {
        ((TreeView) widget).addSelectionHandler(this);
        typeTree = ((TreeView) widget).getTreePresenter();
      }
    }

    @Override
    public SparePartsGridHandler getInstance() {
      return new SparePartsGridHandler();
    }

    public void onSelection(SelectionEvent<IsRow> event) {
      if (event == null) {
        return;
      }
      if (getGridPresenter() != null) {
        Long type = null;
        setSelectedType(event.getSelectedItem());

        if (getSelectedType() != null) {
          type = getSelectedType().getId();
        }
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(type));
        getGridPresenter().requery(true);
      }
    }

    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      IsRow type = getSelectedType();

      if (type != null) {
        List<BeeColumn> cols = getGridPresenter().getDataColumns();
        newRow.setValue(DataUtils.getColumnIndex("Type", cols), type.getId());
        newRow.setValue(DataUtils.getColumnIndex("ParentTypeName", cols),
            getTypeValue(type, "ParentName"));
        newRow.setValue(DataUtils.getColumnIndex("TypeName", cols),
            getTypeValue(type, "Name"));
      }
      return true;
    }

    private Filter getFilter(Long type) {
      if (type == null) {
        return null;
      } else {
        return ComparisonFilter.isEqual("Type", new LongValue(type));
      }
    }

    private IsRow getSelectedType() {
      return selectedType;
    }

    private String getTypeValue(IsRow type, String colName) {
      if (BeeUtils.allNotEmpty(type, typeTree, typeTree.getDataColumns())) {
        return type.getString(DataUtils.getColumnIndex(colName, typeTree.getDataColumns()));
      }
      return null;
    }

    private void setSelectedType(IsRow selectedType) {
      this.selectedType = selectedType;
    }
  }

  private static class TripFormHandler extends AbstractFormCallback {
    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Profit(TransportConstants.VAR_TRIP_ID));
      }
    }
  }

  private static class TripRoutesGridHandler extends AbstractGridCallback {
    @Override
    public boolean afterCreateColumn(final String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        final EditableColumn editableColumn) {

      if (BeeUtils.inList(columnId, "SpeedometerFrom", "SpeedometerTo", "Kilometers")
          && editableColumn != null) {
        editableColumn.addCellValidationHandler(new CellValidateEvent.Handler() {
          @Override
          public Boolean validateCell(CellValidateEvent event) {
            if (event.isPostValidation()
                && !BeeUtils.isEmpty(event.getCellValidation().getRow().getId())) {

              final String viewName = getGridPresenter().getDataProvider().getViewName();
              List<BeeColumn> columns = getGridPresenter().getDataProvider().getColumns();
              CellValidation cv = event.getCellValidation();
              IsRow row = cv.getRow();

              List<BeeColumn> cols = Lists.newArrayList(editableColumn.getDataColumn());
              List<String> values = Lists.newArrayList(cv.getOldValue());

              String updColName;

              if (BeeUtils.equals(columnId, "Kilometers")) {
                updColName = "SpeedometerTo";
              } else {
                updColName = "Kilometers";
              }
              cols.add(DataUtils.getColumn(updColName, columns));
              values.add(row.getString(DataUtils.getColumnIndex(updColName, columns)));

              BeeRowSet rs = new BeeRowSet(viewName, cols);
              rs.addRow(row.getId(), row.getVersion(), values.toArray(new String[0]));
              rs.getRow(0).preliminaryUpdate(0, cv.getNewValue());

              ParameterList args = TransportHandler.createArgs(TransportConstants.SVC_UPDATE_KM);
              args.addDataItem("Rowset", Codec.beeSerialize(rs));

              if (BeeUtils.equals(columnId, "SpeedometerFrom")) {
                updColName = "SpeedometerTo";
              } else {
                updColName = "SpeedometerFrom";
              }
              args.addDataItem(updColName,
                  Codec.beeSerialize(row.getString(DataUtils.getColumnIndex(updColName, columns))));

              BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  Assert.notNull(response);

                  if (response.hasErrors()) {
                    Global.showError((Object[]) response.getErrors());

                  } else if (response.hasResponse(BeeRow.class)) {
                    BeeRow newRow = BeeRow.restore((String) response.getResponse());
                    BeeKeeper.getBus().fireEvent(new RowUpdateEvent(viewName, newRow));

                  } else {
                    Global.showError("Unknown response");
                  }
                }
              });
              return null;
            }
            return true;
          }
        });
      }
      return true;
    }
  }

  private static class VehiclesGridHandler extends AbstractGridCallback
      implements SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedModel = null;
    private TreePresenter modelTree = null;

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof TreeView && BeeUtils.same(name, "VehicleModels")) {
        ((TreeView) widget).addSelectionHandler(this);
        modelTree = ((TreeView) widget).getTreePresenter();
      }
    }

    @Override
    public VehiclesGridHandler getInstance() {
      return new VehiclesGridHandler();
    }

    public void onSelection(SelectionEvent<IsRow> event) {
      if (event == null) {
        return;
      }
      if (getGridPresenter() != null) {
        Long model = null;
        setSelectedModel(event.getSelectedItem());

        if (getSelectedModel() != null) {
          model = getSelectedModel().getId();
        }
        getGridPresenter().getDataProvider().setParentFilter(FILTER_KEY, getFilter(model));
        getGridPresenter().requery(true);
      }
    }

    @Override
    public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow) {
      IsRow model = getSelectedModel();

      if (model != null) {
        List<BeeColumn> cols = getGridPresenter().getDataColumns();
        newRow.setValue(DataUtils.getColumnIndex("Model", cols), model.getId());
        newRow.setValue(DataUtils.getColumnIndex("ParentModelName", cols),
            getModelValue(model, "ParentName"));
        newRow.setValue(DataUtils.getColumnIndex("ModelName", cols),
            getModelValue(model, "Name"));
      }
      return true;
    }

    private Filter getFilter(Long model) {
      if (model == null) {
        return null;
      } else {
        Value value = new LongValue(model);

        return Filter.or(ComparisonFilter.isEqual("ParentModel", value),
            ComparisonFilter.isEqual("Model", value));
      }
    }

    private String getModelValue(IsRow model, String colName) {
      if (BeeUtils.allNotEmpty(model, modelTree, modelTree.getDataColumns())) {
        return model.getString(DataUtils.getColumnIndex(colName, modelTree.getDataColumns()));
      }
      return null;
    }

    private IsRow getSelectedModel() {
      return selectedModel;
    }

    private void setSelectedModel(IsRow selectedModel) {
      this.selectedModel = selectedModel;
    }
  }

  public static void register() {
    Global.registerCaptions(OrderStatus.class);
    FormFactory.registerFormCallback("TransportationOrder", new OrderFormHandler());
    GridFactory.registerGridCallback("Vehicles", new VehiclesGridHandler());
    GridFactory.registerGridCallback("SpareParts", new SparePartsGridHandler());
    GridFactory.registerGridCallback("TripRoutes", new TripRoutesGridHandler());
    GridFactory.registerGridCallback("CargoTrips", new CargoTripsGridHandler());
    FormFactory.registerFormCallback("Trip", new TripFormHandler());
    FormFactory.registerFormCallback("OrderCargo", new CargoFormHandler());
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(TransportConstants.TRANSPORT_MODULE);
    args.addQueryItem(TransportConstants.TRANSPORT_METHOD, name);
    return args;
  }

  private TransportHandler() {
  }
}
