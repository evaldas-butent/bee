package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarConstants.ItemType;
import com.butent.bee.shared.modules.calendar.CalendarTask;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CalendarDataManager {

  private static BeeLogger logger = LogUtils.getLogger(CalendarDataManager.class);
  
  private final List<Appointment> appointments = Lists.newArrayList();
  private final List<CalendarTask> tasks = Lists.newArrayList();

  private Range<DateTime> range;

  public CalendarDataManager() {
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

  public void loadAppointments(long calendarId, final Range<DateTime> calendarRange, boolean force,
      final IntCallback callback) {

    if (!force && getRange() != null && calendarRange != null
        && getRange().encloses(calendarRange)) {
      if (callback != null) {
        callback.onSuccess(appointments.size());
      }
      return;
    }

    ParameterList params = CalendarKeeper.createRequestParameters(SVC_GET_CALENDAR_ITEMS);
    params.addQueryItem(PARAM_CALENDAR_ID, calendarId);

    if (range != null) {
      params.addQueryItem(PARAM_START_TIME, range.lowerEndpoint().getTime());
      params.addQueryItem(PARAM_END_TIME, range.upperEndpoint().getTime());
    }

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        appointments.clear();
        tasks.clear();

        if (!response.isEmpty()) {
          Map<String, String> data = Codec.beeDeserializeMap(response.getResponseAsString());
          
          for (Map.Entry<String, String> entry : data.entrySet()) {
            ItemType type = EnumUtils.getEnumByName(ItemType.class, entry.getKey());
            if (type == null) {
              logger.severe("item type not recognized", entry.getKey());
              continue;
            }
            
            String[] items = Codec.beeDeserializeCollection(entry.getValue());
            if (ArrayUtils.isEmpty(items)) {
              logger.warning("items is empty", entry.getKey());
              continue;
            }
            
            for (String item : items) {
              switch (type) {
                case APPOINTMENT:
                  appointments.add(new Appointment(BeeRow.restore(item)));
                  break;
                case TASK:
                  tasks.add(CalendarTask.restore(item));
                  break;
              }
            }
          }
        }

        setRange(calendarRange);
        if (callback != null) {
          callback.onSuccess(getSize());
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

  private Range<DateTime> getRange() {
    return range;
  }

  private int getSize() {
    return appointments.size() + tasks.size();
  }

  private void setRange(Range<DateTime> range) {
    this.range = range;
  }
}