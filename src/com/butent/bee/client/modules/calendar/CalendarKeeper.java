package com.butent.bee.client.modules.calendar;

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
import com.butent.bee.client.data.HasDataProvider;
import com.butent.bee.client.data.Provider;
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
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.DataView;
import com.butent.bee.client.view.edit.SelectorEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.RowActionEvent;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.LongValue;
import com.butent.bee.shared.data.value.ValueType;
import static com.butent.bee.shared.modules.calendar.CalendarConstants.*;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

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
  
  private static class SelectorHandler implements SelectorEvent.Handler {
    @Override
    public void onDataSelector(SelectorEvent event) {
      if (!event.isOpened()) {
        return;
      }
      if (!BeeUtils.same(event.getRelatedViewName(), VIEW_EXTENDED_PROPERTIES)) {
        return;
      }

      DataView dataView = UiHelper.getDataView(event.getSelector());
      if (dataView == null) {
        return;
      }
      IsRow row = dataView.getActiveRow();
      if (row == null) {
        return;
      }
      
      long id = row.getId();
      Filter filter = null;

      if (BeeUtils.same(dataView.getViewName(), VIEW_PROPERTY_GROUPS)) {
        if (DataUtils.isId(id)) {
          filter = ComparisonFilter.isEqual(COL_PROPERTY_GROUP, new LongValue(id));
        } else {
          filter = Filter.isEmpty(COL_PROPERTY_GROUP);
        }
      
      } else if (BeeUtils.same(dataView.getViewName(), VIEW_ATTENDEE_PROPS)) {
        if (dataView.getViewPresenter() instanceof HasDataProvider) {
          Provider provider = ((HasDataProvider) dataView.getViewPresenter()).getDataProvider();

          if (provider != null) {
            int index = provider.getColumnIndex(COL_PROPERTY);
            Long exclude = DataUtils.isId(id) ? row.getLong(index) : null;
            Set<Long> used = DataUtils.getDistinct(dataView.getRowData(), index, exclude);
            
            if (used.isEmpty()) {
              filter = provider.getImmutableFilter();
            } else {  
              CompoundFilter and = Filter.and();
              and.add(provider.getImmutableFilter());
              
              for (Long value : used) {
                and.add(ComparisonFilter.compareId(Operator.NE, value));
              }
              filter = and;
            }
          }
        }
      }

      event.getSelector().setAdditionalFilter(filter);
    }
  }

  private static final CalendarConfigurationHandler configurationHandler =
      new CalendarConfigurationHandler();

  private static FormView settingsForm = null;

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

  static ParameterList createRequestParameters(String service) {
    ParameterList params = BeeKeeper.getRpc().createParameters(CALENDAR_MODULE);
    params.addQueryItem(CALENDAR_METHOD, service);
    return params;
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

  static long getCompany() {
    return BeeUtils.unbox(configurationHandler.getCompany());
  }

  static long getDefaultAppointmentType() {
    return BeeUtils.unbox(configurationHandler.getAppointmentType());
  }

  static long getDefaultTimeZone() {
    return BeeUtils.unbox(configurationHandler.getTimeZone());
  }

  private static void createCommands() {
    BeeKeeper.getScreen().addCommandItem(new Html("Naujas klientas",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            RowFactory.createRow("Companies", "Company", "Naujas klientas", null);
          }
        }));

    BeeKeeper.getScreen().addCommandItem(new Html("Naujas automobilis",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            RowFactory.createRow("Vehicles", "Vehicle", "Naujas automobilis", null);
          }
        }));
    
    BeeKeeper.getScreen().addCommandItem(new Html("Naujas vizitas",
        new Scheduler.ScheduledCommand() {
          public void execute() {
            openAppointment(null, new DateTime(), null);
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
