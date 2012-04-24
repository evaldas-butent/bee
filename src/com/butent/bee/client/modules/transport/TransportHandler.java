package com.butent.bee.client.modules.transport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.validation.CellValidation;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.List;
import java.util.Map;

public class TransportHandler {

  private static class OrderFormHandler extends AbstractFormCallback {
    private Widget status = null;

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (BeeUtils.same(name, TransportConstants.COL_STATUS)) {
        status = widget;
      }
    }

    @Override
    public void afterRefresh(FormView form, IsRow row) {
      String text = NameUtils.getName(OrderStatus.class,
          row.getInteger(form.getDataIndex(TransportConstants.COL_STATUS)));

      status.getElement().setInnerText(text);
    }

    @Override
    public OrderFormHandler getInstance() {
      return new OrderFormHandler();
    }

    @Override
    public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
      newRow.setValue(form.getDataIndex("Date"), System.currentTimeMillis());
      newRow.setValue(form.getDataIndex(TransportConstants.COL_STATUS),
          OrderStatus.CREATED.ordinal());
    }
  }

  private static class SparePartsGridHandler extends AbstractGridCallback
      implements SelectionHandler<IsRow> {

    private static final String FILTER_KEY = "f1";
    private IsRow selectedType = null;

    @Override
    public void afterCreateWidget(String name, Widget widget) {
      if (widget instanceof TreeView && BeeUtils.same(name, "SparePartTypes")) {
        ((TreeView) widget).addSelectionHandler(this);
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

    private void setSelectedType(IsRow selectedType) {
      this.selectedType = selectedType;
    }
  }

  private static class TripRoutesGridHandler extends AbstractGridCallback {
    private Map<Long, String> liters = null;

    @Override
    public void beforeRefresh(GridPresenter presenter) {
      LogUtils.severe(LogUtils.getDefaultLogger(), "refresh");
    }

    @Override
    public void beforeRequery(final GridPresenter presenter) {
      LogUtils.severe(LogUtils.getDefaultLogger(), "requery");

      liters = null;
      ParameterList args = TransportHandler.createArgs("GET_CONSUMPTION");
      args.addDataItem("Trip", 0);

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          Assert.notNull(response);

          if (response.hasErrors()) {
            Global.showError((Object[]) response.getErrors());

          } else if (response.hasResponse()) {
            Map<Long, String> data = Maps.newHashMap();
            String[] mapData = Codec.beeDeserializeCollection((String) response.getResponse());

            for (String entryData : mapData) {
              String[] entry = Codec.beeDeserializeCollection(entryData);
              data.put(BeeUtils.toLong(entry[0]), entry[1]);
            }
            liters = data;
            presenter.refresh();

          } else {
            Global.showError("Unknown response");
          }
        }
      });
    }

    @Override
    public GridCallback getInstance() {
      return new TripRoutesGridHandler();
    }

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
              cols.add((BeeColumn) DataUtils.getColumn(updColName, columns));
              values.add(row.getString(DataUtils.getColumnIndex(updColName, columns)));

              BeeRowSet rs = new BeeRowSet(viewName, cols);
              rs.addRow(row.getId(), row.getVersion(), values.toArray(new String[0]));
              rs.getRow(0).preliminaryUpdate(0, cv.getNewValue());

              ParameterList args = TransportHandler.createArgs("UPDATE_KILOMETERS");
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

                    if (liters != null) {
                      liters.remove(newRow.getId());
                    }
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
      } else if (BeeUtils.same(columnId, "Consumption") && column instanceof HasCellRenderer) {
        ((HasCellRenderer) column).setRenderer(new AbstractCellRenderer() {
          @Override
          public String render(final IsRow row) {
            LogUtils.warning(LogUtils.getDefaultLogger(), row.getId());

            if (liters == null) {
              return null;

            } else if (!liters.containsKey(row.getId())) {
              ParameterList args = TransportHandler.createArgs("GET_CONSUMPTION");
              args.addDataItem("Route", row.getId());

              BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
                @Override
                public void onResponse(ResponseObject response) {
                  Assert.notNull(response);

                  if (response.hasErrors()) {
                    Global.showError((Object[]) response.getErrors());

                  } else if (response.hasResponse()) {
                    if (liters != null) {
                      liters.put(row.getId(), (String) response.getResponse());
                    }
                    BeeKeeper.getBus().fireEvent(
                        new RowUpdateEvent(getGridPresenter().getDataProvider().getViewName(),
                            row));

                  } else {
                    Global.showError("Unknown response");
                  }
                }
              });
              return null;
            }
            return liters.get(row.getId());
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
        newRow.setValue(DataUtils.getColumnIndex("ModelName", cols),
            getModelValue(model, "ModelName"));
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
    FormFactory.registerFormCallback("TripOrder", new OrderFormHandler());
    GridFactory.registerGridCallback("Vehicles", new VehiclesGridHandler());
    GridFactory.registerGridCallback("SpareParts", new SparePartsGridHandler());
    GridFactory.registerGridCallback("TripRoutes", new TripRoutesGridHandler());
  }

  static ParameterList createArgs(String name) {
    ParameterList args = BeeKeeper.getRpc().createParameters(TransportConstants.TRANSPORT_MODULE);
    args.addQueryItem(TransportConstants.TRANSPORT_METHOD, name);
    return args;
  }

  private TransportHandler() {
  }
}
