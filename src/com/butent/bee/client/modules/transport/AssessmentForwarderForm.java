package com.butent.bee.client.modules.transport;

import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;

public class AssessmentForwarderForm extends PrintFormInterceptor {

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new AssessmentPrintForm();
  }

  @Override
  public FormInterceptor getInstance() {
    return new AssessmentForwarderForm();
  }

}
