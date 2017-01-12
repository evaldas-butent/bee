package com.butent.bee.client.modules.cars;

import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.TBL_STAGES;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_DOCUMENT_VAT_MODE;

import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.administration.Stage;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HasStages;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.interceptor.ParentRowRefreshGrid;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Objects;

public class CarServiceOrderForm extends PrintFormInterceptor implements HasStages {

  private HasWidgets stageContainer;
  private List<Stage> orderStages;

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
