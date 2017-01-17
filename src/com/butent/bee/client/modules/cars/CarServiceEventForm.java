package com.butent.bee.client.modules.cars;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_ORDER_NO;

import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class CarServiceEventForm extends AbstractFormInterceptor implements ClickHandler {

  private Flow orderContainer;
  private MultiSelector attendees;

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (Objects.equals(name, COL_SERVICE_ORDER) && widget instanceof Flow) {
      orderContainer = (Flow) widget;
      orderContainer.addClickHandler(this);
    }
    if (Objects.equals(name, TBL_ATTENDEES) && widget instanceof MultiSelector) {
      attendees = (MultiSelector) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterInsertRow(IsRow result, boolean forced) {
    updateAttendees(result.getLong(getDataIndex(COL_APPOINTMENT)), true);
    super.afterInsertRow(result, forced);
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    updateAttendees(result.getLong(getDataIndex(COL_APPOINTMENT)), false);
    super.afterUpdateRow(result);
  }

  @Override
  public FormInterceptor getInstance() {
    return new CarServiceEventForm();
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    Long orderId = getLongValue(COL_SERVICE_ORDER);

    if (DataUtils.isId(orderId)) {
      RowEditor.open(TBL_SERVICE_ORDERS, orderId, Opener.MODAL);
    } else {
      RowFactory.createRow(TBL_SERVICE_ORDERS, null, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          IsRow row = getActiveRow();
          row.setValue(getDataIndex(COL_SERVICE_ORDER), result.getId());
          refreshOrder(row);
        }
      });
    }
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (event.isEmpty()) {
      afterUpdateRow(getActiveRow());
    }
    super.onSaveChanges(listener, event);
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    if (Objects.nonNull(attendees)) {
      if (DataUtils.isNewRow(row)) {
        attendees.setIds(DataUtils.parseIdSet(row.getProperty(TBL_ATTENDEES)));
        row.removeProperty(TBL_ATTENDEES);
      } else {
        Queries.getDistinctLongs(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE,
            Filter.equals(COL_APPOINTMENT, row.getLong(getDataIndex(COL_APPOINTMENT))),
            new RpcCallback<Set<Long>>() {
              @Override
              public void onSuccess(Set<Long> ids) {
                attendees.setIds(ids);
              }
            });
      }
    }
    refreshOrder(row);
    super.onSetActiveRow(row);
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

  private void refreshOrder(IsRow row) {
    if (Objects.nonNull(orderContainer)) {
      Long orderId = row.getLong(getDataIndex(COL_SERVICE_ORDER));

      if (DataUtils.isId(orderId)) {
        Queries.getValue(TBL_SERVICE_ORDERS, orderId, COL_ORDER_NO, new RpcCallback<String>() {
          @Override
          public void onSuccess(String orderNo) {
            orderContainer.getElement()
                .setInnerText(BeeUtils.joinWords(Localized.dictionary().serviceOrder(), orderNo));
          }
        });
      } else {
        orderContainer.getElement().setInnerText(Localized.dictionary().newServiceOrder());
      }
    }
  }

  private void updateAttendees(Long appointmentId, boolean isNew) {
    if (Objects.nonNull(attendees)) {
      Queries.updateChildren(TBL_APPOINTMENTS, appointmentId,
          Collections.singleton(RowChildren.create(TBL_APPOINTMENT_ATTENDEES,
              COL_APPOINTMENT, appointmentId, COL_ATTENDEE, attendees.getValue())),
          getAppointmentCallback(isNew));
    }
  }
}
