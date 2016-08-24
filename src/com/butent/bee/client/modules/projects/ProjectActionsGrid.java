package com.butent.bee.client.modules.projects;

import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.projects.ProjectConstants;

public class ProjectActionsGrid extends AbstractGridInterceptor {

  private IsRow projectRow;

  @Override
  public GridInterceptor getInstance() {
    return new ProjectActionsGrid();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();
    CalendarKeeper.openAppointment(new Appointment((BeeRow) event.getRowValue()), null,
        ProjectConstants.FORM_PROJECT_ACTION);

  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    if (DataUtils.isNewRow(projectRow) && getGridView() != null) {
      getGridView().ensureRelId(result -> {
        FormView parentForm = ViewHelper.getForm(getGridView().asWidget());
        if (parentForm != null && parentForm.getActiveRow() != null) {
          CalendarKeeper.createAppointment(null, null, null,
              null, null, null, parentForm.getActiveRow(), ProjectConstants.FORM_PROJECT_ACTION);
        }
      });

    } else {
      CalendarKeeper.createAppointment(null, null, null,
          null, null, null, projectRow, ProjectConstants.FORM_PROJECT_ACTION);
    }

    return false;
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (event.getRow() != null && event.getViewName().equals(ProjectConstants.VIEW_PROJECTS)) {
      projectRow = event.getRow();
    }

  }

}