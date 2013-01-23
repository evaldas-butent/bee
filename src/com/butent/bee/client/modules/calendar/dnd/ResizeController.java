package com.butent.bee.client.modules.calendar.dnd;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.event.logical.MoveEvent;
import com.butent.bee.client.modules.calendar.Appointment;
import com.butent.bee.client.modules.calendar.AppointmentWidget;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.modules.calendar.CalendarView;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class ResizeController implements MoveEvent.Handler {

  private final CalendarView calendarView;
  private final Widget scrollArea;

  private CalendarSettings settings = null;

  private AppointmentWidget appointmentWidget = null;

  private int initialHeight;
  private int currentHeight;
  private int maxHeight;

  private int marginBottom;
  
  private boolean scrollEnabled = false;
  private int initialScrollTop;

  public ResizeController(CalendarView calendarView, Widget scrollArea) {
    super();
    this.calendarView = calendarView;
    this.scrollArea = scrollArea;
  }

  @Override
  public void onMove(MoveEvent event) {
    if (getSettings() == null) {
      return;
    }

    Mover mover = event.getMover();
    if (mover == null) {
      return;
    }

    int snapSize = getSettings().getPixelsPerInterval();

    if (getAppointmentWidget() == null) {
      AppointmentWidget widget = CalendarUtils.getAppointmentWidget(mover);
      if (widget == null) {
        return;
      }

      setAppointmentWidget(widget);

      int height = StyleUtils.getHeight(widget);
      setInitialHeight(height);
      setCurrentHeight(height);

      setMaxHeight(getSettings().getHourHeight() * TimeUtils.HOURS_PER_DAY
          - StyleUtils.getTop(widget));

      setMarginBottom((snapSize - height % snapSize) % snapSize);
      
      setScrollEnabled(scrollArea != null 
          && scrollArea.getElement().getScrollHeight() > scrollArea.getOffsetHeight());
      setInitialScrollTop(isScrollEnabled() ? scrollArea.getElement().getScrollTop() : 0);
    }

    int y = mover.getCurrentY();
    int scrollTop = isScrollEnabled() ? scrollArea.getElement().getScrollTop() : 0;
    
    int desired = getInitialHeight() + y - mover.getStartY() + scrollTop - getInitialScrollTop();
    int clamped = BeeUtils.clamp(desired, snapSize, getMaxHeight());

    int newHeight = BeeUtils.snap(clamped, snapSize) - getMarginBottom();

    if (newHeight > 0 && getCurrentHeight() != newHeight) {
      StyleUtils.setHeight(getAppointmentWidget(), newHeight);
      setCurrentHeight(newHeight);
    }

    if (event.isFinished()) {
      if (getCurrentHeight() != getInitialHeight()) {
        int minutes = CalendarUtils.getMinutes(newHeight + getMarginBottom(), getSettings());

        Appointment appointment = getAppointmentWidget().getAppointment();

        DateTime newEnd = new DateTime(appointment.getStart().getTime()
            + minutes * TimeUtils.MILLIS_PER_MINUTE);

        calendarView.updateAppointment(appointment, appointment.getStart(), newEnd,
            appointmentWidget.getColumnIndex(), appointmentWidget.getColumnIndex());

        calendarView.getCalendarWidget().refresh(false);
      }

      setAppointmentWidget(null);
    
    } else if (isScrollEnabled()) {
      int top = scrollArea.getElement().getAbsoluteTop();
      
      int newPos = BeeConst.UNDEF;

      if (scrollTop > 0 && event.getDeltaY() < 0 && y < top + snapSize) {
        newPos = Math.max(BeeUtils.snap(scrollTop, snapSize) - snapSize, 0);
      
      } else {
        int clientHeight = scrollArea.getElement().getClientHeight();
        int scrollHeight = scrollArea.getElement().getScrollHeight();
        
        if (scrollTop + clientHeight < scrollHeight && event.getDeltaY() > 0
            && y > top + clientHeight - snapSize) {
          newPos = Math.min(BeeUtils.snap(scrollTop, snapSize) + snapSize,
              scrollHeight - clientHeight);
        }
      }
      
      if (newPos >= 0 && newPos != scrollTop) {
        scrollArea.getElement().setScrollTop(newPos);
      }
    }
  }

  public void setSettings(CalendarSettings settings) {
    this.settings = settings;
  }

  private AppointmentWidget getAppointmentWidget() {
    return appointmentWidget;
  }

  private int getCurrentHeight() {
    return currentHeight;
  }

  private int getInitialHeight() {
    return initialHeight;
  }

  private int getInitialScrollTop() {
    return initialScrollTop;
  }

  private int getMarginBottom() {
    return marginBottom;
  }

  private int getMaxHeight() {
    return maxHeight;
  }

  private CalendarSettings getSettings() {
    return settings;
  }

  private boolean isScrollEnabled() {
    return scrollEnabled;
  }

  private void setAppointmentWidget(AppointmentWidget appointmentWidget) {
    this.appointmentWidget = appointmentWidget;
  }

  private void setCurrentHeight(int currentHeight) {
    this.currentHeight = currentHeight;
  }

  private void setInitialHeight(int initialHeight) {
    this.initialHeight = initialHeight;
  }

  private void setInitialScrollTop(int initialScrollTop) {
    this.initialScrollTop = initialScrollTop;
  }

  private void setMarginBottom(int marginBottom) {
    this.marginBottom = marginBottom;
  }

  private void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  private void setScrollEnabled(boolean scrollEnabled) {
    this.scrollEnabled = scrollEnabled;
  }
}
