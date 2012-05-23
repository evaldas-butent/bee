package com.butent.bee.client.modules.calendar;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.Attendee;
import com.butent.bee.client.calendar.Calendar;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.DateTimeFormat.PredefinedFormat;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class CalendarKeeper {

  private static class RowActionHandler implements RowActionEvent.Handler {
    public void onRowAction(RowActionEvent event) {
      if (event.hasView(VIEW_CALENDARS)) {
        Long id = event.getRowId();
        if (DataUtils.isId(id)) {
          openCalendar(id);
        }
      }
    }
  }

  private static final CalendarConfigurationHandler configurationHandler =
      new CalendarConfigurationHandler();

  private static FormView settingsForm = null;

  private static DataInfo appointmentViewInfo = null;

  public static void register() {
    Global.registerCaptions(AppointmentStatus.class);
    Global.registerCaptions(ReminderMethod.class);
    Global.registerCaptions(ResponseStatus.class);
    Global.registerCaptions(Transparency.class);
    Global.registerCaptions("Calendar_Visibility", Visibility.class);

    Global.registerCaptions(TimeBlockClick.class);

    FormFactory.registerFormCallback(FORM_CONFIGURATION, configurationHandler);

    GridFactory.registerGridCallback(GRID_APPOINTMENTS, new AppointmentGridHandler());

    BeeKeeper.getBus().registerRowActionHandler(new RowActionHandler());
    SelectorEvent.register(new SelectorHandler());

    createCommands();
  }

  static void createAppointment(boolean modal) {
    createAppointment(TimeUtils.nextHour(0), modal);
  }

  static void createAppointment(final DateTime start, boolean modal) {
    final AppointmentBuilder builder = new AppointmentBuilder(start);

    if (modal) {
      FormFactory.createFormView(FORM_NEW_APPOINTMENT, VIEW_APPOINTMENTS,
          getAppointmentViewInfo().getColumns(), builder, new FormFactory.FormViewCallback() {
            public void onSuccess(FormDescription formDescription, FormView result) {
              if (result != null) {
                result.start(null);
                result.updateRow(AppointmentBuilder.createEmptyRow(start), false);

                Global.inputWidget(result.getCaption(), result.asWidget(),
                    builder.getModalCallback(), RowFactory.OK, RowFactory.CANCEL, true,
                    RowFactory.DIALOG_STYLE);
              }
            }
          }, false);

    } else {
      FormFactory.openForm(FORM_NEW_APPOINTMENT, builder);
    }
  }

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CALENDAR_MODULE);
    params.addQueryItem(CALENDAR_METHOD, service);
    return params;
  }

  static void createVehicle(Long owner) {
    RowFactory.createRow(TransportConstants.VIEW_VEHICLES,
        TransportConstants.FORM_NEW_VEHICLE, "Nauja transporto priemonė");
  }

  static void editSettings(long id, final CalendarWidget calendarWidget) {
    getUserCalendar(id, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (getSettingsForm() == null) {
          createSettingsForm(result, calendarWidget);
        } else {
          openSettingsForm(result, calendarWidget);
        }
      }
    });
  }

  static DataInfo getAppointmentViewInfo() {
    if (appointmentViewInfo == null) {
      appointmentViewInfo = Data.getDataInfo(VIEW_APPOINTMENTS);
    }
    return appointmentViewInfo;
  }

  static long getCompany() {
    return BeeUtils.unbox(configurationHandler.getCompany());
  }

  static long getDefaultAppointmentType() {
    return BeeUtils.unbox(configurationHandler.getAppointmentType());
  }

  static long getDefaultTimeZone() {
    return BeeUtils.unbox(configurationHandler.getTimeZone());
  }

  static void openAppointment(final Appointment appointment, final Calendar calendar) {
    String caption = "Vizitas";
    final DialogBox dialogBox = new DialogBox(caption);

    FlexTable panel = new FlexTable();
    panel.setCellSpacing(4);

    int row = 0;
    panel.setWidget(row, 0, new Html("Pavadinimas:"));

    final InputText summary = new InputText();
    StyleUtils.setWidth(summary, 300);
    panel.setWidget(row, 1, summary);

    DateTimeFormat dtFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

    row++;
    panel.setWidget(row, 0, new Html("Pradžia:"));
    final InputDate start = new InputDate(ValueType.DATETIME, dtFormat);
    panel.setWidget(row, 1, start);

    row++;
    panel.setWidget(row, 0, new Html("Pabaiga:"));
    final InputDate end = new InputDate(ValueType.DATETIME, dtFormat);
    panel.setWidget(row, 1, end);

    row++;
    panel.setWidget(row, 0, new Html("Aprašymas:"));
    final InputArea description = new InputArea();
    description.setVisibleLines(3);
    StyleUtils.setWidth(description, 300);
    panel.setWidget(row, 1, description);

    if (!appointment.getAttendees().isEmpty()) {
      row++;
      panel.setWidget(row, 0, new Html("Resursai"));

      StringBuilder sb = new StringBuilder();
      for (Attendee attendee : appointment.getAttendees()) {
        if (!BeeUtils.isEmpty(attendee.getName())) {
          sb.append(' ').append(attendee.getName().trim());
        }
      }
      panel.setWidget(row, 1, new Html(sb.toString().trim()));
    }

    summary.setText(appointment.getSummary());
    start.setDate(appointment.getStart());
    end.setDate(appointment.getEnd());
    description.setText(appointment.getDescription());

    BeeButton confirm = new BeeButton("Išsaugoti", new ClickHandler() {
      public void onClick(ClickEvent ev) {
        HasDateValue from = start.getDate();
        HasDateValue to = end.getDate();
        if (from == null || to == null || TimeUtils.isMeq(from, to)) {
          Global.showError("Sorry, no appointment");
          return;
        }

        appointment.setStart(from.getDateTime());
        appointment.setEnd(to.getDateTime());
        if (calendar != null) {
          calendar.refresh();
        }

        dialogBox.hide();
      }
    });

    row++;
    panel.setWidget(row, 0, confirm);

    dialogBox.setWidget(panel);

    dialogBox.setAnimationEnabled(true);
    dialogBox.center();

    summary.setFocus(true);
  }

  private static void createCommands() {
    BeeKeeper.getScreen().addCommandItem(new Html("Naujas klientas",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            RowFactory.createRow(CommonsConstants.VIEW_COMPANIES,
                CommonsConstants.FORM_NEW_COMPANY, "Naujas klientas");
          }
        }));

    BeeKeeper.getScreen().addCommandItem(new Html("Naujas automobilis",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            createVehicle(null);
          }
        }));

    BeeKeeper.getScreen().addCommandItem(new Html("Naujas vizitas",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            createAppointment(true);
          }
        }));
  }

  private static void createSettingsForm(final BeeRowSet rowSet, final CalendarWidget cw) {
    FormFactory.createFormView(FORM_CALENDAR_SETTINGS, null, rowSet.getColumns(),
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null && getSettingsForm() == null) {
              setSettingsForm(result);
              getSettingsForm().setEditing(true);
              getSettingsForm().start(null);
            }

            openSettingsForm(rowSet, cw);
          }
        }, false);
  }

  private static FormView getSettingsForm() {
    return settingsForm;
  }

  private static void getUserCalendar(long id, final Queries.RowSetCallback callback) {
    ParameterList params = createRequestParameters(SVC_GET_USER_CALENDAR);
    params.addQueryItem(PARAM_CALENDAR_ID, id);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore((String) response.getResponse()));
        }
      }
    });
  }

  private static void openCalendar(final long id) {
    getUserCalendar(id, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        CalendarSettings settings = CalendarSettings.create(result.getRow(0), result.getColumns());
        BeeKeeper.getScreen().updateActivePanel(new CalendarPanel(id, settings));
      }
    });
  }

  private static void openSettingsForm(final BeeRowSet rowSet, final CalendarWidget cw) {
    if (getSettingsForm() == null || getSettingsForm().asWidget().isAttached()) {
      return;
    }

    final BeeRow oldRow = rowSet.getRow(0);
    final BeeRow newRow = DataUtils.cloneRow(oldRow);

    getSettingsForm().updateRow(newRow, false);

    String caption = getSettingsForm().getCaption();

    Global.inputWidget(caption, getSettingsForm().asWidget(), new InputWidgetCallback() {
      public void onSuccess() {
        int updCount = Queries.update(VIEW_USER_CALENDARS, rowSet.getColumns(), oldRow, newRow,
            null);

        if (updCount > 0) {
          cw.getSettings().loadFrom(newRow, rowSet.getColumns());
          cw.refresh();
        }
      }
    });
  }

  private static void setSettingsForm(FormView settingsForm) {
    CalendarKeeper.settingsForm = settingsForm;
  }

  private CalendarKeeper() {
  }
}
