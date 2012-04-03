package com.butent.bee.client.modules.transport;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.grid.AbstractColumn;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.presenter.TreePresenter;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.utils.AbstractEvaluation;
import com.butent.bee.client.utils.Evaluator.Parameters;
import com.butent.bee.client.utils.HasEvaluation;
import com.butent.bee.client.view.TreeView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.modules.transport.TransportConstants.OrderStatus;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

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
      String text = BeeUtils.getName(OrderStatus.class,
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

  private static class OrdersGridHandler extends AbstractGridCallback {
    @Override
    public boolean afterCreateColumn(String columnId, AbstractColumn<?> column,
        ColumnHeader header, ColumnFooter footer) {

      if (BeeUtils.same(columnId, TransportConstants.COL_STATUS) && column instanceof HasEvaluation) {
        ((HasEvaluation) column).setEvaluation(new AbstractEvaluation() {
          @Override
          public String eval(Parameters parameters) {
            return BeeUtils.getName(OrderStatus.class,
                parameters.getInteger(TransportConstants.COL_STATUS));
          }
        });
      }
      return true;
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
    GridFactory.registerGridCallback("TripOrders", new OrdersGridHandler());
    FormFactory.registerFormCallback("TripOrder", new OrderFormHandler());
    GridFactory.registerGridCallback("Vehicles", new VehiclesGridHandler());
    GridFactory.registerGridCallback("SpareParts", new SparePartsGridHandler());
  }

  private TransportHandler() {
  }
}
