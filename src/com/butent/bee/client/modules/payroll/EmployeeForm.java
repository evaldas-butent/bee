package com.butent.bee.client.modules.payroll;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.layout.SummaryProxy;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.rights.Module;
import com.butent.bee.shared.rights.ModuleAndSub;

class EmployeeForm extends AbstractFormInterceptor {

  EmployeeForm() {
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    if (BeeKeeper.getUser().isModuleVisible(ModuleAndSub.of(Module.PAYROLL))) {
      Widget container = form.getWidgetByName("WorkSchedule", false);
      if (container instanceof SummaryProxy) {
        SummaryProxy panel = (SummaryProxy) container;

        if (DataUtils.hasId(row)) {
          EmployeeSchedule widget = new EmployeeSchedule(row.getId());
          panel.setWidget(widget);

          widget.refresh();

        } else if (!panel.isEmpty()) {
          panel.clear();
        }
      }
    }

    super.afterRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new EmployeeForm();
  }
}
