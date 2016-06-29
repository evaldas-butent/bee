package com.butent.bee.client.modules.orders;

import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.event.logical.SelectorEvent.Handler;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Arrays;
import java.util.Map;

public class NewOrderInvoiceForm extends AbstractFormInterceptor {

  private static final String NAME_SERIES_LABEL = "SeriesLabel";
  private Label seriesLabel;

  @Override
  public FormInterceptor getInstance() {
    return new NewOrderInvoiceForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, NAME_SERIES_LABEL)) {
      seriesLabel = (Label) widget;
      seriesLabel.setStyleName(StyleUtils.NAME_REQUIRED, true);
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    createCellValidationHandler(form, row);
  }

  private void createCellValidationHandler(FormView form, IsRow row) {
    if (form == null || row == null) {
      return;
    }

    form.addCellValidationHandler(COL_SALE_PROFORMA, new CellValidateEvent.Handler() {

      @Override
      public Boolean validateCell(CellValidateEvent event) {
        getSeriesRequired(event.getNewValue());
        return true;
      }
    });
  }

  private void getSeriesRequired(String newValue) {
    boolean valueRequired = !BeeUtils.isEmpty(newValue);
    seriesLabel.setStyleName(StyleUtils.NAME_REQUIRED, !valueRequired);
  }
}
