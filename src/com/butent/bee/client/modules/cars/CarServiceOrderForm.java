package com.butent.bee.client.modules.cars;

import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.TBL_STAGES;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_DOCUMENT_VAT_MODE;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.administration.Stage;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HasStages;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Objects;

public class CarServiceOrderForm extends PrintFormInterceptor implements HasStages,
    SelectorEvent.Handler {

  private HasWidgets stageContainer;
  private List<Stage> orderStages;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (Objects.equals(editableWidget.getColumnId(), COL_CAR) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(this);
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      switch (name) {
        case TBL_SERVICE_ORDER_JOBS:
          ((ChildGrid) widget).setGridInterceptor(new CarServiceJobsGrid());
          break;
        case TBL_SERVICE_ORDER_ITEMS:
          ((ChildGrid) widget).setGridInterceptor(new ParentRowRefreshGrid());
          break;
        case TBL_SERVICE_EVENTS:
          ((ChildGrid) widget).setGridInterceptor(new CarServiceEventsGrid());
          break;
      }
    }
    if (Objects.equals(name, TBL_STAGES) && widget instanceof HasWidgets) {
      stageContainer = (HasWidgets) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    refreshStages();
    super.beforeRefresh(form, row);
  }

  @Override
  public HasWidgets getStageContainer() {
    return stageContainer;
  }

  @Override
  public List<Stage> getStages() {
    return orderStages;
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    DataInfo eventInfo = Data.getDataInfo(getViewName());
    DataInfo carInfo = Data.getDataInfo(event.getRelatedViewName());
    Long owner = getLongValue(COL_CUSTOMER);

    if (event.isNewRow()) {
      RelationUtils.copyWithDescendants(eventInfo, COL_CUSTOMER, getActiveRow(),
          carInfo, COL_OWNER, event.getNewRow());

    } else if (event.isOpened()) {
      event.getSelector().setAdditionalFilter(Objects.isNull(owner) ? null
          : Filter.equals(COL_OWNER, owner));

    } else if (event.isChanged() && Objects.isNull(owner)) {
      RelationUtils.copyWithDescendants(carInfo, COL_OWNER, event.getRelatedRow(),
          eventInfo, COL_CUSTOMER, getActiveRow());
      getFormView().refresh();
    }
  }

  @Override
  public void onSourceChange(IsRow row, String source, String value) {
    if (!DataUtils.isNewRow(row) && !BeeUtils.isEmpty(source)) {
      switch (source) {
        case COL_TRADE_DOCUMENT_VAT_MODE:
          BeeRowSet rs = DataUtils.getUpdated(getViewName(), row.getId(), row.getVersion(),
              DataUtils.getColumn(source, getDataColumns()),
              getFormView().getOldRow().getString(getDataIndex(source)), value);

          if (!DataUtils.isEmpty(rs)) {
            Queries.updateRow(rs, RowUpdateCallback.refreshRow(getViewName(), true));
          }
          break;
      }
    }
    super.onSourceChange(row, source, value);
  }

  @Override
  public void setStages(List<Stage> stages) {
    orderStages = stages;
  }

  @Override
  public FormInterceptor getInstance() {
    return new CarServiceOrderForm();
  }
}
