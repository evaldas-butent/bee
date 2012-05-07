package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.AppointmentStyle;
import com.butent.bee.client.calendar.Attendee;
import com.butent.bee.client.calendar.Calendar;
import com.butent.bee.client.calendar.CalendarWidget;
import com.butent.bee.client.calendar.theme.Appearance;
import com.butent.bee.client.calendar.theme.DefaultTheme;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.InputWidgetCallback;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.DateTimeFormat.PredefinedFormat;
import com.butent.bee.client.presenter.FormPresenter;
import com.butent.bee.client.screen.BookmarkEvent;
import com.butent.bee.client.screen.Favorites;
import com.butent.bee.client.screen.Favorites.Group;
import com.butent.bee.client.ui.AbstractFormCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.validation.CellValidateEvent;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridCallback;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataEvent;
import com.butent.bee.shared.data.event.HandlesAllDataEvents;
import com.butent.bee.shared.data.event.MultiDeleteEvent;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.event.RowDeleteEvent;
import com.butent.bee.shared.data.event.RowInsertEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CalendarKeeper {

  private static class CalendarGridHandler extends AbstractGridCallback implements
      BookmarkEvent.Handler {

    @Override
    public boolean afterCreateColumn(String columnId, List<? extends IsColumn> dataColumns,
        AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
        EditableColumn editableColumn) {
      if (BeeUtils.same(columnId, CalendarConstants.COL_NAME) && editableColumn != null) {
        editableColumn.addCellValidationHandler(new CellValidateEvent.Handler() {
          public Boolean validateCell(CellValidateEvent event) {
            if (event.isPostValidation() && !event.sameValue() && !event.isNewRow()) {
              updateCalendarName(event.getRowId(), event.getNewValue());
            }
            return true;
          }
        });
      }
      return true;
    }

    @Override
    public void afterDeleteRow(long rowId) {
      removeUserCalendar(rowId);
    }

    @Override
    public void onBookmark(BookmarkEvent event) {
      if (Favorites.Group.CALENDARS.equals(event.getGroup())) {
        createUserCalendar(event.getRowId());
      }
    }
  }

  private static class ConfigurationHandler extends AbstractFormCallback {

    private Long companyId = null;
    private Long appointmentTypeId = null;

    private Long timeZoneId = null;
    private Long themeId = null;

    private BeeRowSet configuration = null;

    @Override
    public boolean beforeAction(Action action, FormPresenter presenter) {
      if (Action.SAVE.equals(action)) {
        final IsRow row = presenter.getActiveRow();
        if (row == null || configuration == null) {
          return false;
        }

        String co = DataUtils.getString(configuration, row, CalendarConstants.COL_COMPANY);
        if (BeeUtils.isEmpty(co)) {
          presenter.getNotificationListener().notifySevere("Company is required");
          presenter.getView().getContent().focus(CalendarConstants.COL_COMPANY);
          return false;
        }

        String at = DataUtils.getString(configuration, row, CalendarConstants.COL_APPOINTMENT_TYPE);
        if (BeeUtils.isEmpty(at)) {
          presenter.getNotificationListener().notifySevere("Appointment type is required");
          presenter.getView().getContent().focus(CalendarConstants.COL_APPOINTMENT_TYPE);
          return false;
        }

        RowCallback callback = new Queries.RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (result != null) {
              update(result);
              DataUtils.updateRow(row, result, configuration.getNumberOfColumns());
            }
          }
        };

        if (DataUtils.isNewRow(row)) {
          Queries.insert(CalendarConstants.VIEW_CONFIGURATION, configuration.getColumns(), row,
              callback);
        } else {
          int cnt = Queries.update(CalendarConstants.VIEW_CONFIGURATION,
              configuration.getColumns(), configuration.getRow(0), row, callback);
          if (cnt <= 0) {
            presenter.getNotificationListener().notifyInfo("No changes found");
          }
        }
        return false;
      }
      return true;
    }

    @Override
    public BeeRowSet getRowSet() {
      if (configuration == null || configuration.isEmpty()) {
        return null;
      } else {
        return configuration.clone();
      }
    }

    @Override
    public boolean hasFooter(int rowCount) {
      return false;
    }

    @Override
    public void onShow(FormPresenter presenter) {
      presenter.getView().getContent().setEditing(true);
    }

    private void load() {
      ParameterList params = createRequestParameters(CalendarConstants.SVC_GET_CONFIGURATION);

      BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
        public void onResponse(ResponseObject response) {
          if (response.hasResponse(BeeRowSet.class)) {
            configuration = BeeRowSet.restore((String) response.getResponse());
            if (configuration.isEmpty()) {
              configuration.addEmptyRow();
            }
            updateFields(configuration.getRow(0));
          }
        }
      });
    }

    private void update(BeeRow row) {
      if (row == null || configuration == null) {
        return;
      }

      if (!configuration.isEmpty()) {
        configuration.clearRows();
      }
      configuration.addRow(row);

      updateFields(row);
    }

    private void updateFields(BeeRow row) {
      if (row == null || configuration == null) {
        return;
      }

      companyId = DataUtils.getLong(configuration, row, CalendarConstants.COL_COMPANY);
      appointmentTypeId = DataUtils.getLong(configuration, row,
          CalendarConstants.COL_APPOINTMENT_TYPE);

      timeZoneId = DataUtils.getLong(configuration, row, CalendarConstants.COL_TIME_ZONE);
      themeId = DataUtils.getLong(configuration, row, CalendarConstants.COL_THEME);
    }
  }

  private static class RowActionHandler implements RowActionEvent.Handler {
    private int calendarColumnIndex = BeeConst.UNDEF;

    public void onRowAction(RowActionEvent event) {
      Long id = null;
      String viewName = null;

      if (event.hasView(CalendarConstants.VIEW_CALENDARS)) {
        id = event.getRowId();
        viewName = CalendarConstants.VIEW_CALENDARS;
      } else if (event.hasView(CalendarConstants.VIEW_USER_CALENDARS) && event.hasRow()) {
        if (BeeConst.isUndef(calendarColumnIndex)) {
          DataInfo dataInfo = Global.getDataInfo(CalendarConstants.VIEW_USER_CALENDARS, true);
          if (dataInfo != null) {
            calendarColumnIndex = dataInfo.getColumnIndex(CalendarConstants.COL_CALENDAR);
          }
        }

        if (!BeeConst.isUndef(calendarColumnIndex)) {
          id = event.getRow().getLong(calendarColumnIndex);
          viewName = CalendarConstants.VIEW_CALENDARS;
        }
      }

      if (id == null || viewName == null) {
        return;
      }

      if (viewName.equals(CalendarConstants.VIEW_CALENDARS)) {
        openCalendar(id);
      }
    }
  }

  private static class UserCalendarGridHandler extends AbstractGridCallback {
  }

  private static class UserCalendarViewHandler implements HandlesAllDataEvents {

    public void onCellUpdate(CellUpdateEvent event) {
    }

    public void onMultiDelete(MultiDeleteEvent event) {
      if (isEventRelevant(event)) {
        for (RowInfo rowInfo : event.getRows()) {
          removeUserCalendar(getCalendarId(rowInfo.getId()));
        }
      }
    }

    public void onRowDelete(RowDeleteEvent event) {
      if (isEventRelevant(event)) {
        removeUserCalendar(getCalendarId(event.getRowId()));
      }
    }

    public void onRowInsert(RowInsertEvent event) {
    }

    public void onRowUpdate(RowUpdateEvent event) {
    }

    private boolean isEventRelevant(DataEvent event) {
      return BeeUtils.same(event.getViewName(), CalendarConstants.VIEW_USER_CALENDARS);
    }
  }

  private static BeeRowSet userCalendars = null;

  private static FormView settingsForm = null;

  private static final ConfigurationHandler configurationHandler = new ConfigurationHandler();

  public static void loadUserCalendars() {
    ParameterList params = createRequestParameters(CalendarConstants.SVC_GET_USER_CALENDARS);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          userCalendars = BeeRowSet.restore((String) response.getResponse());
        }
      }
    });
  }

  public static void openAppointment(Appointment appointment, HasDateValue date,
      final Calendar calendar) {
    final boolean isNew = appointment == null;
    String caption = isNew ? "Naujas Vizitas" : "Vizitas";
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
  
    row++;
    panel.setWidget(row, 0, new Html("Spalva"));
    final TabBar colors = new TabBar("bee-ColorBar-");
  
    for (AppointmentStyle style : AppointmentStyle.values()) {
      if (AppointmentStyle.DEFAULT.equals(style) || AppointmentStyle.CUSTOM.equals(style)) {
        continue;
      }
      Appearance gs = DefaultTheme.STYLES.get(style);
      if (gs == null) {
        continue;
      }
  
      Html color = new Html();
      StyleUtils.setSize(color, 14, 14);
      color.getElement().getStyle().setBackgroundColor(gs.getBackground());
      color.getElement().getStyle().setPaddingBottom(2, Unit.PX);
  
      colors.addItem(color);
    }
  
    colors.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        Widget widget = colors.getSelectedWidget();
        if (widget != null) {
          widget.getElement().setInnerHTML(BeeConst.STRING_EMPTY);
        }
      }
    });
  
    colors.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> event) {
        Widget widget = colors.getSelectedWidget();
        if (widget != null) {
          widget.getElement().setInnerHTML(BeeUtils.toString(BeeConst.CHECK_MARK));
        }
      }
    });
  
    panel.setWidget(row, 1, colors);
  
    final Appointment ap = isNew ? new Appointment() : appointment;
    
    if (!isNew && !ap.getAttendees().isEmpty()) {
      row++;
      panel.setWidget(row, 0, new Html("Resursai"));
      
      StringBuilder sb = new StringBuilder();
      for (Attendee attendee : ap.getAttendees()) {
        if (!BeeUtils.isEmpty(attendee.getName())) {
          sb.append(' ').append(attendee.getName().trim());
        }
      }
      panel.setWidget(row, 1, new Html(sb.toString().trim()));
    }
  
    if (isNew && date != null) {
      ap.setStart(date.getDateTime());
      DateTime to = DateTime.copyOf(ap.getStart());
      TimeUtils.addHour(to, 1);
      ap.setEnd(to);
    }
    if (isNew) {
      ap.setStyle(AppointmentStyle.BLUE);
    }
  
    summary.setText(ap.getTitle());
    start.setDate(ap.getStart());
    end.setDate(ap.getEnd());
    description.setText(ap.getDescription());
    colors.selectTab(ap.getStyle().ordinal());
  
    BeeButton confirm = new BeeButton("Išsaugoti", new ClickHandler() {
      public void onClick(ClickEvent ev) {
        HasDateValue from = start.getDate();
        HasDateValue to = end.getDate();
        if (from == null || to == null || TimeUtils.isMeq(from, to)) {
          Global.showError("Sorry, no appointment");
          return;
        }
  
        ap.setTitle(summary.getText());
        ap.setStart(from.getDateTime());
        ap.setEnd(to.getDateTime());
        ap.setDescription(description.getText());
        ap.setStyle(AppointmentStyle.values()[colors.getSelectedTab()]);
        
        if (calendar != null) {
          if (isNew) {
            calendar.addAppointment(ap);
          } else {
            calendar.refresh();
          }
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

  public static void refreshConfiguration() {
    configurationHandler.load();
  }

  public static void register() {
    Global.registerCaptions(CalendarConstants.AppointmentStatus.class);
    Global.registerCaptions(CalendarConstants.ReminderMethod.class);
    Global.registerCaptions(CalendarConstants.ResponseStatus.class);
    Global.registerCaptions(CalendarConstants.Transparency.class);
    Global.registerCaptions("Calendar_Visibility", CalendarConstants.Visibility.class);

    Global.registerCaptions(CalendarConstants.TimeBlockClick.class);

    FormFactory.registerFormCallback(CalendarConstants.FORM_CONFIGURATION, configurationHandler);

    CalendarGridHandler calendarGridHandler = new CalendarGridHandler();

    GridFactory.registerGridCallback(CalendarConstants.GRID_CALENDARS, calendarGridHandler);
    GridFactory.registerGridCallback(CalendarConstants.GRID_USER_CALENDARS,
        new UserCalendarGridHandler());

    BeeKeeper.getBus().registerRowActionHandler(new RowActionHandler());
    BeeKeeper.getBus().registerBookmarkHandler(calendarGridHandler);

    BeeKeeper.getBus().registerDataHandler(new UserCalendarViewHandler());

    configurationHandler.load();
    loadUserCalendars();

    createCommands();
  }

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CalendarConstants.CALENDAR_MODULE);
    params.addQueryItem(CalendarConstants.CALENDAR_METHOD, service);
    return params;
  }

  static void editSettings(long calendarId, CalendarWidget calendarWidget) {
    if (settingsForm == null) {
      createSettingsForm(calendarId, calendarWidget);
    } else {
      openSettingsForm(calendarId, calendarWidget);
    }
  }

  static long getCompany() {
    return BeeUtils.unbox(configurationHandler.companyId);
  }

  static long getDefaultAppointmentType() {
    return BeeUtils.unbox(configurationHandler.appointmentTypeId);
  }

  static long getDefaultTheme() {
    return BeeUtils.unbox(configurationHandler.themeId);
  }

  static long getDefaultTimeZone() {
    return BeeUtils.unbox(configurationHandler.timeZoneId);
  }

  static CalendarSettings getSettings(long calendarId) {
    return new CalendarSettings(getCalendarRow(calendarId), userCalendars.getColumns());
  }

  private static void createCommands() {
    BeeKeeper.getScreen().addCommandItem(new Html("Mano kalendoriai",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            GridFactory.openGrid(CalendarConstants.GRID_USER_CALENDARS);
          }
        }));

    BeeKeeper.getScreen().addCommandItem(new Html("Naujas vizitas",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            openAppointment(null, new DateTime(), null);
          }
        }));
  }

  private static void createSettingsForm(final long calendarId,
      final CalendarWidget calendarWidget) {
    FormFactory.createFormView(CalendarConstants.FORM_CALENDAR_SETTINGS, null,
        userCalendars.getColumns(),
        new FormFactory.FormViewCallback() {
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              settingsForm = result;
              settingsForm.setEditing(true);
              settingsForm.start(null);

              openSettingsForm(calendarId, calendarWidget);
            }
          }
        }, false);
  }

  private static void createUserCalendar(long id) {
    List<BeeColumn> columns = Lists.newArrayList(
        new BeeColumn(ValueType.LONG, CalendarConstants.COL_USER),
        new BeeColumn(ValueType.LONG, CalendarConstants.COL_CALENDAR));
    List<String> values = Lists.newArrayList(
        BeeUtils.toString(BeeKeeper.getUser().getUserId()),
        BeeUtils.toString(id));

    Queries.insert(CalendarConstants.VIEW_USER_CALENDARS, columns, values,
        new Queries.RowCallback() {
          @Override
          public void onSuccess(BeeRow result) {
            if (result != null) {
              if (userCalendars == null) {
                loadUserCalendars();
              } else {
                userCalendars.addRow(result);
              }
            }
          }
        });
  }

  private static long getCalendarId(long userCalendarId) {
    if (userCalendars == null) {
      return BeeConst.UNDEF;
    }

    BeeRow row = userCalendars.getRowById(userCalendarId);
    if (row == null) {
      return BeeConst.UNDEF;
    }
    return DataUtils.getLong(userCalendars, row, CalendarConstants.COL_CALENDAR);
  }

  private static String getCalendarName(IsRow row) {
    return DataUtils.getString(userCalendars, row, CalendarConstants.COL_CALENDAR_NAME);
  }

  private static BeeRow getCalendarRow(long calendarId) {
    if (userCalendars == null) {
      return null;
    }
    for (BeeRow row : userCalendars.getRows()) {
      if (DataUtils.getLong(userCalendars, row, CalendarConstants.COL_CALENDAR) == calendarId) {
        return row;
      }
    }
    return null;
  }

  private static void openCalendar(long id) {
    BeeKeeper.getScreen().updateActivePanel(new CalendarPanel(id, getSettings(id)));
  }

  private static void openSettingsForm(long calendarId, final CalendarWidget calendarWidget) {
    final BeeRow oldRow = getCalendarRow(calendarId);
    final BeeRow newRow = DataUtils.cloneRow(oldRow, userCalendars.getNumberOfColumns());
    settingsForm.updateRow(newRow, false);

    String caption = BeeUtils.concat(1, getCalendarName(oldRow), settingsForm.getCaption());

    Global.inputWidget(caption, settingsForm.asWidget(), new InputWidgetCallback() {
      public void onSuccess() {
        String oldValue;
        String newValue;

        List<BeeColumn> columns = Lists.newArrayList();
        List<String> oldValues = Lists.newArrayList();
        List<String> newValues = Lists.newArrayList();

        for (int i = 0; i < userCalendars.getNumberOfColumns(); i++) {
          oldValue = oldRow.getString(i);
          newValue = newRow.getString(i);

          if (!BeeUtils.equalsTrimRight(oldValue, newValue)) {
            columns.add(userCalendars.getColumn(i));
            oldValues.add(oldValue);
            newValues.add(newValue);

            oldRow.setValue(i, newValue);
          }
        }

        if (!columns.isEmpty()) {
          calendarWidget.getSettings().update(newRow, userCalendars.getColumns());
          calendarWidget.refresh();

          Queries.update(CalendarConstants.VIEW_USER_CALENDARS, oldRow.getId(),
              oldRow.getVersion(), columns, oldValues, newValues, new Queries.RowCallback() {
                @Override
                public void onSuccess(BeeRow result) {
                  userCalendars.updateRow(result);
                }
              });
        }
      }
    });
  }

  private static void removeUserCalendar(long id) {
    if (!DataUtils.isId(id)) {
      return;
    }
    if (userCalendars != null) {
      int index = userCalendars.getRowIndex(id);
      if (!BeeConst.isUndef(index)) {
        userCalendars.removeRow(index);
      }
    }

    BeeKeeper.getScreen().getFavorites().removeItem(Group.CALENDARS, id);
  }

  private static void updateCalendarName(long id, String value) {
    if (!DataUtils.isId(id) || BeeUtils.isEmpty(value)) {
      return;
    }

    if (userCalendars != null) {
      IsRow row = userCalendars.getRowById(id);
      if (row != null) {
        DataUtils.setValue(userCalendars, row, CalendarConstants.COL_CALENDAR_NAME, value);
      }
    }

    BeeKeeper.getScreen().getFavorites().updateItem(Group.CALENDARS, id, value);
  }

  private CalendarKeeper() {
  }
}
