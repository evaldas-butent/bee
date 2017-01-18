package com.butent.bee.client.modules.cars;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;
import static com.butent.bee.shared.modules.cars.CarsConstants.*;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.COL_TRADE_CUSTOMER;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.communication.RpcCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.MultiSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.data.RowInsertCallback;
import com.butent.bee.client.data.RowUpdateCallback;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.calendar.CalendarPanel;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputTime;
import com.butent.bee.shared.State;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.RowChildren;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class CarServiceEventForm extends AbstractFormInterceptor implements ClickHandler,
    SelectorEvent.Handler {

  private Flow orderContainer;
  private MultiSelector attendees;

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (Objects.equals(editableWidget.getColumnId(), COL_CAR) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(this);
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

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
    commitAttendees(result.getLong(getDataIndex(COL_APPOINTMENT)), true);
    super.afterInsertRow(result, forced);
  }

  @Override
  public void afterUpdateRow(IsRow result) {
    commitAttendees(result.getLong(getDataIndex(COL_APPOINTMENT)), false);
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
      DataInfo eventInfo = Data.getDataInfo(getViewName());
      IsRow eventRow = getActiveRow();
      DataInfo orderInfo = Data.getDataInfo(TBL_SERVICE_ORDERS);
      BeeRow orderRow = RowFactory.createEmptyRow(orderInfo);

      orderRow.setValue(orderInfo.getColumnIndex(COL_NOTES),
          eventRow.getString(eventInfo.getColumnIndex(CalendarConstants.COL_DESCRIPTION)));

      ImmutableMap.of(COL_COMPANY, COL_TRADE_CUSTOMER, COL_COMPANY_PERSON,
          COL_TRADE_CUSTOMER + COL_PERSON, COL_CAR, COL_CAR).forEach((s, t) ->
          RelationUtils.copyWithDescendants(eventInfo, s, eventRow, orderInfo, t, orderRow));

      RowFactory.createRow(orderInfo, orderRow, null, new RowCallback() {
        @Override
        public void onSuccess(BeeRow result) {
          RelationUtils.updateRow(eventInfo, COL_SERVICE_ORDER, eventRow, orderInfo, result, true);
          refreshOrder(eventRow);
        }
      });
    }
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isNewRow()) {
      RelationUtils.copyWithDescendants(Data.getDataInfo(getViewName()), COL_COMPANY,
          getActiveRow(), Data.getDataInfo(event.getRelatedViewName()), COL_OWNER,
          event.getNewRow());

    } else if (event.isOpened()) {
      Long owner = getLongValue(COL_COMPANY);
      event.getSelector().setAdditionalFilter(Objects.isNull(owner) ? null
          : Filter.equals(COL_OWNER, owner));
    }
  }

  @Override
  public void onLoad(FormView form) {
    FaLabel cal = new FaLabel(FontAwesome.CALENDAR);
    cal.setTitle(Localized.dictionary().startingTime());

    cal.addClickHandler(clickEvent -> BeeKeeper.getRpc()
        .makeGetRequest(CarsKeeper.createSvcArgs(SVC_GET_CALENDAR), new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            CalendarKeeper.openCalendar(response.getResponseAsLong(), result -> {
              if (result instanceof CalendarPanel) {
                CalendarPanel panel = (CalendarPanel) result;

                panel.setTimePickerCallback((dateTime, attendee) -> {
                  if (BeeUtils.allNotNull(attendees, attendee)) {
                    attendees.setIds(Collections.singletonList(attendee));
                  }
                  updateTime(dateTime);
                  UiHelper.closeDialog(panel);
                });
                StyleUtils.setWidth(panel, BeeKeeper.getScreen().getWidth() * 0.7, CssUnit.PX);
                StyleUtils.setHeight(panel, BeeKeeper.getScreen().getHeight() * 0.9, CssUnit.PX);

                Global.showModalWidget(cal.getTitle(), panel);
              }
            });
          }
        }));
    getHeaderView().addCommandItem(cal);
    super.onLoad(form);
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
      Long appointmentId = row.getLong(getDataIndex(COL_APPOINTMENT));

      if (DataUtils.isId(appointmentId)) {
        Queries.getDistinctLongs(TBL_APPOINTMENT_ATTENDEES, COL_ATTENDEE,
            Filter.equals(COL_APPOINTMENT, appointmentId), new RpcCallback<Set<Long>>() {
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

  @Override
  public void onStartNewRow(FormView form, IsRow oldRow, IsRow newRow) {
    if (BeeUtils.isEmpty(newRow.getProperty(COL_DURATION))) {
      newRow.setProperty(COL_DURATION, BeeUtils.toString(TimeUtils.MINUTES_PER_HOUR));
    }
    super.onStartNewRow(form, oldRow, newRow);
  }

  private void commitAttendees(Long appointmentId, boolean isNew) {
    if (Objects.nonNull(attendees)) {
      Queries.updateChildren(TBL_APPOINTMENTS, appointmentId,
          Collections.singleton(RowChildren.create(TBL_APPOINTMENT_ATTENDEES,
              COL_APPOINTMENT, appointmentId, COL_ATTENDEE, attendees.getValue())),
          getAppointmentCallback(isNew));
    }
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

      orderContainer.getElement().setInnerText(DataUtils.isId(orderId)
          ? BeeUtils.joinWords(Localized.dictionary().serviceOrder(),
          row.getString(getDataIndex(COL_ORDER_NO)))
          : Localized.dictionary().newServiceOrder());

      orderContainer.setVisible(!Objects.equals(orderId, DataUtils.NEW_ROW_ID));
    }
  }

  private void updateTime(DateTime startTime) {
    Long startMillis = getLongValue(COL_START_DATE_TIME);
    Long endMillis = getLongValue(COL_END_DATE_TIME);

    getActiveRow().setValue(getDataIndex(COL_START_DATE_TIME), startTime);
    getFormView().refreshBySource(COL_START_DATE_TIME);

    if (BeeUtils.allNotNull(startMillis, endMillis)) {
      getActiveRow().setValue(getDataIndex(COL_END_DATE_TIME),
          endMillis + (startTime.getTime() - startMillis));
      getFormView().refreshBySource(COL_END_DATE_TIME);
    }
  }
}
