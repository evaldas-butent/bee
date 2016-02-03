package com.butent.bee.client.modules.transport;

import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.IsRow;

class AssessmentPrintForm extends AbstractFormInterceptor {
  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    for (String name : new String[] {COL_CUSTOMER, COL_FORWARDER}) {
      Widget widget = form.getWidgetByName(name);

      if (widget != null) {
        ClassifierUtils.getCompanyInfo(row.getLong(form.getDataIndex(name)), widget, name);
      }
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return null;
  }
}