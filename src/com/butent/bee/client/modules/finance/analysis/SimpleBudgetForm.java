package com.butent.bee.client.modules.finance.analysis;

import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;

public class SimpleBudgetForm extends AbstractFormInterceptor{

  public SimpleBudgetForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new SimpleBudgetForm();
  }
}
