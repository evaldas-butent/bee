package com.butent.bee.client.modules.transport;

import static com.butent.bee.shared.modules.trade.TradeConstants.TBL_PURCHASE_ITEMS;
import static com.butent.bee.shared.modules.transport.TransportConstants.VIEW_CARGO_CREDIT_INCOMES;

import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.shared.utils.BeeUtils;

public class CargoCreditInvoiceForm extends AbstractFormInterceptor {

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof ChildGrid) {
      ChildGrid grid = (ChildGrid) widget;

      if (BeeUtils.same(name, TBL_PURCHASE_ITEMS)) {
        grid.setGridInterceptor(new InvoiceItemsGrid(getFormView()));

      } else if (BeeUtils.same(name, VIEW_CARGO_CREDIT_INCOMES)) {
        grid.setGridInterceptor(new AbstractGridInterceptor());
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new CargoInvoiceForm();
  }
}
