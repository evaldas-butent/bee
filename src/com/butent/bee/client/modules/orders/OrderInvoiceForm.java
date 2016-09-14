package com.butent.bee.client.modules.orders;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.orders.OrdersConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class OrderInvoiceForm extends AbstractFormInterceptor {

  private Button confirmAction;
  private DataSelector series;

  @Override
  public FormInterceptor getInstance() {
    return new OrderInvoiceForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_SERIES)) {
      series = (DataSelector) widget;
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    HeaderView header = form.getViewPresenter().getHeader();
    int idx = form.getDataIndex(COL_SALE_PROFORMA);
    boolean proforma = !BeeConst.isUndef(idx) && row != null && BeeUtils.unbox(row.getBoolean(idx));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.dictionary().trProformaInvoice()
        : Localized.dictionary().trdInvoice());

    series.setEnabled(proforma);

    if (confirmAction == null) {
      confirmAction = new Button(Localized.dictionary().trdInvoice(), new ClickHandler() {

        @Override
        public void onClick(ClickEvent arg0) {
          Global.confirm(Localized.dictionary().trConfirmProforma(), new ConfirmationCallback() {
            @Override
            public void onConfirm() {

              if (Data.isNull(VIEW_ORDER_CHILD_INVOICES, row, COL_TRADE_SALE_SERIES)) {
                getFormView().notifySevere(
                    Localized.dictionary().trdInvoicePrefix() + " "
                        + Localized.dictionary().valueRequired());
                return;
              }

              Queries.update(getViewName(), IdFilter.compareId(getActiveRowId()),
                  COL_SALE_PROFORMA, BooleanValue.getNullValue(), new IntCallback() {
                    @Override
                    public void onSuccess(Integer result) {
                      if (BeeUtils.isPositive(result)) {
                        Data.onViewChange(getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
                      }
                    }
                  });
            }
          });
        }
      });
      header.addCommandItem(confirmAction);
    }
    confirmAction.setVisible(proforma && form.isEnabled());
  }
}

