package com.butent.bee.client.modules.service;

import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;

public class ServiceDefectForm extends PrintFormInterceptor {

  ServiceDefectForm() {
  }

  @Override
  public FormInterceptor getInstance() {
    return new ServiceDefectForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintServiceDefect();
  }
}
