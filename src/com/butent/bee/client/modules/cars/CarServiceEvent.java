package com.butent.bee.client.modules.cars;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.VIEW_APPOINTMENTS;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;

import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.shared.data.IsRow;

public class CarServiceEvent extends Appointment {

  public CarServiceEvent(IsRow row, Long separatedAttendee) {
    super(row, separatedAttendee);
  }

  @Override
  public boolean open() {
    RowEditor.open(TBL_SERVICE_EVENTS, getEventId(), Opener.MODAL);
    return true;
  }

  public Long getEventId() {
    return Data.getLong(VIEW_APPOINTMENTS, getRow(), COL_SERVICE_EVENT);
  }
}
