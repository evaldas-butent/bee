package com.butent.bee.client.modules.service;

import com.butent.bee.client.modules.transport.CargoPurchaseInvoiceForm;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.trade.TradeConstants;

public class ServiceInvoiceForm extends CargoPurchaseInvoiceForm {

  ServiceInvoiceForm() {
    super();
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
  }

  @Override
  public FormInterceptor getInstance() {
    return new ServiceInvoiceForm();
  }

  @Override
  protected String getTradeItemsName() {
    return TradeConstants.TBL_SALE_ITEMS;
  }
}
