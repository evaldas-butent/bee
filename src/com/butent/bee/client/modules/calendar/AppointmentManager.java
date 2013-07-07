package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.time.DateTime;

import java.util.Collections;
import java.util.List;

public class AppointmentManager {

  private final List<Appointment> appointments = Lists.newArrayList();
  private Range<DateTime> appointmentRange;

  public AppointmentManager() {
    super();
  }

  public void addAppointment(Appointment appt) {
    if (appt != null) {
      appointments.add(appt);
    }
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }
  
  public void loadAppointments(long calendarId, final Range<DateTime> range, boolean force,
      final IntCallback callback) {
    
    if (!force && getAppointmentRange() != null && getAppointmentRange().encloses(range)) {
      if (callback != null) {
        callback.onSuccess(appointments.size());
      }
      return;
    }

    ParameterList params = CalendarKeeper.createRequestParameters(SVC_GET_CALENDAR_APPOINTMENTS);
    params.addQueryItem(PARAM_CALENDAR_ID, calendarId);
    
    if (range != null) {
      params.addQueryItem(PARAM_START_TIME, range.lowerEndpoint().getTime());
      params.addQueryItem(PARAM_END_TIME, range.upperEndpoint().getTime());
    }

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (Queries.checkResponse(SVC_GET_CALENDAR_APPOINTMENTS, VIEW_APPOINTMENTS, response,
            BeeRowSet.class, callback)) {

          BeeRowSet rowSet = BeeRowSet.restore((String) response.getResponse());
          appointments.clear();
          for (BeeRow row : rowSet.getRows()) {
            appointments.add(new Appointment(row));
          }
          
          setAppointmentRange(range);
          
          if (callback != null) {
            callback.onSuccess(appointments.size());
          }
        }
      }
    });
  }

  public boolean removeAppointment(long id) {
    int index = getAppointmentIndex(id);
    if (BeeConst.isUndef(index)) {
      return false;
    }  

    appointments.remove(index);
    return true;
  }

  public void sortAppointments() {
    Collections.sort(appointments);
  }
  
  private int getAppointmentIndex(long id) {
    for (int i = 0; i < appointments.size(); i++) {
      if (appointments.get(i).getId() == id) {
        return i;
      }
    }
    return BeeConst.UNDEF;
  }

  private Range<DateTime> getAppointmentRange() {
    return appointmentRange;
  }

  private void setAppointmentRange(Range<DateTime> appointmentRange) {
    this.appointmentRange = appointmentRange;
  }
}