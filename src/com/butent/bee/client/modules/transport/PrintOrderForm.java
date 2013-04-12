package com.butent.bee.client.modules.transport;

import com.butent.bee.client.ui.AbstractFormInterceptor;
import com.butent.bee.client.ui.FormFactory.FormInterceptor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.data.IsRow;

public class PrintOrderForm extends AbstractFormInterceptor {

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    AssessmentForm.updateTotal(form, row, form.getWidgetByName("Total"));
  }

  @Override
  public FormInterceptor getInstance() {
    return new PrintOrderForm();
  }
}
