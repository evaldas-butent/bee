package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.Calendar;
import com.butent.bee.client.calendar.CalendarView.Type;
import com.butent.bee.client.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.calendar.event.TimeBlockClickHandler;
import com.butent.bee.client.calendar.monthview.MonthView;
import com.butent.bee.client.calendar.resourceview.ResourceView;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.datepicker.DatePicker;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.i18n.DateTimeFormat;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.modules.calendar.CalendarConstants;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class CalendarPanel extends Complex implements NewAppointmentEvent.Handler {
  
  private static final String STYLE_PANEL = "bee-cal-Panel";

  private static final String STYLE_TODAY = "bee-cal-Panel-today";
  
  private static final String STYLE_NAV_CONTAINER = "bee-cal-Panel-navContainer";
  private static final String STYLE_NAV_ITEM = "bee-cal-Panel-navItem";
  private static final String STYLE_NAV_PREV = "bee-cal-Panel-navPrev";
  private static final String STYLE_NAV_NEXT = "bee-cal-Panel-navNext";
  
  private static final String STYLE_DATE = "bee-cal-Panel-date";
  
  private static final String STYLE_VIEW_PREFIX = "bee-cal-Panel-view-";
  
  private static final String STYLE_REFRESH = "bee-cal-Panel-refresh";
  private static final String STYLE_SETTINGS = "bee-cal-Panel-settings";
  
  private static final String STYLE_CALENDAR = "bee-cal-Panel-calendar";

  private static final DateTimeFormat DATE_FORMAT = 
      DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL);

  private final long calendarId;

  private final Calendar calendar;
  private final Html dateBox;

  private HandlerRegistration newAppointmentRegistration;

  public CalendarPanel(long calendarId, CalendarSettings settings) {
    super();
    addStyleName(STYLE_PANEL);

    this.calendarId = calendarId;

    this.calendar = new Calendar(settings);

    calendar.addOpenHandler(new OpenHandler<Appointment>() {
      public void onOpen(OpenEvent<Appointment> event) {
        CalendarKeeper.openAppointment(event.getTarget(), calendar);
      }
    });

    calendar.addTimeBlockClickHandler(new TimeBlockClickHandler<DateTime>() {
      public void onTimeBlockClick(TimeBlockClickEvent<DateTime> event) {
        CalendarKeeper.createAppointment(event.getTarget(), true);
      }
    });
    
    calendar.suspendLayout();
    calendar.setType(Type.DAY, calendar.getSettings().getDefaultDisplayedDays());

    this.dateBox = new Html(DATE_FORMAT.format(calendar.getDate()));
    dateBox.addStyleName(STYLE_DATE);

    dateBox.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        pickDate();
      }
    });

    Html today = new Html("Šiandien");
    today.addStyleName(STYLE_TODAY);

    today.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        setDate(TimeUtils.today());
      }
    });

    Html prev = new Html("<");
    prev.addStyleName(STYLE_NAV_ITEM);
    prev.addStyleName(STYLE_NAV_PREV);
    
    prev.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        navigate(false);
      }
    });

    Html next = new Html(">");
    next.addStyleName(STYLE_NAV_ITEM);
    next.addStyleName(STYLE_NAV_NEXT);

    next.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        navigate(true);
      }
    });

    BeeImage config = new BeeImage(Global.getImages().settings(), new Scheduler.ScheduledCommand() {
      public void execute() {
        CalendarKeeper.editSettings(CalendarPanel.this.calendarId, CalendarPanel.this.calendar);
      }
    });
    config.addStyleName(STYLE_SETTINGS);

    BeeImage refresh = new BeeImage(Global.getImages().refresh(), new Scheduler.ScheduledCommand() {
      public void execute() {
        refresh();
      }
    });
    refresh.addStyleName(STYLE_REFRESH);

    add(today);

    Horizontal nav = new Horizontal();
    nav.addStyleName(STYLE_NAV_CONTAINER);
    nav.add(prev);
    nav.add(next);
    add(nav);

    add(dateBox);

    add(createViews());

    add(refresh);
    add(config);
    
    Simple container = new Simple();
    container.addStyleName(STYLE_CALENDAR);
    container.setWidget(calendar);
    add(container);

    loadAppointments();

    setNewAppointmentRegistration(NewAppointmentEvent.register(this));
  }

  @Override
  public void onNewAppointment(NewAppointmentEvent event) {
    refresh();
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

  @Override
  protected void onUnload() {
    super.onUnload();

    if (!BeeKeeper.getScreen().isTemporaryDetach() && getNewAppointmentRegistration() != null) {
      getNewAppointmentRegistration().removeHandler();
      setNewAppointmentRegistration(null);
    }
  }

  private Widget createViews() {
    TabBar tabBar = new TabBar(STYLE_VIEW_PREFIX);

    tabBar.addItem("Diena");
    tabBar.addItem(BeeUtils.toString(calendar.getSettings().getDefaultDisplayedDays()) + " dienos");
    tabBar.addItem("Darbo savaitė");
    tabBar.addItem("Savaitė");
    tabBar.addItem("Mėnuo");
    tabBar.addItem("Resursai");

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
            setDate(TimeUtils.startOfWeek(calendar.getDate()));
            calendar.setType(Type.DAY, 5);
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

  private HandlerRegistration getNewAppointmentRegistration() {
    return newAppointmentRegistration;
  }

  private void loadAppointments() {
    ParameterList params =
        CalendarKeeper.createRequestParameters(CalendarConstants.SVC_GET_CALENDAR_APPOINTMENTS);
    params.addQueryItem(CalendarConstants.PARAM_CALENDAR_ID, calendarId);

    BeeKeeper.getRpc().makeGetRequest(params, new ResponseCallback() {
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          setAppointments(BeeRowSet.restore((String) response.getResponse()));
        }
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
    setDate(newDate);
  }
  
  private void pickDate() {
    final Popup popup = new Popup(true, false);
    DatePicker datePicker = new DatePicker(calendar.getDate());

    datePicker.addValueChangeHandler(new ValueChangeHandler<JustDate>() {
      @Override
      public void onValueChange(ValueChangeEvent<JustDate> event) {
        popup.hide();
        setDate(event.getValue());
      }
    });
    
    popup.setWidget(datePicker);
    popup.showRelativeTo(dateBox);
  }

  private void refresh() {
    loadAppointments();
  }

  private void setAppointments(BeeRowSet rowSet) {
    if (rowSet == null || rowSet.isEmpty()) {
      return;
    }

    List<Appointment> lst = Lists.newArrayList();
    for (IsRow row : rowSet.getRows()) {
      lst.add(new Appointment(row));
    }

    calendar.setAppointments(lst);
  }

  private void setDate(JustDate date) {
    if (date != null && !date.equals(calendar.getDate())) {
      calendar.setDate(date);
      dateBox.setHTML(DATE_FORMAT.format(date));
    }
  }
  
  private void setNewAppointmentRegistration(HandlerRegistration newAppointmentRegistration) {
    this.newAppointmentRegistration = newAppointmentRegistration;
  }
}
