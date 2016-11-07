package com.butent.bee.client.modules.service;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.data.IsRow;


public class MaintenanceCommentForm extends AbstractFormInterceptor {

  private IsRow serviceMaintenance;

  public MaintenanceCommentForm() {
    this.serviceMaintenance = null;
  }

  MaintenanceCommentForm(IsRow serviceMaintenance) {
    this();
    this.serviceMaintenance = serviceMaintenance;
  }

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    super.onStartNewRow(form, oldRow, newRow);
    newRow.setValue(Data.getColumnIndex(form.getViewName(), COL_SERVICE_MAINTENANCE),
        serviceMaintenance.getId());

    newRow.setValue(Data.getColumnIndex(form.getViewName(), COL_EVENT_NOTE),
        serviceMaintenance.getString(Data.getColumnIndex(COL_SERVICE_MAINTENANCE,
            ALS_STATE_NAME)));
  }

  @Override
  public FormInterceptor getInstance() {
    return new MaintenanceCommentForm(serviceMaintenance);
  }
}