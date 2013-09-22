package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.VIEW_CARGO_INVOICE_INCOMES;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.filter.IdFilter;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoInvoiceForm extends CargoCreditInvoiceForm implements ClickHandler {

  private final Button confirmAction = new Button(Localized.getConstants().trInvoice(), this);

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, TBL_SALE_ITEMS)) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getFormView()));

      } else if (BeeUtils.same(name, VIEW_CARGO_INVOICE_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor());
      }
    }
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    int idx = DataUtils.getColumnIndex(COL_SALE_PROFORMA, form.getDataColumns());

    if (idx == BeeConst.UNDEF) {
      return;
    }
    boolean proforma = BeeUtils.unbox(row.getBoolean(idx));

    form.getViewPresenter().getHeader().setCaption(proforma
        ? Localized.getConstants().trProformaInvoice()
        : Localized.getConstants().trInvoice());

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (proforma) {
      header.addCommandItem(confirmAction);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoInvoiceForm();
  }

  @Override
  public void onClick(ClickEvent event) {
    final FormView form = getFormView();

    Global.confirm(Localized.getConstants().trConfirmProforma(), new ConfirmationCallback() {
      @Override
      public void onConfirm() {
        Queries.update(form.getViewName(),
            IdFilter.compareId(form.getActiveRow().getId()),
            COL_SALE_PROFORMA, BooleanValue.getNullValue(), new IntCallback() {
              @Override
              public void onSuccess(Integer result) {
                if (BeeUtils.isPositive(result)) {
                  Data.onViewChange(form.getViewName(), DataChangeEvent.CANCEL_RESET_REFRESH);
                }
              }
            });
      }
    });
  }
}
