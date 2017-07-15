package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;

public class TradeSaleInvoiceForm extends PrintFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new TradeSaleInvoiceForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintActForm();
  }
}
