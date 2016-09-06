package com.butent.bee.client.modules.projects;

import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.calendar.CalendarPanel;
import com.butent.bee.client.modules.calendar.CalendarWidget;
import com.butent.bee.client.modules.calendar.event.AppointmentEvent;
import com.butent.bee.client.modules.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarItem;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.ui.Action;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ProjectCalendar extends CalendarPanel {

  private BeeRowSet userCalendarRowSet = Data.createRowSet(TBL_USER_CALENDARS);
  private BeeRow settingsRow;
  private boolean enabledEditing;
  private IsRow projectRow;

  public ProjectCalendar(IsRow projectRow, BeeRow settingsRow, BeeRowSet ucAttendees,
      boolean enabledEditing) {
    super(BeeConst.UNDEF, "",
        CalendarSettings.create(settingsRow, Data.getDataInfo(TBL_USER_CALENDARS).getColumns()),
        ucAttendees, projectRow.getId());

    this.settingsRow = settingsRow;
    this.enabledEditing = enabledEditing;
    this.projectRow = projectRow;

  }

  @Override
  protected void createAppointment(TimeBlockClickEvent event) {
    if (enabledEditing) {
      CalendarKeeper.createAppointment(null, event.getStart(), null,
          event.getAttendeeId(), null, null, projectRow, null);
    }
  }

  @Override
  protected void openAppointment(OpenEvent<CalendarItem> event) {
    if (enabledEditing) {
      CalendarItem item = event.getTarget();

      if (item.getItemType().equals(ItemType.APPOINTMENT)) {
        CalendarKeeper.openAppointment((Appointment) item, null);
      }
    }
  }

  @Override
  protected CalendarWidget createCalendarWidget(CalendarSettings settings) {
    return new CalendarWidget(BeeConst.UNDEF, settings, getObjectId());
  }

  @Override
  protected void editSettings() {
    userCalendarRowSet.addRow(settingsRow);
    FormFactory.createFormView(FORM_CALENDAR_SETTINGS, TBL_USER_CALENDARS,
        userCalendarRowSet.getColumns(), false, (formDescription, result) -> {
          FormView settingsForm;
          if (result != null) {
            settingsForm = result;
            settingsForm.setEditing(true);
            settingsForm.start(null);
            openSettingsForm(settingsForm, userCalendarRowSet);
          }

        });
  }


  private void openSettingsForm(FormView settingsForm, final BeeRowSet rowSet) {
    if (settingsForm == null || settingsForm.asWidget().isAttached()) {
      return;
    }

    if (getSettings().getActiveView() != null) {
      Data.setValue(VIEW_USER_CALENDARS, settingsRow, COL_ACTIVE_VIEW,
          getSettings().getActiveView().ordinal());
    }

    settingsForm.updateRow(settingsRow, false);

    String caption = settingsForm.getCaption();

    Widget separateAttendeesWidget = settingsForm.getWidgetBySource(COL_SEPARATE_ATTENDEES);
    if (separateAttendeesWidget != null) {
      separateAttendeesWidget.setVisible(false);
    }

    Global.inputWidget(caption, settingsForm, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (settingsForm.checkOnSave(null)
            && settingsForm.validate(settingsForm, true)) {
          return null;
        } else {
          return InputBoxes.SILENT_ERROR;
        }
      }

      @Override
      public void onClose(CloseCallback closeCallback) {
        if (settingsForm.checkOnClose(null)) {
          settingsForm.onClose(closeCallback);
        }
      }

      @Override
      public void onSuccess() {
        updateSettings(settingsRow, rowSet.getColumns(), true);
      }
    });
  }

  @Override
  public Flow getTodoContainer() {
    return null;
  }


  @Override
  protected void updateUcAttendees(BeeRowSet ucAttendees, boolean refresh) {
    List<Long> attIds = new ArrayList<>();
    if (!DataUtils.isEmpty(ucAttendees)) {
      attIds.addAll(ucAttendees.getDistinctLongs(ucAttendees.getColumnIndex(COL_ATTENDEE)));
    }
    getCalendar().setAttendees(attIds, refresh);
  }

  @Override
  public void onAppointment(AppointmentEvent event) {
    for (Long attendeeId : event.getAppointment().getAttendees()) {
      if (!getCalendar().getAttendees().contains(attendeeId)) {
        getCalendar().getAttendees().add(attendeeId);
      }
    }

    super.onAppointment(event);
  }

  @Override
  protected void onUnload() {
  }

  @Override
  protected Set<Action> getHeaderHiddenAction() {
    return EnumSet.of(Action.CLOSE, Action.PRINT);
  }
}