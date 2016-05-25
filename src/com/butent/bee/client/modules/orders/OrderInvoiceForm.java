package com.butent.bee.client.modules.orders;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
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

public class OrderInvoiceForm extends AbstractFormInterceptor implements ClickHandler {

  private Button confirmAction;

  @Override
  public FormInterceptor getInstance() {
    return new OrderInvoiceForm();
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    int idx = form.getDataIndex(COL_SALE_PROFORMA);
    boolean proforma = !BeeConst.isUndef(idx) && row != null && BeeUtils.unbox(row.getBoolean(idx));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.dictionary().trProformaInvoice()
        : Localized.dictionary().trdInvoice());

    if (confirmAction == null) {
      confirmAction = new Button(Localized.dictionary().trdInvoice(), this);
      form.getViewPresenter().getHeader().addCommandItem(confirmAction);
    }
    confirmAction.setVisible(proforma && form.isEnabled());
  }

  @Override
  public void onClick(ClickEvent event) {
    Global.confirm(Localized.dictionary().trConfirmProforma(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
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
}
