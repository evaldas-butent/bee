package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

class AppointmentGridHandler extends AbstractGridCallback {

  AppointmentGridHandler() {
    super();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnId, List<? extends IsColumn> dataColumns,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnId, CalendarConstants.NAME_START)) {
      return new AppointmentTimeRenderer(dataColumns, true);
    } else if (BeeUtils.same(columnId, CalendarConstants.NAME_END)) {
      return new AppointmentTimeRenderer(dataColumns, false);
    } else {
      return null;
    }
  }
}
