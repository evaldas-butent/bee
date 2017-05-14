package com.butent.bee.client.modules.trade;

import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.modules.trade.DebtKind;

class PaymentForm extends AbstractFormInterceptor {

  private final DebtKind debtKind;

  PaymentForm(DebtKind debtKind) {
    this.debtKind = debtKind;
  }

  @Override
  public FormInterceptor getInstance() {
    return new PaymentForm(debtKind);
  }
}
