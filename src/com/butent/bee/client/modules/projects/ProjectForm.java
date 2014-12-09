package com.butent.bee.client.modules.projects;

import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;

class ProjectForm extends AbstractFormInterceptor {

  @Override
  public FormInterceptor getInstance() {
    return new ProjectForm();
  }
}
