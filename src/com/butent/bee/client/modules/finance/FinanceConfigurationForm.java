package com.butent.bee.client.modules.finance;

import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;

class FinanceConfigurationForm extends AbstractFormInterceptor {

  FinanceConfigurationForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new FinanceConfigurationForm();
  }

  @Override
  public void onStart(FormView form) {
    super.onStart(form);

    if (form != null && form.getRowCount() <= 0) {
      form.startNewRow(false);
    }
  }
}
