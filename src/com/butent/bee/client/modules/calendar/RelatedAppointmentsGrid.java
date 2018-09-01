package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.time.DateTime;

public class RelatedAppointmentsGrid extends AbstractGridInterceptor {

  private final String relation;

  public RelatedAppointmentsGrid(String relation) {
    this.relation = relation;
  }

  @Override
  public GridInterceptor getInstance() {
    return new RelatedAppointmentsGrid(relation);
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    event.consume();
    CalendarKeeper.openAppointment(Appointment.create(event.getRowValue()), null, null, null);
  }

  @Override
  public boolean onStartNewRow(GridView gridView, IsRow oldRow, IsRow newRow, boolean copy) {
    FormView form = ViewHelper.getForm(gridView);

    CalendarKeeper.ensureData(new Command() {
      @Override
      public void execute() {
        CalendarKeeper.createAppointment(null, new DateTime(), null, null, beeRow -> {
          DataInfo sourceInfo = Data.getDataInfo(form.getViewName());
          DataInfo targetInfo = Data.getDataInfo(gridView.getViewName());

          RelationUtils.updateRow(targetInfo, relation, beeRow, sourceInfo,
              form.getActiveRow(), true);

        }, result -> Data.refreshLocal(gridView.getViewName()));
      }
    });

    return false;
  }
}
