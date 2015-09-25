package com.butent.bee.client.modules.payroll;

import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.xml.client.Element;

import static com.butent.bee.shared.modules.payroll.PayrollConstants.*;

import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.i18n.Localized;

class WorkScheduleEditor extends AbstractFormInterceptor {

  private final boolean showGrid;

  WorkScheduleEditor(boolean showGrid) {
    this.showGrid = showGrid;
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (GRID_WORK_SCHEDULE_DAY.equals(name)) {
      return showGrid;
    } else {
      return super.beforeCreateWidget(name, description);
    }
  }

  @Override
  public FormInterceptor getInstance() {
    return new WorkScheduleEditor(showGrid);
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
