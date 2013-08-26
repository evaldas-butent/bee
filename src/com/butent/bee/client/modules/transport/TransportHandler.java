package com.butent.bee.client.modules.transport;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.modules.transport.charts.ChartHelper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.edit.EditStopEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.transport.TransportConstants.AssessmentStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.modules.transport.TransportConstants.TripStatus;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.List;

public final class TransportHandler {

  private static class CargoFormHandler extends AbstractFormInterceptor {
    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget).addClickHandler(new Profit(VAR_CARGO_ID));
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new CargoFormHandler();
    }
  }

  private static class CargoGridHandler extends CargoPlaceRenderer {
    @Override
    public DeleteMode getDeleteMode(GridPresenter presenter, IsRow activeRow,
        Collection<RowInfo> selectedRows, DeleteMode defMode) {

      return new CargoTripChecker().getDeleteMode(presenter, activeRow, selectedRows, defMode);
    }
  }

  private static class OrderFormHandler extends AbstractFormInterceptor {
    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Profit(COL_ORDER));
      }
      if (BeeUtils.same(name, "trips") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Trips(COL_ORDER));
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return this;
    }
  }

  private static final class Trips implements ClickHandler {
    private final String idName;
    private DialogBox dialog;

    private Trips(String idName) {
      this.idName = idName;
    }

    @Override
    public void onClick(ClickEvent event) {

      final FormView form = UiHelper.getForm((Widget) event.getSource());

      this.dialog = DialogBox.create(Localized.getConstants().assignCargosToTripCaption());
      dialog.setHideOnEscape(true);

      HtmlTable container = new HtmlTable();
      container.setBorderSpacing(5);

      container.setText(0, 0, Localized.getConstants().cargoSelectTrip());

      Relation relation = Relation.create(VIEW_ACTIVE_TRIPS,
          Lists.newArrayList("TripNo", "VehicleNumber", "DriverFirstName", "DriverLastName",
              "ExpeditionType", "ForwarderName"));
      relation.disableNewRow();

      relation.setOrder(new Order(COL_TRIP_ID, false));
      relation.setCaching(Relation.Caching.QUERY);

      final UnboundSelector selector = UnboundSelector.create(relation,
          Lists.newArrayList("TripNo"));

      selector.addEditStopHandler(new EditStopEvent.Handler() {

        @Override
        public void onEditStop(EditStopEvent esEvent) {
          if (esEvent.isChanged()) {
            ParameterList args = TransportHandler.createArgs(SVC_GET_UNASSIGNED_CARGOS);
            args.addDataItem(idName, form.getActiveRow().getId());

            dialog.close();

            BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {

              @Override
              public void onResponse(ResponseObject response) {

                if (!response.hasResponse()) {
                  Global.showError(Localized.getMessages().dataNotAvailable("Unassignment cargos"));
                  return;
                }

                if (response.hasResponse(SimpleRowSet.class)) {
                  SimpleRowSet rs = SimpleRowSet.restore((String) response.getResponse());
                  BeeColumn cargo = new BeeColumn(ValueType.NUMBER, "Cargo");
                  BeeColumn trip = new BeeColumn(ValueType.NUMBER, "Trip");
                  List<BeeColumn> column = Lists.newArrayList(cargo, trip);
                  List<String> values = Lists.newArrayList();

                  for (int row = 0; row < rs.getNumberOfRows(); row++) {
                    values.add(rs.getValue(row, 0));
                    values.add(selector.getValue());
                    Queries.insert(VIEW_CARGO_TRIPS, column, values);
                    values.clear();
                  }

                  Global.showInfo(Localized.getMessages().orderCargoAddingToTrips(
                      rs.getNumberOfRows()));
                }
              }
            });
          }
        }
      });
      container.setWidget(0, 1, selector);
      dialog.setWidget(container);
      dialog.showOnTop(form.getElement(), 1);
    }
  }

  private static final class Profit implements ClickHandler {
    private final String idName;

    private Profit(String idName) {
      this.idName = idName;
    }

    @Override
    public void onClick(ClickEvent event) {
      ParameterList args = TransportHandler.createArgs(SVC_GET_PROFIT);
      final FormView form = UiHelper.getForm((Widget) event.getSource());
      args.addDataItem(idName, form.getActiveRow().getId());

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            Global.showError(Lists.newArrayList(response.getErrors()));

          } else if (response.hasArrayResponse(String.class)) {
            String[] arr = Codec.beeDeserializeCollection((String) response.getResponse());
            List<String> messages = Lists.newArrayList();

            if (arr != null && arr.length % 2 == 0) {
              for (int i = 0; i < arr.length; i += 2) {
                messages.add(BeeUtils.joinWords(arr[i],
                    BeeUtils.isDouble(arr[i + 1]) ? BeeUtils.round(arr[i + 1], 2) : arr[i + 1]));
              }
            }

            if (messages.isEmpty()) {
              form.notifyInfo(arr);
            } else {
              form.notifyInfo(ArrayUtils.toArray(messages));
            }

          } else {
            Global.showError("Unknown response");
          }
        }
      });
    }
  }

  private static class SparePartsGridHandler extends AbstractGridInterceptor
      implements SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedType;
    private TreePresenter typeTree;

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof TreeView && BeeUtils.same(name, "SparePartTypes")) {
        ((TreeView) widget).addSelectionHandler(this);
        typeTree = ((TreeView) widget).getTreePresenter();
      }
    }

    @Override
    public SparePartsGridHandler getInstance() {
      return new SparePartsGridHandler();
    }

    @Override
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
        getGridPresenter().refresh(true);
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

    private static Filter getFilter(Long type) {
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
      if (BeeUtils.allNotNull(type, typeTree, typeTree.getDataColumns())) {
        return type.getString(DataUtils.getColumnIndex(colName, typeTree.getDataColumns()));
      }
      return null;
    }

    private void setSelectedType(IsRow selectedType) {
      this.selectedType = selectedType;
    }
  }

  private static class TripFormHandler extends AbstractFormInterceptor {
    @Override
    public void afterCreateEditableWidget(EditableWidget editableWidget) {
      if (BeeUtils.same(editableWidget.getColumnId(), "Vehicle")) {
        String viewName = getFormView().getViewName();
        final int dateIndex = Data.getColumnIndex(viewName, "Date");
        final int speedIndex = Data.getColumnIndex(viewName, "SpeedometerBefore");
        final int fuelIndex = Data.getColumnIndex(viewName, "FuelBefore");

        editableWidget.addCellValidationHandler(new CellValidateEvent.Handler() {
          @Override
          public Boolean validateCell(CellValidateEvent event) {
            if (event.isCellValidation() && event.isPostValidation()) {
              CellValidation cv = event.getCellValidation();
              String id = cv.getNewValue();

              if (!BeeUtils.isEmpty(id)) {
                final IsRow row = cv.getRow();

                ParameterList args = TransportHandler.createArgs(SVC_GET_BEFORE);
                args.addDataItem("Vehicle", id);

                if (!row.isNull(dateIndex)) {
                  args.addDataItem("Date", row.getString(dateIndex));
                }
                BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                  @Override
                  public void onResponse(ResponseObject response) {
                    Assert.notNull(response);

                    if (response.hasErrors()) {
                      Global.showError(Lists.newArrayList(response.getErrors()));

                    } else if (response.hasArrayResponse(String.class)) {
                      String[] r = Codec.beeDeserializeCollection((String) response.getResponse());
                      row.setValue(speedIndex, r[0]);
                      row.setValue(fuelIndex, r[1]);
                      getFormView().refresh(false, false);

                    } else {
                      Global.showError("Unknown response");
                    }
                  }
                });
              }
            }
            return true;
          }
        });
      }
    }

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (BeeUtils.same(name, "profit") && widget instanceof HasClickHandlers) {
        ((HasClickHandlers) widget)
            .addClickHandler(new Profit(VAR_TRIP_ID));
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return this;
    }
  }

  private static class TripRoutesGridHandler extends AbstractGridInterceptor {
    private String viewName;
    private Integer speedFromIndex;
    private Integer speedToIndex;
    private BeeColumn speedToColumn;
    private Integer kmIndex;
    private BeeColumn kmColumn;

    private Integer scale;

    @Override
    public boolean afterCreateColumn(final String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        final EditableColumn editableColumn) {

      if (BeeUtils.inList(columnId, "SpeedometerFrom", "SpeedometerTo", "Kilometers")
          && editableColumn != null) {

        editableColumn.addCellValidationHandler(new CellValidateEvent.Handler() {
          @Override
          public Boolean validateCell(CellValidateEvent event) {
            if (event.isCellValidation() && event.isPostValidation()) {
              CellValidation cv = event.getCellValidation();
              IsRow row = cv.getRow();

              BeeColumn updColumn;
              int updIndex;
              Double updValue;
              double newVal = BeeUtils.toDouble(cv.getNewValue());

              if (Objects.equal(columnId, "Kilometers")) {
                updValue = row.getDouble(speedFromIndex);
                updColumn = speedToColumn;
                updIndex = speedToIndex;
              } else {
                if (Objects.equal(columnId, "SpeedometerFrom")) {
                  newVal = 0 - newVal;
                  updValue = row.getDouble(speedToIndex);
                } else {
                  updValue = 0 - BeeUtils.unbox(row.getDouble(speedFromIndex));
                }
                updColumn = kmColumn;
                updIndex = kmIndex;
              }
              updValue = BeeUtils.unbox(updValue) + newVal;

              if (BeeUtils.isPositive(scale)) {
                if (updValue < 0) {
                  updValue += scale;
                } else if (updValue >= scale) {
                  updValue -= scale;
                }
              } else if (updValue < 0) {
                updValue = null;
              }
              if (event.isNewRow()) {
                row.setValue(updIndex, updValue);

              } else {
                List<BeeColumn> cols = Lists.newArrayList(cv.getColumn(), updColumn);
                List<String> oldValues = Lists.newArrayList(cv.getOldValue(),
                    row.getString(updIndex));
                List<String> newValues = Lists.newArrayList(cv.getNewValue(),
                    BeeUtils.toString(updValue));

                Queries.update(viewName, row.getId(), row.getVersion(), cols, oldValues, newValues,
                    null, new RowUpdateCallback(viewName));
                return null;
              }
            }
            return true;
          }
        });
      }
      return true;
    }

    @Override
    public void beforeCreate(List<? extends IsColumn> dataColumns,
        GridDescription gridDescription) {

      viewName = gridDescription.getViewName();
      speedFromIndex = Data.getColumnIndex(viewName, "SpeedometerFrom");
      speedToIndex = Data.getColumnIndex(viewName, "SpeedometerTo");
      speedToColumn = new BeeColumn(ValueType.NUMBER, "SpeedometerTo");
      kmIndex = Data.getColumnIndex(viewName, "Kilometers");
      kmColumn = new BeeColumn(ValueType.NUMBER, "Kilometers");
    }

    @Override
    public GridInterceptor getInstance() {
      return new TripRoutesGridHandler();
    }

    @Override
    public void onParentRow(ParentRowEvent event) {
      if (event.getRow() == null) {
        scale = null;
      } else {
        scale = Data.getInteger(event.getViewName(), event.getRow(), "Speedometer");
      }
    }
  }

  private static class VehiclesGridHandler extends AbstractGridInterceptor
      implements SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedModel;
    private TreePresenter modelTree;

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {
      if (widget instanceof TreeView && BeeUtils.same(name, "VehicleModels")) {
        ((TreeView) widget).addSelectionHandler(this);
        modelTree = ((TreeView) widget).getTreePresenter();
      }
    }

    @Override
    public VehiclesGridHandler getInstance() {
      return new VehiclesGridHandler();
    }

    @Override
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
        getGridPresenter().refresh(true);
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

    private static Filter getFilter(Long model) {
      if (model == null) {
        return null;
      } else {
        Value value = new LongValue(model);

        return Filter.or(ComparisonFilter.isEqual("ParentModel", value),
            ComparisonFilter.isEqual("Model", value));
      }
    }

    private String getModelValue(IsRow model, String colName) {
      if (BeeUtils.allNotNull(model, modelTree, modelTree.getDataColumns())) {
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

  public static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(TRANSPORT_MODULE);
    args.addQueryItem(TRANSPORT_METHOD, name);
    return args;
  }

  public static void register() {
    Captions.register(OrderStatus.class);
    Captions.register(AssessmentStatus.class);
    Captions.register(TripStatus.class);

    SelectorEvent.register(new TransportSelectorHandler());

    GridFactory.registerGridInterceptor(VIEW_VEHICLES, new VehiclesGridHandler());
    GridFactory.registerGridInterceptor(VIEW_SPARE_PARTS, new SparePartsGridHandler());
    GridFactory.registerGridInterceptor(VIEW_TRIP_ROUTES, new TripRoutesGridHandler());
    GridFactory.registerGridInterceptor(VIEW_CARGO_TRIPS, new CargoTripsGridHandler());

    GridFactory.registerGridInterceptor(VIEW_ORDERS, new CargoTripChecker());
    GridFactory.registerGridInterceptor(VIEW_TRIPS, new CargoTripChecker());
    GridFactory.registerGridInterceptor(VIEW_EXPEDITION_TRIPS, new CargoTripChecker());

    GridFactory.registerGridInterceptor(VIEW_ORDER_CARGO, new CargoGridHandler());
    GridFactory.registerGridInterceptor(VIEW_TRIP_CARGO, new TripCargoGridHandler());
    GridFactory.registerGridInterceptor(VIEW_CARGO_HANDLING, new CargoPlaceRenderer());
    GridFactory.registerGridInterceptor(VIEW_ALL_CARGO, new CargoPlaceRenderer());

    GridFactory.registerGridInterceptor("CargoRequests", new CargoRequestsGrid());
    FormFactory.registerFormInterceptor("CargoRequest", new CargoRequestForm());

    GridFactory.registerGridInterceptor("AssessmentRequests", new AssessmentsGrid());
    GridFactory.registerGridInterceptor("AssessmentOrders", new AssessmentsGrid());

    GridFactory.registerGridInterceptor(VIEW_CARGO_INCOME_LIST, new CargoIncomeListGrid());

    FormFactory.registerFormInterceptor(FORM_ORDER, new OrderFormHandler());
    FormFactory.registerFormInterceptor(FORM_TRIP, new TripFormHandler());
    FormFactory.registerFormInterceptor(FORM_EXPEDITION_TRIP, new TripFormHandler());
    FormFactory.registerFormInterceptor(FORM_CARGO, new CargoFormHandler());

    FormFactory.registerFormInterceptor(FORM_ASSESSMENT, new AssessmentForm());
    FormFactory.registerFormInterceptor("CargoInvoice", new CargoInvoiceForm());

    BeeKeeper.getBus().registerRowActionHandler(new TransportActionHandler(), false);

    ChartHelper.register();
  }

  private TransportHandler() {
  }
}
