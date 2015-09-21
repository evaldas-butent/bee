package com.butent.bee.client.modules.payroll;

import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;

class WorkScheduleForm extends AbstractFormInterceptor {

  WorkScheduleForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new WorkScheduleForm();
  }
}
