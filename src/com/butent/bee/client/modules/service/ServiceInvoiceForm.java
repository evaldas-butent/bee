package com.butent.bee.client.modules.service;

import com.butent.bee.client.modules.trade.TradeDocumentRenderer;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.modules.trade.TradeConstants;

public class ServiceInvoiceForm extends PrintFormInterceptor {

  ServiceInvoiceForm() {
    super();
  }

  @Override
  public FormInterceptor getInstance() {
    return new ServiceInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new TradeDocumentRenderer(TradeConstants.VIEW_SALE_ITEMS, TradeConstants.COL_SALE);
  }
}
