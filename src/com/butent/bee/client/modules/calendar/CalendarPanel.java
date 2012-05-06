package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.AppointmentStyle;
import com.butent.bee.client.calendar.Attendee;
import com.butent.bee.client.calendar.Calendar;
import com.butent.bee.client.calendar.CalendarView.Type;
import com.butent.bee.client.calendar.event.CreateEvent;
import com.butent.bee.client.calendar.event.CreateHandler;
import com.butent.bee.client.calendar.event.DateRequestEvent;
import com.butent.bee.client.calendar.event.DateRequestHandler;
import com.butent.bee.client.calendar.event.DeleteEvent;
import com.butent.bee.client.calendar.event.DeleteHandler;
import com.butent.bee.client.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.calendar.event.TimeBlockClickHandler;
import com.butent.bee.client.calendar.event.UpdateEvent;
import com.butent.bee.client.calendar.event.UpdateHandler;
import com.butent.bee.client.calendar.monthview.MonthView;
import com.butent.bee.client.calendar.resourceview.ResourceView;
import com.butent.bee.client.calendar.theme.Appearance;
import com.butent.bee.client.calendar.theme.DefaultTheme;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.data.Provider;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.datepicker.DatePicker;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.grid.FlexTable;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.i18n.DateTimeFormat.PredefinedFormat;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.ui.GridDescription;
import com.butent.bee.shared.ui.ColumnDescription.ColType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class CalendarPanel extends Complex {
  
  private final long calendarId; 

  private final Calendar calendar;
  private final DatePicker datePicker;
  
  private final Complex gridPanel = new Complex();
  private GridPresenter gridPresenter = null;
  
  private int resourceNameIndex = BeeConst.UNDEF;

  public CalendarPanel(long calendarId, CalendarSettings settings) {
    this.calendarId = calendarId;
    this.calendar = new Calendar(settings);
    configureCalendar();

    calendar.suspendLayout();
    calendar.setType(Type.DAY, calendar.getSettings().getDefaultDisplayedDays());
    
    datePicker = new DatePicker(calendar.getDate());
    datePicker.addValueChangeHandler(new ValueChangeHandler<JustDate>() {
      public void onValueChange(ValueChangeEvent<JustDate> event) {
        calendar.setDate(event.getValue());
      }
    });

    BeeButton todayButton = new BeeButton("Today");
    todayButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        calendar.setDate(TimeUtils.today());
        datePicker.setDate(calendar.getDate());
      }
    });

    BeeButton leftWeekButton = new BeeButton("<");
    leftWeekButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        navigate(false);
      }
    });

    BeeButton rightWeekButton = new BeeButton(">");
    rightWeekButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        navigate(true);
      }
    });

    Horizontal panel = new Horizontal();
    panel.add(todayButton);
    panel.add(leftWeekButton);
    panel.add(rightWeekButton);

    BeeButton createButton = new BeeButton("CREATE");
    createButton.setStyleName("bee-CreateAppointment");
    createButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        DateTime date = calendar.getDate().getDateTime();
        date.setHour(12);
        openDialog(null, date);
      }
    });

    BeeButton refreshButton = new BeeButton("Refresh");
    refreshButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        refresh();
      }
    });
    
    addLeftTop(createButton, 30, 40);
    addLeftTop(datePicker, 10, 100);
    
    addLeftWidthTopBottom(gridPanel, 10, 180, 360, 80);
    addLeftBottom(refreshButton, 30, 40);

    addLeftTop(panel, 220, 10);

    BeeImage config = new BeeImage(Global.getImages().settings(), new Scheduler.ScheduledCommand() {
      public void execute() {
        CalendarKeeper.editSettings(CalendarPanel.this.calendarId, CalendarPanel.this.calendar);
      }
    });
    addRightTop(config, 10, 10);
    
    addRightTop(createViews(), 50, 10);

    add(calendar, new Edges(40, 10, 10, 220));
    
    loadResources();
    loadAppointments(null);
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        calendar.resumeLayout();
        calendar.scrollToHour(calendar.getSettings().getScrollToHour());
      }
    });
  }

  private void configureCalendar() {
    calendar.addDeleteHandler(new DeleteHandler<Appointment>() {
      public void onDelete(DeleteEvent<Appointment> event) {
        BeeKeeper.getLog().debug("Appointment deleted");
      }
    });

    calendar.addUpdateHandler(new UpdateHandler<Appointment>() {
      public void onUpdate(UpdateEvent<Appointment> event) {
        BeeKeeper.getLog().debug("Appointment updated");
      }
    });

    calendar.addOpenHandler(new OpenHandler<Appointment>() {
      public void onOpen(OpenEvent<Appointment> event) {
        openDialog(event.getTarget(), null);
      }
    });

    calendar.addCreateHandler(new CreateHandler<Appointment>() {
      public void onCreate(CreateEvent<Appointment> event) {
        BeeKeeper.getLog().debug("Appointment created");
      }
    });

    calendar.addTimeBlockClickHandler(new TimeBlockClickHandler<HasDateValue>() {
      public void onTimeBlockClick(TimeBlockClickEvent<HasDateValue> event) {
        openDialog(null, event.getTarget());
      }
    });

    calendar.addDateRequestHandler(new DateRequestHandler<HasDateValue>() {
      public void onDateRequested(DateRequestEvent<HasDateValue> event) {
        BeeKeeper.getLog().debug("Requested", event.getTarget(),
            ((Element) event.getClicked()).getInnerText());
      }
    });
  }

  private GridDescription createGridDescription(BeeRowSet rowSet, List<String> columnNames) {
    String viewName = rowSet.getViewName();
    GridDescription gridDescription = new GridDescription(viewName, viewName, null, null);

    gridDescription.setCaption("Resources");
    gridDescription.setReadOnly(true);

    gridDescription.setHasHeaders(false);
    gridDescription.setHasFooters(true);

    gridDescription.setSearchThreshold(DataUtils.getDefaultSearchThreshold());

    gridDescription.addColumn(new ColumnDescription(ColType.SELECTION,
        NameUtils.createUniqueName("select-")));

    for (String colName : columnNames) {
      ColumnDescription columnDescription = new ColumnDescription(ColType.DATA, colName);
      columnDescription.setSource(colName);
      columnDescription.setSortable(true);
      columnDescription.setHasFooter(true);

      gridDescription.addColumn(columnDescription);
    }
    return gridDescription;
  }
  
  private void createGridPresenter(BeeRowSet rowSet, List<String> columnNames) {
    GridPresenter gp = new GridPresenter(rowSet.getViewName(), rowSet.getNumberOfRows(),
        rowSet, Provider.Type.LOCAL, createGridDescription(rowSet, columnNames), null, null,
        EnumSet.of(UiOption.CHILD));
    setGridPresenter(gp);
    gp.setEventSource(getId());
    
    StyleUtils.makeAbsolute(gp.getWidget());
    gp.getWidget().addStyleName(StyleUtils.NAME_OCCUPY);

    gridPanel.add(gp.getWidget());
  }

  private Widget createViews() {
    TabBar tabBar = new TabBar();
    tabBar.addItem("1 Day");
    tabBar.addItem(BeeUtils.toString(calendar.getSettings().getDefaultDisplayedDays()) + " Days");
    tabBar.addItem("Work Week");
    tabBar.addItem("Week");
    tabBar.addItem("Month");
    tabBar.addItem("Resources");
    tabBar.selectTab(1, false);

    tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> event) {
        int tabIndex = event.getSelectedItem();
        switch (tabIndex) {
          case 0:
            calendar.setType(Type.DAY, 1);
            break;
          case 1:
            calendar.setType(Type.DAY, calendar.getSettings().getDefaultDisplayedDays());
            break;
          case 2:
            calendar.setDate(TimeUtils.startOfWeek(calendar.getDate()));
            calendar.setType(Type.DAY, 5);
            datePicker.setDate(calendar.getDate());
            break;
          case 3:
            calendar.setType(Type.DAY, 7);
            break;
          case 4:
            calendar.setType(Type.MONTH);
            break;
          case 5:
            calendar.setType(Type.RESOURCE);
            refresh();
            break;
        }
      }
    });
    return tabBar;
  }

  private GridPresenter getGridPresenter() {
    return gridPresenter;
  }
  
  private int getResourceNameIndex() {
    return resourceNameIndex;
  }

  private void loadAppointmentAttendees(final BeeRowSet appRowSet, final Set<Long> attIds) {
    Queries.getRowSet("AppointmentAttendees", null, new Queries.RowSetCallback() {
      public void onSuccess(BeeRowSet result) {
        setAppointments(appRowSet, result, attIds);
      }
    }); 
  }

  private void loadAppointments(final Set<Long> attIds) {
    Queries.getRowSet("Appointments", null, new Queries.RowSetCallback() {
      public void onSuccess(BeeRowSet result) {
        loadAppointmentAttendees(result, attIds);
      }
    }); 
  }
  
  private void loadResources() {
    Queries.getRowSet("Attendees", null, new Queries.RowSetCallback() {
      public void onSuccess(BeeRowSet result) {
        setResourceNameIndex(DataUtils.getColumnIndex("Name", result.getColumns()));
        createGridPresenter(result, Lists.newArrayList("Name"));
      }
    }); 
  }
  
  private void navigate(boolean forward) {
    JustDate oldDate = calendar.getDate();
    JustDate newDate;

    if (calendar.getView() instanceof MonthView) {
      if (forward) {
        newDate = TimeUtils.startOfNextMonth(oldDate);
      } else {
        newDate = TimeUtils.startOfPreviousMonth(oldDate);
      }
      
    } else {
      int days = (calendar.getView() instanceof ResourceView) ? 1 : Math.max(calendar.getDays(), 1);
      int shift = days;
      if (days == 5) {
        shift = 7;
      }
      if (!forward) {
        shift = -shift;
      }
      
      newDate = TimeUtils.nextDay(oldDate, shift);
      if (days == 5) {
        newDate = TimeUtils.startOfWeek(newDate);
      }
    }
    
    calendar.setDate(newDate);
    datePicker.setDate(newDate);
  }
  
  private void openDialog(Appointment appointment, HasDateValue date) {
    final boolean isNew = appointment == null;
    String caption = isNew ? "New Appointment" : "Edit Appointment";
    final DialogBox dialogBox = new DialogBox(caption);

    FlexTable panel = new FlexTable();
    panel.setCellSpacing(4);
    
    int row = 0;
    panel.setWidget(row, 0, new Html("Summary"));
    
    final InputText summary = new InputText();
    StyleUtils.setWidth(summary, 300);
    panel.setWidget(row, 1, summary);

    DateTimeFormat dtFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
    
    row++;
    panel.setWidget(row, 0, new Html("Start"));
    final InputDate start = new InputDate(ValueType.DATETIME, dtFormat);
    panel.setWidget(row, 1, start);

    row++;
    panel.setWidget(row, 0, new Html("End"));
    final InputDate end = new InputDate(ValueType.DATETIME, dtFormat);
    panel.setWidget(row, 1, end);
    
    row++;
    panel.setWidget(row, 0, new Html("Description"));
    final InputArea description = new InputArea();
    description.setVisibleLines(3);
    StyleUtils.setWidth(description, 300);
    panel.setWidget(row, 1, description);

    row++;
    panel.setWidget(row, 0, new Html("Color"));
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
      panel.setWidget(row, 0, new Html("Attendees"));
      
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

    BeeButton confirm = new BeeButton(isNew ? "Create" : "Update", new ClickHandler() {
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

        if (isNew) {
          calendar.addAppointment(ap);
        } else {
          calendar.refresh();
        }

        dialogBox.hide();
      }
    });
    
    row++;
    panel.setWidget(row, 0, confirm);

    if (!isNew) {
      BeeButton delete = new BeeButton("Delete", new ClickHandler() {
        public void onClick(ClickEvent ev) {
          calendar.removeAppointment(ap);
          dialogBox.hide();
        }
      });
      panel.setWidget(row, 1, delete);
    }

    dialogBox.setWidget(panel);
    
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();

    summary.setFocus(true);
  }

  private void refresh() {
    if (getGridPresenter() == null) {
      return;
    }

    Collection<RowInfo> selectedRows = getGridPresenter().getView().getContent().getSelectedRows();
    if (selectedRows.isEmpty()) {
      return;
    }

    List<Attendee> lst = Lists.newArrayList();
    Set<Long> attIds = Sets.newHashSet();

    for (RowInfo rowInfo : selectedRows) {
      IsRow row = getGridPresenter().getView().getContent().getGrid().getRowById(rowInfo.getId());
      if (row != null) {
        lst.add(new Attendee(row.getId(), row.getString(getResourceNameIndex())));
        attIds.add(row.getId());
      }
    }
    
    if (!lst.isEmpty()) {
      calendar.setAttendees(lst);
      loadAppointments(attIds);
    }
  }

  private void setAppointments(BeeRowSet apprs, BeeRowSet aars, Set<Long> attIds) {
    if (apprs == null || apprs.isEmpty()) {
      return;
    }
    
    List<Appointment> lst = Lists.newArrayList();
    AppointmentStyle[] styles = AppointmentStyle.values();
    
    int startIndex = DataUtils.getColumnIndex("StartDateTime", apprs.getColumns());
    int endIndex = DataUtils.getColumnIndex("EndDateTime", apprs.getColumns());

    int summaryIndex = DataUtils.getColumnIndex("Summary", apprs.getColumns());
    int descrIndex = DataUtils.getColumnIndex("Description", apprs.getColumns());
    
    int appIndex = BeeConst.UNDEF;
    int attIndex = BeeConst.UNDEF;
    int nameIndex = BeeConst.UNDEF;
    
    if (aars != null && !aars.isEmpty()) {
      appIndex = DataUtils.getColumnIndex("Appointment", aars.getColumns());
      attIndex = DataUtils.getColumnIndex("Attendee", aars.getColumns());
      nameIndex = DataUtils.getColumnIndex("AttendeeName", aars.getColumns());
    }
    
    for (IsRow row : apprs.getRows()) {
      Appointment appt = new Appointment();
      appt.setId(row.getId());
      
      appt.setTitle(row.getString(summaryIndex));
      appt.setDescription(row.getString(descrIndex));

      appt.setStart(row.getDateTime(startIndex));
      appt.setEnd(row.getDateTime(endIndex));

      appt.setStyle(styles[Random.nextInt(styles.length - 2)]);
      
      if (nameIndex >= 0) {
        for (IsRow aa : aars.getRows()) {
          if (attIds != null && !attIds.isEmpty()) {
            if (!attIds.contains(aa.getLong(attIndex))) {
              continue;
            }
          }
          if (aa.getLong(appIndex) == row.getId()) {
            appt.getAttendees().add(new Attendee(aa.getLong(attIndex), aa.getString(nameIndex)));
          }
        }
      }
      
      if (attIds != null && !attIds.isEmpty() && appt.getAttendees().isEmpty()) {
        continue;
      }
      lst.add(appt);
    }
    
    calendar.setAppointments(lst);
  }

  private void setGridPresenter(GridPresenter gridPresenter) {
    this.gridPresenter = gridPresenter;
  }

  private void setResourceNameIndex(int resourceNameIndex) {
    this.resourceNameIndex = resourceNameIndex;
  }
}
