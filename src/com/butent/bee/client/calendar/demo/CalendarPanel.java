package com.butent.bee.client.calendar.demo;

import com.google.common.collect.Lists;
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
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.Appointment;
import com.butent.bee.client.calendar.AppointmentStyle;
import com.butent.bee.client.calendar.Calendar;
import com.butent.bee.client.calendar.CalendarSettings;
import com.butent.bee.client.calendar.CalendarSettings.Click;
import com.butent.bee.client.calendar.CalendarViews;
import com.butent.bee.client.calendar.DateUtils;
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
import com.butent.bee.client.calendar.theme.Appearance;
import com.butent.bee.client.calendar.theme.DefaultTheme;
import com.butent.bee.client.composite.InputDate;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.datepicker.DatePicker;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Vertical;
import com.butent.bee.client.widget.BeeButton;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.TimeUtils;

import java.util.Date;
import java.util.List;

public class CalendarPanel extends Complex {

  private final Calendar calendar = new Calendar();
  private final DatePicker datePicker = new DatePicker();

  public CalendarPanel(int days, boolean multi) {
    configureCalendar();

    calendar.suspendLayout();
    calendar.addAppointments(buildAppointments(days, multi));    
    calendar.setView(CalendarViews.DAY, 4);
    
    datePicker.setDate(calendar.getDate());
    datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
      public void onValueChange(ValueChangeEvent<Date> event) {
        calendar.setDate(event.getValue());
      }
    });

    BeeButton todayButton = new BeeButton("Today");
    todayButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        calendar.setDate(new Date());
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
        Date date = DateUtils.newDate(calendar.getDate());
        DateUtils.resetTime(date);
        date.setTime(date.getTime() + 12 * 60 * 60 * 1000);
        openDialog(null, date);
      }
    });
    
    addLeftTop(createButton, 30, 40);
    addLeftTop(datePicker, 10, 100);

    addLeftTop(panel, 220, 10);

    addRightTop(new BeeImage(Global.getImages().settings()), 10, 10);
    addRightTop(createViews(), 50, 10);

    add(calendar, new Edges(40, 10, 10, 220));
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

  private List<Appointment> buildAppointments(int days, boolean multi) {
    List<Appointment> lst = Lists.newArrayList();

    JustDate date = new JustDate();
    AppointmentStyle[] styles = AppointmentStyle.values();

    for (int day = 0; day < days; day++) {
      int appointmentsPerDay = Random.nextInt(6) + 1;

      for (int i = 0; i < appointmentsPerDay; i++) {
        int hour = BeeUtils.randomInt(6, 18);
        int min = Random.nextInt(2) * 30;
        int dur = Random.nextInt(8) * 30 + 30;

        DateTime start = new DateTime(date.getYear(), date.getMonth(), date.getDom(), hour, min, 0);
        DateTime end = new DateTime(start.getTime() + dur * TimeUtils.MILLIS_PER_MINUTE);

        Appointment appt = new Appointment();
        appt.setStart(start.getJava());
        appt.setEnd(end.getJava());

        appt.setTitle(BeeUtils.randomString(3, 10, 'A', 'Z'));
        appt.setDescription(BeeUtils.randomString(10, 50, ' ', 'z'));
        appt.setStyle(styles[Random.nextInt(styles.length - 1)]);

        lst.add(appt);
      }
      date.setDay(date.getDay() + 1);
    }
    
    if (multi) {
      Appointment multi1 = new Appointment();
      multi1.setStyle(AppointmentStyle.BLUE);
      multi1.setStart(TimeUtils.today(0).getJava());
      multi1.setEnd(TimeUtils.today(14).getJava());
      multi1.setTitle("All day All night");
      lst.add(multi1);

      Appointment multi2 = new Appointment();
      multi2.setStart(TimeUtils.today(0).getJava());
      multi2.setEnd(TimeUtils.today(10).getJava());
      multi2.setTitle("Johnny, la gente esta muy loca");
      multi2.setStyle(AppointmentStyle.RED);
      lst.add(multi2);

      Appointment multi3 = new Appointment();
      multi3.setStart(TimeUtils.today(3).getJava());
      multi3.setEnd(TimeUtils.today(6).getJava());
      multi3.setTitle("Viva la fiesta, viva la noche");
      multi3.setStyle(AppointmentStyle.RED);
      lst.add(multi3);
    }
    
    return lst;
  }

  private void configureCalendar() {
    CalendarSettings settings = new CalendarSettings();
    settings.setTimeBlockClickNumber(Click.Double);
    settings.setEnableDragDropCreation(false);

    calendar.setSettings(settings);

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

    calendar.addTimeBlockClickHandler(new TimeBlockClickHandler<Date>() {
      public void onTimeBlockClick(TimeBlockClickEvent<Date> event) {
        openDialog(null, event.getTarget());
      }
    });

    calendar.addDateRequestHandler(new DateRequestHandler<Date>() {
      public void onDateRequested(DateRequestEvent<Date> event) {
        BeeKeeper.getLog().debug("Requested", event.getTarget(),
            ((Element) event.getClicked()).getInnerText());
      }
    });
  }

  private Widget createViews() {
    TabBar tabBar = new TabBar();
    tabBar.addItem("1 Day");
    tabBar.addItem("4 Days");
    tabBar.addItem("Work Week");
    tabBar.addItem("Week");
    tabBar.addItem("Month");
    tabBar.selectTab(1, false);

    tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
      public void onSelection(SelectionEvent<Integer> event) {
        int tabIndex = event.getSelectedItem();
        switch (tabIndex) {
          case 0:
            calendar.setView(CalendarViews.DAY, 1);
            break;
          case 1:
            calendar.setView(CalendarViews.DAY, 4);
            break;
          case 2:
            calendar.setDate(DateUtils.firstOfTheWeek(calendar.getDate()));
            calendar.setView(CalendarViews.DAY, 5);
            datePicker.setDate(calendar.getDate());
            break;
          case 3:
            calendar.setView(CalendarViews.DAY, 7);
            break;
          case 4:
            calendar.setView(CalendarViews.MONTH);
            break;
        }
      }
    });
    return tabBar;
  }
  
  private void navigate(boolean forward) {
    Date oldDate = calendar.getDate();
    Date newDate;

    if (calendar.getView() instanceof MonthView) {
      if (forward) {
        newDate = DateUtils.firstOfNextMonth(oldDate);
      } else {
        newDate = DateUtils.firstOfPrevMonth(oldDate);
      }
      
    } else {
      int days = Math.max(calendar.getDays(), 1);
      int shift = days;
      if (days == 5) {
        shift = 7;
      }
      if (!forward) {
        shift = -shift;
      }
      
      newDate = DateUtils.shiftDate(oldDate, shift);
      if (days == 5) {
        newDate = DateUtils.firstOfTheWeek(newDate);
      }
    }
    
    calendar.setDate(newDate);
    datePicker.setDate(newDate);
  }

  private void openDialog(Appointment appointment, Date date) {
    final boolean isNew = appointment == null;
    String caption = isNew ? "New Appointment" : "Edit Appointment";
    final DialogBox dialogBox = new DialogBox(caption);

    Vertical panel = new Vertical();
    panel.setSpacing(4);

    panel.add(new Html("Summary"));
    final InputText summary = new InputText();
    StyleUtils.setWidth(summary, 300);
    panel.add(summary);

    DateTimeFormat dtFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);

    panel.add(new Html("Start"));
    final InputDate start = new InputDate(ValueType.DATETIME, dtFormat);
    panel.add(start);

    panel.add(new Html("End"));
    final InputDate end = new InputDate(ValueType.DATETIME, dtFormat);
    panel.add(end);

    panel.add(new Html("Description"));
    final InputArea description = new InputArea();
    description.setVisibleLines(5);
    StyleUtils.setWidth(description, 360);
    panel.add(description);

    panel.add(new Html("Color"));
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

    panel.add(colors);

    final Appointment ap = isNew ? new Appointment() : appointment;

    if (isNew && date != null) {
      ap.setStart(date);
      DateTime to = new DateTime(date);
      TimeUtils.addHour(to, 1);
      ap.setEnd(to.getJava());
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
        Date from = start.getJava();
        Date to = end.getJava();
        if (from == null || to == null || BeeUtils.isMeq(from, to)) {
          Global.showError("Sorry, no appointment");
          return;
        }

        ap.setTitle(summary.getText());
        ap.setStart(from);
        ap.setEnd(to);
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
    panel.add(confirm);

    if (!isNew) {
      BeeButton delete = new BeeButton("Delete", new ClickHandler() {
        public void onClick(ClickEvent ev) {
          calendar.removeAppointment(ap);
          dialogBox.hide();
        }
      });
      panel.add(delete);
    }

    dialogBox.setWidget(panel);
    
    dialogBox.setAnimationEnabled(true);
    dialogBox.center();

    summary.setFocus(true);
  }
}
