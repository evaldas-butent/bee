package com.butent.bee.client.modules.payroll;

import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.grid.GridPanel;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.i18n.Localized;

class WorkScheduleEditor extends AbstractFormInterceptor {

  private final GridInterceptor gridInterceptor = new AbstractGridInterceptor() {
    @Override
    public void afterDeleteRow(long rowId) {
      dayRefresher.run();
    }

    @Override
    public void afterInsertRow(IsRow result) {
      dayRefresher.run();
    }

    @Override
    public void afterUpdateCell(IsColumn column, String oldValue, String newValue, IsRow result,
        boolean rowMode) {
      dayRefresher.run();
    }

    @Override
    public void afterUpdateRow(IsRow result) {
      dayRefresher.run();
    }

    @Override
    public GridInterceptor getInstance() {
      return null;
    }
  };

  private final Runnable dayRefresher;

  WorkScheduleEditor(Runnable dayRefresher) {
    this.dayRefresher = dayRefresher;
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof GridPanel && dayRefresher != null) {
      ((GridPanel) widget).setGridInterceptor(gridInterceptor);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new WorkScheduleEditor(dayRefresher);
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    boolean tr = event.containsColumn(COL_TIME_RANGE_CODE);
    boolean tc = event.containsColumn(COL_TIME_CARD_CODE);

    boolean fr = event.containsColumn(COL_WORK_SCHEDULE_FROM);
    boolean to = event.containsColumn(COL_WORK_SCHEDULE_UNTIL);

    boolean du = event.containsColumn(COL_WORK_SCHEDULE_DURATION);

    boolean ok = tr && !tc && !fr && !to && !du
        || !tr && tc && !fr && !to && !du
        || !tr && !tc && fr && (to || du)
        || !tr && !tc && !fr && !to && du;

    if (!ok) {
      event.consume();
      event.getCallback().onFailure(Localized.getConstants().error());
    }
  }
}
