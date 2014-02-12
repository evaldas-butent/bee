package com.butent.bee.client.modules.calendar;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.data.Queries.IntCallback;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.modules.calendar.event.HasTimeBlockClickHandlers;
import com.butent.bee.client.modules.calendar.event.HasUpdateHandlers;
import com.butent.bee.client.modules.calendar.event.TimeBlockClickEvent;
import com.butent.bee.client.modules.calendar.event.UpdateEvent;
import com.butent.bee.client.modules.calendar.view.DayView;
import com.butent.bee.client.modules.calendar.view.MonthView;
import com.butent.bee.client.modules.calendar.view.ResourceView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CalendarWidget extends FlowPanel implements HasOpenHandlers<Appointment>,
    HasTimeBlockClickHandlers, HasUpdateHandlers, RequiresResize, ProvidesResize {

  private static final BeeLogger logger = LogUtils.getLogger(CalendarWidget.class);

  private final long calendarId;

  private final CalendarSettings settings;

  private final CalendarDataManager appointmentManager;
  private final List<Long> attendees = Lists.newArrayList();

  private final Map<CalendarView.Type, CalendarView> viewCache = Maps.newHashMap();

  private final Timer resizeTimer = new Timer() {
    @Override
    public void run() {
      doLayout();
      doSizing();
    }
  };

  private CalendarView view;

  private JustDate date;
  private int displayedDays = BeeConst.UNDEF;

  private boolean layoutSuspended;
  private boolean layoutPending;
  private boolean scrollPending;

  public CalendarWidget(long calendarId, CalendarSettings settings) {
    super();

    this.calendarId = calendarId;
    this.settings = settings;
    this.appointmentManager = new CalendarDataManager();

    this.date = TimeUtils.today();

    sinkEvents(Event.ONMOUSEDOWN | Event.ONDBLCLICK);
  }

  public void addAppointment(Appointment appointment, boolean refresh) {
    Assert.notNull(appointment);
    appointmentManager.addAppointment(appointment);
    if (refresh) {
      refresh(false);
    }
  }

  @Override
  public HandlerRegistration addOpenHandler(OpenHandler<Appointment> handler) {
    return addHandler(handler, OpenEvent.getType());
  }

  @Override
  public HandlerRegistration addTimeBlockClickHandler(TimeBlockClickEvent.Handler handler) {
    return addHandler(handler, TimeBlockClickEvent.getType());
  }

  @Override
  public HandlerRegistration addUpdateHandler(UpdateEvent.Handler handler) {
    return addHandler(handler, UpdateEvent.getType());
  }

  public List<Appointment> getAppointments() {
    return appointmentManager.getAppointments();
  }

  public List<Long> getAttendees() {
    return attendees;
  }

  public JustDate getDate() {
    return date;
  }

  public int getDisplayedDays() {
    return displayedDays;
  }

  public CalendarSettings getSettings() {
    return settings;
  }

  public CalendarView.Type getType() {
    if (getView() == null) {
      return null;
    } else {
      return getView().getType();
    }
  }

  public CalendarView getView() {
    return view;
  }

  public void loadAppointments(boolean force, final boolean scroll) {
    if (getView() != null) {
      final long startMillis = System.currentTimeMillis();
      final Range<DateTime> range = getView().getVisibleRange();

      appointmentManager.loadAppointments(calendarId, range, force, new IntCallback() {
        @Override
        public void onSuccess(Integer result) {
          logger.debug("load", CalendarUtils.renderRange(range), result,
              TimeUtils.elapsedMillis(startMillis));
          refresh(scroll);
        }
      });
    }
  }

  @Override
  public void onBrowserEvent(Event event) {
    int eventType = event.getTypeInt();

    switch (eventType) {
      case Event.ONDBLCLICK:
        if (onDoubleClick(EventUtils.getEventTargetElement(event), event)) {
          event.stopPropagation();
          return;
        }
        break;

      case Event.ONMOUSEDOWN:
        if (event.getButton() == NativeEvent.BUTTON_LEFT
            && EventUtils.isCurrentTarget(event, getElement())) {
          if (onMouseDown(EventUtils.getEventTargetElement(event), event)) {
            event.stopPropagation();
            event.preventDefault();
            return;
          }
        }
        break;
    }

    super.onBrowserEvent(event);
  }

  public void onClock() {
    if (getView() != null) {
      getView().onClock();
    }
  }

  @Override
  public void onResize() {
    resizeTimer.schedule(100);
  }

  public void refresh(boolean scroll) {
    if (layoutSuspended) {
      layoutPending = true;
      scrollPending = scroll;
      return;
    }

    appointmentManager.sortAppointments();

    doLayout();
    doSizing();

    if (scroll) {
      doScroll();
    }
  }

  public boolean removeAppointment(long id, boolean refresh) {
    boolean removed = appointmentManager.removeAppointment(id);
    if (removed && refresh) {
      refresh(false);
    }
    return removed;
  }

  public void resumeLayout() {
    layoutSuspended = false;
    if (layoutPending) {
      refresh(scrollPending);
    }
  }

  public void setAttendees(Collection<Long> atts, boolean refresh) {
    this.attendees.clear();
    this.attendees.addAll(atts);

    if (refresh) {
      refresh(true);
    }
  }

  public void suspendLayout() {
    layoutSuspended = true;
  }

  public boolean update(CalendarView.Type viewType, JustDate newDate, int days) {
    boolean changed = false;
    
    if (viewType != null && !viewType.equals(getType())) {
      setType(viewType);
      changed = true;
    }
    
    if (newDate != null && !newDate.equals(getDate())) {
      setDate(newDate);
      changed = true;
    }
    
    if (days != getDisplayedDays()) {
      setDisplayedDays(days);
      changed = true;
    }
    
    if (changed) {
      loadAppointments(false, true);
    }
    
    return changed;
  }
  
  private void doLayout() {
    if (getView() != null) {
      long startMillis = System.currentTimeMillis();

      getView().doLayout(calendarId);
      
      logger.debug("layout", getView().getAppointmentWidgets().size(),
          TimeUtils.elapsedMillis(startMillis));
    }
  }

  private void doScroll() {
    if (getView() != null) {
      getView().doScroll();
    }
  }

  private void doSizing() {
    if (getView() != null) {
      getView().doSizing();
    }
  }

  private boolean onDoubleClick(Element element, Event event) {
    if (getView() != null && settings.isDoubleClick()) {
      return getView().onClick(calendarId, element, event);
    } else {
      return false;
    }
  }

  private boolean onMouseDown(Element element, Event event) {
    if (getView() != null && settings.isSingleClick()) {
      return getView().onClick(calendarId, element, event);
    } else {
      return false;
    }
  }

  private void setDate(JustDate date) {
    this.date = date;
  }

  private void setDisplayedDays(int displayedDays) {
    this.displayedDays = displayedDays;
  }

  private void setType(CalendarView.Type viewType) {
    CalendarView cached = viewCache.get(viewType);
    if (cached != null) {
      setView(cached);
      return;
    }
    
    CalendarView cv;

    switch (viewType) {
      case DAY:
        cv = new DayView();
        break;

      case MONTH:
        cv = new MonthView();
        break;

      case RESOURCE:
        cv = new ResourceView();
        break;

      default:
        Assert.untouchable();
        cv = null;
    }

    viewCache.put(viewType, cv);
    setView(cv);
  }

  private void setView(CalendarView view) {
    this.view = view;

    view.attach(this);

    setStyleName(view.getStyleName());
  }
}