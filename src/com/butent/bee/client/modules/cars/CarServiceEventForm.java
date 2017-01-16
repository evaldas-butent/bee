package com.butent.bee.client.modules.cars;

import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.filter.Filter;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class CarServiceEventForm extends AbstractFormInterceptor {

  private MultiSelector attendees;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (Objects.equals(name, TBL_ATTENDEES) && widget instanceof MultiSelector) {
      attendees = (MultiSelector) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    Long appointmentId = result.getLong(getDataIndex(COL_APPOINTMENT));

    if (!updateAttendees(appointmentId, true)) {
      updateAppointment(appointmentId, true);
    }
    super.afterInsertRow(result, forced);
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    updateAppointment(result.getLong(getDataIndex(COL_APPOINTMENT)), false);
    super.afterUpdateRow(result);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CarServiceEventForm();
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    updateAttendees(getLongValue(COL_APPOINTMENT), false);
    super.onSaveChanges(listener, event);
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    if (Objects.nonNull(attendees)) {
      Queries.getDistinctLongs(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE,
          Filter.equals(COL_APPOINTMENT, row.getLong(getDataIndex(COL_APPOINTMENT))),
          new RpcCallback<Set<Long>>() {
            @Override
            public void onSuccess(Set<Long> ids) {
              attendees.setIds(ids);
            }
          });
    }
    super.onSetActiveRow(row);
  }

  private boolean updateAttendees(Long appointmentId, boolean isNew) {
    boolean hasChanges = Objects.nonNull(attendees) && attendees.isValueChanged();

    if (hasChanges) {
      Queries.updateChildren(TBL_APPOINTMENTS, appointmentId,
          Collections.singleton(RowChildren.create(TBL_APPOINTMENT_ATTENDEES,
              COL_APPOINTMENT, appointmentId, COL_ATTENDEE, attendees.getValue())),
          getAppointmentCallback(isNew));
    }
    return hasChanges;
  }

  private static void updateAppointment(Long appointmentId, boolean isNew) {
    Queries.getRow(TBL_APPOINTMENTS, appointmentId, getAppointmentCallback(isNew));
  }

  private static RowCallback getAppointmentCallback(boolean isNew) {
    if (isNew) {
      return new RowInsertCallback(TBL_APPOINTMENTS) {
        @Override
        public void onSuccess(BeeRow result) {
          AppointmentEvent.fire(Appointment.create(result), State.CREATED);
          super.onSuccess(result);
        }
      };
    } else {
      return new RowUpdateCallback(TBL_APPOINTMENTS) {
        @Override
        public void onSuccess(BeeRow result) {
          AppointmentEvent.fire(Appointment.create(result), State.CHANGED);
          super.onSuccess(result);
        }
      };
    }
  }
}
