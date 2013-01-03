package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.MenuManager;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.dialog.InputBoxes;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.modules.commons.ParametersHandler;
import com.butent.bee.client.screen.Domain;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.WidgetFactory;
import com.butent.bee.client.ui.WidgetSupplier;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.view.form.CloseCallback;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowTransformEvent;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CalendarKeeper {

  private static class RowActionHandler implements RowActionEvent.Handler {
    @Override
    public void onRowAction(final RowActionEvent event) {
      if (event.hasView(VIEW_CALENDARS)) {
        event.consume();
        Long calId = event.getRowId();
        if (DataUtils.isId(calId)) {
          String calName = event.hasRow() ?
              Data.getString(VIEW_CALENDARS, event.getRow(), COL_NAME) : event.getOptions();
          openCalendar(calId, calName, true);
        }

      } else if (event.hasView(VIEW_APPOINTMENTS)) {
        event.consume();
        if (event.getRow() instanceof BeeRow) {
          ensureData(new Command() {
            @Override
            public void execute() {
              openAppointment(new Appointment((BeeRow) event.getRow()), false, null);
            }
          });
        }
      }
    }
  }

  private static class RowTransformHandler implements RowTransformEvent.Handler {
    @Override
    public void onRowTransform(RowTransformEvent event) {
      if (event.hasView(VIEW_CALENDARS)) {
        event.setResult(DataUtils.join(Data.getDataInfo(VIEW_CALENDARS), event.getRow(),
            Lists.newArrayList(COL_NAME, COL_DESCRIPTION, COL_OWNER_FIRST_NAME,
                COL_OWNER_LAST_NAME), BeeConst.STRING_SPACE));

      } else if (event.hasView(VIEW_APPOINTMENTS)) {
        event.setResult(APPOINTMENT_RENDERER.renderString(BeeConst.UNDEF,
            new Appointment(event.getRow())));
      }
    }
  }

  static final CalendarCache CACHE = new CalendarCache();

  private static final List<String> CACHED_VIEWS =
      Lists.newArrayList(VIEW_CONFIGURATION, VIEW_APPOINTMENT_TYPES, VIEW_ATTENDEES,
          VIEW_EXTENDED_PROPERTIES, CommonsConstants.VIEW_REMINDER_TYPES, 
          CommonsConstants.VIEW_THEMES, CommonsConstants.VIEW_THEME_COLORS, VIEW_ATTENDEE_PROPS,
          VIEW_APPOINTMENT_STYLES, VIEW_CAL_APPOINTMENT_TYPES);

  private static final AppointmentRenderer APPOINTMENT_RENDERER = new AppointmentRenderer();

  private static final ReportManager REPORT_MANAGER = new ReportManager();

  private static final SelectorHandler SELECTOR_HANDLER = new SelectorHandler();

  private static final BeeLogger logger = LogUtils.getLogger(CalendarKeeper.class);

  private static final Map<String, Long> activePanels = Maps.newHashMap();
  private static final Map<Long, String> activeControllers = Maps.newHashMap();

  private static FormView settingsForm = null;

  private static boolean dataLoaded = false;

  public static void ensureData(final Command command) {
    if (isDataLoaded()) {
      command.execute();
    } else {
      CACHE.getData(CACHED_VIEWS, new CalendarCache.MultiCallback() {
        @Override
        public void onSuccess(Integer result) {
          setDataLoaded(true);
          command.execute();
        }
      });
    }
  }

  public static String getAttendeeCaption(long calendarId, long attId) {
    if (activeControllers.containsKey(calendarId)) {
      CalendarController controller = getController(calendarId);
      if (controller != null) {
        String caption = controller.getAttendeeCaption(attId);
        if (!BeeUtils.isEmpty(caption)) {
          return caption;
        }
      }
    }
    return getAttendeeName(attId);
  }

  public static Map<Long, String> getAttendeeColors(long calendarId) {
    if (activeControllers.containsKey(calendarId)) {
      CalendarController controller = getController(calendarId);
      if (controller != null) {
        return controller.getAttendeeColors();
      }
    }
    return Maps.newHashMap();
  }

  public static String getPropertyName(long id) {
    return CACHE.getString(VIEW_EXTENDED_PROPERTIES, id, COL_NAME);
  }

  public static String getReminderTypeName(long id) {
    return CACHE.getString(CommonsConstants.VIEW_REMINDER_TYPES, id, COL_NAME);
  }

  public static boolean isDataLoaded() {
    return dataLoaded;
  }

  public static void register() {
    String key = Captions.register(AppointmentStatus.class);
    Captions.registerColumn(VIEW_APPOINTMENTS, COL_STATUS, key);

    Captions.register(ResponseStatus.class);

    key = Captions.register(Transparency.class);
    Captions.registerColumn(VIEW_APPOINTMENTS, COL_TRANSPARENCY, key);
    Captions.registerColumn(VIEW_ATTENDEES, COL_TRANSPARENCY, key);
    Captions.registerColumn(VIEW_ATTENDEE_TYPES, COL_TRANSPARENCY, key);
    Captions.registerColumn(VIEW_CALENDARS, COL_TRANSPARENCY, key);

    Captions.registerColumn(VIEW_ATTENDEES, COL_TYPE_TRANSPARENCY, key);
    Captions.registerColumn(VIEW_CAL_ATTENDEE_TYPES, COL_TYPE_TRANSPARENCY, key);
    
    key = Captions.register("Calendar_Visibility", Visibility.class);
    Captions.registerColumn(VIEW_APPOINTMENTS, COL_VISIBILITY, key);
    Captions.registerColumn(VIEW_CALENDARS, COL_VISIBILITY, key);

    key = Captions.register(TimeBlockClick.class);
    Captions.registerColumn(VIEW_CALENDARS, COL_TIME_BLOCK_CLICK_NUMBER, key);
    Captions.registerColumn(VIEW_USER_CALENDARS, COL_TIME_BLOCK_CLICK_NUMBER, key);

    BeeKeeper.getMenu().registerMenuCallback("calendar_parameters", new MenuManager.MenuCallback() {
      @Override
      public void onSelection(String parameters) {
        GridFactory.openGrid("Parameters", new ParametersHandler(parameters));
      }
    });

    GridFactory.registerGridInterceptor(GRID_APPOINTMENTS, new AppointmentGridHandler());

    BeeKeeper.getBus().registerDataHandler(CACHE, true);
    BeeKeeper.getBus().registerRowActionHandler(new RowActionHandler(), false);
    BeeKeeper.getBus().registerRowTransformHandler(new RowTransformHandler(), false);

    SelectorEvent.register(SELECTOR_HANDLER);

    REPORT_MANAGER.register();

    RowEditor.registerHasDelegate(VIEW_CALENDARS);
    RowEditor.registerHasDelegate(VIEW_APPOINTMENTS);
  }

  public static void setDataLoaded(boolean dataLoaded) {
    CalendarKeeper.dataLoaded = dataLoaded;
  }

  static void createAppointment(final Long calendarId, final DateTime start, final Long attendeeId,
      final boolean glass) {

    Long type = null;
    if (calendarId != null) {
      BeeRowSet rowSet = CACHE.getRowSet(VIEW_CAL_APPOINTMENT_TYPES);
      if (rowSet != null) {
        for (BeeRow row : rowSet.getRows()) {
          if (Data.equals(VIEW_CAL_APPOINTMENT_TYPES, row, COL_CALENDAR, calendarId)) {
            type = Data.getLong(VIEW_CAL_APPOINTMENT_TYPES, row, COL_APPOINTMENT_TYPE);
            break;
          }
        }
      }
    }

    if (type == null) {
      type = getDefaultAppointmentType();
    }
    final BeeRow typeRow = (type == null) ? null : CACHE.getRow(VIEW_APPOINTMENT_TYPES, type);

    String formName = (typeRow == null) ? null : Data.getString(VIEW_APPOINTMENT_TYPES, typeRow,
        COL_APPOINTMENT_CREATOR);
    if (BeeUtils.isEmpty(formName)) {
      formName = DEFAULT_NEW_APPOINTMENT_FORM;
    }

    final AppointmentBuilder builder = new AppointmentBuilder(true);

    FormFactory.createFormView(formName, VIEW_APPOINTMENTS,
        getAppointmentViewColumns(), false, builder, new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.start(null);
              result.updateRow(AppointmentBuilder.createEmptyRow(typeRow, start), false);

              if (DataUtils.isId(attendeeId)) {
                builder.setAttendees(Lists.newArrayList(attendeeId));

              } else if (DataUtils.isId(calendarId)) {
                CalendarController controller = getController(calendarId);
                if (controller != null) {
                  List<Long> attendees = controller.getAttendees();
                  if (!attendees.isEmpty()) {
                    builder.setAttendees(Lists.newArrayList(attendees.get(0)));
                    builder.setUcAttendees(attendees);
                  }
                }
              }

              builder.setRequiredFields(formDescription.getOptions());
              builder.initPeriod(start);

              boolean companyAndVehicle = builder.isRequired(COL_COMPANY)
                  && builder.isRequired(COL_VEHICLE);
              SELECTOR_HANDLER.setCompanyHandlerEnabled(companyAndVehicle);
              SELECTOR_HANDLER.setVehicleHandlerEnabled(companyAndVehicle);

              Global.inputWidget(getAppointmentViewInfo().getNewRowCaption(), result,
                  builder.getModalCallback(), glass, RowFactory.DIALOG_STYLE, true);
            }
          }
        });
  }

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CALENDAR_MODULE);
    params.addQueryItem(CALENDAR_METHOD, service);
    return params;
  }

  static void editSettings(long id, final CalendarPanel calendarPanel) {
    getUserCalendar(id, new Queries.RowSetCallback() {
      @Override
      public void onSuccess(BeeRowSet result) {
        if (getSettingsForm() == null) {
          createSettingsForm(result, calendarPanel);
        } else {
          openSettingsForm(result, calendarPanel);
        }
      }
    });
  }

  static BeeRow getAppointmentTypeRow(Appointment appointment) {
    Long type = appointment.getType();
    if (type == null) {
      type = getDefaultAppointmentType();
    }

    BeeRowSet rowSet = CACHE.getRowSet(VIEW_APPOINTMENT_TYPES);

    BeeRow row = null;
    if (rowSet != null && !rowSet.isEmpty()) {
      if (type != null) {
        row = rowSet.getRowById(type);
      }
      if (row == null && rowSet.getNumberOfRows() == 1) {
        row = rowSet.getRow(0);
      }
    }
    return row;
  }

  static List<BeeColumn> getAppointmentViewColumns() {
    return CACHE.getAppointmentViewColumns();
  }

  static DataInfo getAppointmentViewInfo() {
    return CACHE.getAppointmentViewInfo();
  }

  static BeeRowSet getAttendeeProps() {
    return CACHE.getRowSet(VIEW_ATTENDEE_PROPS);
  }

  static BeeRowSet getAttendees() {
    return CACHE.getRowSet(VIEW_ATTENDEES);
  }

  static BeeRowSet getAttendeeTypes() {
    return CACHE.getRowSet(VIEW_ATTENDEE_TYPES);
  }

  static String getCalendarSupplierKey(long calendarId) {
    return "calendar_" + calendarId;
  }

  static void getData(Collection<String> viewNames, final Command command) {
    CACHE.getData(viewNames, new CalendarCache.MultiCallback() {
      @Override
      public void onSuccess(Integer result) {
        command.execute();
      }
    });
  }

  static Long getDefaultAppointmentType() {
    BeeRowSet rowSet = CACHE.getRowSet(VIEW_CONFIGURATION);
    if (rowSet != null) {
      for (BeeRow row : rowSet.getRows()) {
        Long type = Data.getLong(VIEW_CONFIGURATION, row, COL_APPOINTMENT_TYPE);
        if (type != null) {
          return type;
        }
      }
    }
    return null;
  }

  static BeeRowSet getExtendedProperties() {
    return CACHE.getRowSet(VIEW_EXTENDED_PROPERTIES);
  }

  static BeeRowSet getReminderTypes() {
    return CACHE.getRowSet(CommonsConstants.VIEW_REMINDER_TYPES);
  }

  static BeeRowSet getThemeColors() {
    return CACHE.getRowSet(CommonsConstants.VIEW_THEME_COLORS);
  }

  static BeeRowSet getThemes() {
    return CACHE.getRowSet(CommonsConstants.VIEW_THEMES);
  }

  static boolean isAttendeeOpaque(long id) {
    BeeRow row = CACHE.getRow(VIEW_ATTENDEES, id);
    if (row == null) {
      return false;
    }

    Integer value = Data.getInteger(VIEW_ATTENDEES, row, COL_TRANSPARENCY);
    if (value != null) {
      return Transparency.isOpaque(value);
    } else {
      return Transparency.isOpaque(Data.getInteger(VIEW_ATTENDEES, row, COL_TYPE_TRANSPARENCY));
    }
  }

  static void onActivateController(long calendarId) {
    IdentifiableWidget iw = BeeKeeper.getScreen().getActiveWidget();
    if (iw instanceof CalendarPanel && ((CalendarPanel) iw).getCalendarId() == calendarId) {
      return;
    }

    for (Map.Entry<String, Long> entry : activePanels.entrySet()) {
      if (entry.getValue() == calendarId) {
        Widget panel = DomUtils.getWidget(entry.getKey());
        if (panel instanceof CalendarPanel) {
          BeeKeeper.getScreen().activateWidget((CalendarPanel) panel);
          break;
        }
      }
    }
  }

  static void onActivatePanel(CalendarPanel calendarPanel) {
    if (!activePanels.containsKey(calendarPanel.getId())) {
      activePanels.put(calendarPanel.getId(), calendarPanel.getCalendarId());
    }

    BeeKeeper.getScreen().activateDomainEntry(Domain.CALENDAR, calendarPanel.getCalendarId());
  }

  static void onRemoveController(long calendarId) {
    activeControllers.remove(calendarId);

    Set<CalendarPanel> panels = getActivePanels(calendarId);
    for (CalendarPanel panel : panels) {
      BeeKeeper.getScreen().closeWidget(panel);
    }
  }

  static void onRemovePanel(String panelId, long calendarId) {
    activePanels.remove(panelId);

    if (!activePanels.containsValue(calendarId) && activeControllers.containsKey(calendarId)) {
      activeControllers.remove(calendarId);
      BeeKeeper.getScreen().removeDomainEntry(Domain.CALENDAR, calendarId);
    }
  }

  static void openAppointment(final Appointment appointment, final boolean glass,
      final Long calendarId) {
    Assert.notNull(appointment);

    BeeRow typeRow = getAppointmentTypeRow(appointment);
    String formName = (typeRow == null) ? null : Data.getString(VIEW_APPOINTMENT_TYPES, typeRow,
        COL_APPOINTMENT_EDITOR);
    if (BeeUtils.isEmpty(formName)) {
      formName = DEFAULT_EDIT_APPOINTMENT_FORM;
    }

    final AppointmentBuilder builder = new AppointmentBuilder(false);

    FormFactory.createFormView(formName, VIEW_APPOINTMENTS,
        getAppointmentViewColumns(), false, builder, new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              result.start(null);
              result.updateRow(DataUtils.cloneRow(appointment.getRow()), false);

              builder.setAttendees(appointment.getAttendees());
              builder.setProperties(appointment.getProperties());
              builder.setReminders(appointment.getReminders());

              builder.setColor(appointment.getColor());

              builder.setRequiredFields(formDescription.getOptions());

              boolean companyAndVehicle = builder.isRequired(COL_COMPANY)
                  && builder.isRequired(COL_VEHICLE);
              SELECTOR_HANDLER.setCompanyHandlerEnabled(companyAndVehicle);
              SELECTOR_HANDLER.setVehicleHandlerEnabled(companyAndVehicle);

              if (DataUtils.isId(calendarId)) {
                CalendarController controller = getController(calendarId);
                if (controller != null) {
                  builder.setUcAttendees(controller.getAttendees());
                }
              }

              Global.inputWidget(result.getCaption(), result, builder.getModalCallback(), glass,
                  RowEditor.DIALOG_STYLE, true);
            }
          }
        });
  }

  static void renderAppoinment(long calendarId, AppointmentWidget appointmentWidget,
      boolean multi) {

    BeeRow row = getAppointmentTypeRow(appointmentWidget.getAppointment());
    if (row == null) {
      if (multi) {
        APPOINTMENT_RENDERER.renderMulti(calendarId, appointmentWidget);
      } else {
        APPOINTMENT_RENDERER.renderSimple(calendarId, appointmentWidget);
      }

    } else {
      String viewName = VIEW_APPOINTMENT_TYPES;
      String header = Data.getString(viewName, row, multi ? COL_MULTI_HEADER : COL_SIMPLE_HEADER);
      String body = Data.getString(viewName, row, multi ? COL_MULTI_BODY : COL_SIMPLE_BODY);
      String title = Data.getString(viewName, row, COL_APPOINTMENT_TITLE);

      APPOINTMENT_RENDERER.render(calendarId, appointmentWidget, header, body, title, multi);
    }

    BeeRow styleRow = getAppointmentStyleRow(appointmentWidget.getAppointment(), row);
    if (styleRow != null) {
      String viewName = VIEW_APPOINTMENT_STYLES;
      String panel = Data.getString(viewName, styleRow, multi ? COL_MULTI : COL_SIMPLE);

      String header = Data.getString(viewName, styleRow, COL_HEADER);
      String body = Data.getString(viewName, styleRow, COL_BODY);
      String footer = Data.getString(viewName, styleRow, COL_FOOTER);

      CalendarStyleManager.applyStyle(appointmentWidget, panel, header, body, footer);
    }
  }

  static void renderCompact(long calendarId, AppointmentWidget panel, Widget htmlWidget,
      Widget titleWidget) {

    BeeRow row = getAppointmentTypeRow(panel.getAppointment());

    String compact;
    String title;

    if (row == null) {
      compact = null;
      title = null;
    } else {
      String viewName = VIEW_APPOINTMENT_TYPES;
      compact = Data.getString(viewName, row, COL_APPOINTMENT_COMPACT);
      title = Data.getString(viewName, row, COL_APPOINTMENT_TITLE);
    }

    APPOINTMENT_RENDERER.renderCompact(calendarId, panel.getAppointment(), compact, htmlWidget,
        title, titleWidget);

    BeeRow styleRow = getAppointmentStyleRow(panel.getAppointment(), row);
    if (styleRow != null) {
      String styles = Data.getString(VIEW_APPOINTMENT_STYLES, styleRow, COL_COMPACT);
      CalendarStyleManager.applyStyle(panel, styles);
    }
  }

  static void saveActiveView(final CalendarSettings settings) {
    if (settings != null && settings.getActiveView() != null) {
      ParameterList params = createRequestParameters(SVC_SAVE_ACTIVE_VIEW);
      params.addQueryItem(PARAM_USER_CALENDAR_ID, settings.getId());
      params.addQueryItem(PARAM_ACTIVE_VIEW, settings.getActiveView().ordinal());

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
        }
      });
    }
  }

  static void synchronizeDate(long calendarId, JustDate date, boolean sourceIsController) {
    if (date == null) {
      return;
    }

    if (sourceIsController) {
      Set<CalendarPanel> panels = getActivePanels(calendarId);
      for (CalendarPanel panel : panels) {
        panel.setDate(date, false);
      }
    } else {
      CalendarController controller = getController(calendarId);
      if (controller != null) {
        controller.setDate(date);
      }
    }
  }

  static void updatePanels(long calendarId, BeeRowSet ucAttendees) {
    Set<CalendarPanel> panels = getActivePanels(calendarId);
    for (CalendarPanel panel : panels) {
      panel.updateUcAttendees(ucAttendees, true);
    }
  }

  private static void createSettingsForm(final BeeRowSet rowSet, final CalendarPanel cp) {
    FormFactory.createFormView(FORM_CALENDAR_SETTINGS, null, rowSet.getColumns(), false,
        new FormFactory.FormViewCallback() {
          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null && getSettingsForm() == null) {
              setSettingsForm(result);
              getSettingsForm().setEditing(true);
              getSettingsForm().start(null);
            }

            openSettingsForm(rowSet, cp);
          }
        });
  }

  private static Set<CalendarPanel> getActivePanels(long calendarId) {
    Set<CalendarPanel> panels = Sets.newHashSet();

    for (Map.Entry<String, Long> entry : activePanels.entrySet()) {
      if (entry.getValue() == calendarId) {
        Widget widget = DomUtils.getWidget(entry.getKey());
        if (widget instanceof CalendarPanel) {
          panels.add((CalendarPanel) widget);
        } else {
          logger.warning("Calendar panel", entry.getKey(), entry.getValue(), "not found");
        }
      }
    }
    return panels;
  }

  private static BeeRow getAppointmentStyleRow(Appointment appointment, BeeRow typeRow) {
    Long style = appointment.getStyle();
    if (style == null && typeRow != null) {
      style = Data.getLong(VIEW_APPOINTMENT_TYPES, typeRow, COL_STYLE);
    }

    if (style == null) {
      return null;
    } else {
      return CACHE.getRow(VIEW_APPOINTMENT_STYLES, style);
    }
  }

  private static String getAttendeeName(long id) {
    return CACHE.getString(VIEW_ATTENDEES, id, COL_NAME);
  }

  private static CalendarController getController(long calendarId) {
    String ccId = activeControllers.get(calendarId);
    if (BeeUtils.isEmpty(ccId)) {
      return null;
    }

    Widget widget = DomUtils.getWidget(ccId);
    if (widget instanceof CalendarController) {
      return (CalendarController) widget;
    } else {
      return null;
    }
  }

  private static FormView getSettingsForm() {
    return settingsForm;
  }

  private static void getUserCalendar(long id, final Queries.RowSetCallback callback) {
    ParameterList params = createRequestParameters(SVC_GET_USER_CALENDAR);
    params.addQueryItem(PARAM_CALENDAR_ID, id);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          callback.onSuccess(BeeRowSet.restore((String) response.getResponse()));
        }
      }
    });
  }

  private static void onCreatePanel(long calendarId, long ucId, String caption,
      BeeRowSet ucAttendees) {

    if (!BeeKeeper.getScreen().containsDomainEntry(Domain.CALENDAR, calendarId)) {
      CalendarController calendarController = new CalendarController(calendarId, ucId,
          caption, ucAttendees);
      activeControllers.put(calendarId, calendarController.getId());

      BeeKeeper.getScreen().addDomainEntry(Domain.CALENDAR, calendarController, calendarId,
          caption);
    }
  }

  private static void openCalendar(final long id, final String name, final boolean newPanel) {

    class OpenCommand extends Command {
      private final long calendarId;
      private final String calendarName;
      private final Callback<IdentifiableWidget> callback;

      private OpenCommand(long calendarId, String calendarName,
          Callback<IdentifiableWidget> callback) {
        super();
        this.calendarId = calendarId;
        this.calendarName = calendarName;
        this.callback = callback;
      }

      @Override
      public void execute() {
        getUserCalendar(id, new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            BeeRow row = result.getRow(0);
            BeeRowSet ucAttendees = BeeRowSet.restore(row.getProperty(PROP_USER_CAL_ATTENDEES));

            CalendarSettings settings = CalendarSettings.create(row, result.getColumns());
            CalendarPanel calendarPanel = new CalendarPanel(calendarId, calendarName, settings,
                ucAttendees);

            onCreatePanel(calendarId, row.getId(), calendarName, ucAttendees);

            callback.onSuccess(calendarPanel);
          }
        });
      }
    }

    String supplierKey = getCalendarSupplierKey(id);
    if (!WidgetFactory.hasSupplier(supplierKey)) {
      WidgetSupplier supplier = new WidgetSupplier() {
        @Override
        public void create(final Callback<IdentifiableWidget> callback) {
          OpenCommand command = new OpenCommand(id, name, callback);
          ensureData(command);
        }
      };

      WidgetFactory.registerSupplier(supplierKey, supplier);
    }

    OpenCommand command = new OpenCommand(id, name, new Callback<IdentifiableWidget>() {
      @Override
      public void onSuccess(IdentifiableWidget result) {
        BeeKeeper.getScreen().showWidget(result, newPanel);
      }
    });

    ensureData(command);
  }

  private static void openSettingsForm(final BeeRowSet rowSet, final CalendarPanel cp) {
    if (getSettingsForm() == null || getSettingsForm().asWidget().isAttached()) {
      return;
    }

    final BeeRow oldRow = rowSet.getRow(0);
    final BeeRow newRow = DataUtils.cloneRow(oldRow);

    if (cp.getSettings().getActiveView() != null) {
      Data.setValue(VIEW_USER_CALENDARS, newRow, COL_ACTIVE_VIEW,
          cp.getSettings().getActiveView().ordinal());
    }

    getSettingsForm().updateRow(newRow, false);

    String caption = getSettingsForm().getCaption();

    Global.inputWidget(caption, getSettingsForm(), new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (getSettingsForm().checkOnSave()
            && getSettingsForm().validate(getSettingsForm(), true)) {
          return null;
        } else {
          return InputBoxes.SILENT_ERROR;
        }
      }

      @Override
      public void onClose(CloseCallback closeCallback) {
        if (getSettingsForm().checkOnClose()) {
          getSettingsForm().onClose(closeCallback);
        }
      }

      @Override
      public void onSuccess() {
        int updCount = Queries.update(VIEW_USER_CALENDARS, rowSet.getColumns(), oldRow, newRow,
            null);

        if (updCount > 0) {
          cp.updateSettings(newRow, rowSet.getColumns());
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
