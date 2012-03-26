package com.butent.bee.client.calendar;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.calendar.dayview.DayView;
import com.butent.bee.client.calendar.monthview.MonthView;
import com.butent.bee.shared.Assert;

public class Calendar extends CalendarWidget implements RequiresResize, ProvidesResize {

  private DayView dayView = null;

  private MonthView monthView = null;

  private Timer resizeTimer = new Timer() {
    private int height;

    @Override
    public void run() {
      int newHeight = getOffsetHeight();
      if (newHeight != height) {
        height = newHeight;
        doSizing();
        if (getView() instanceof MonthView) {
          doLayout();
        }
      }
    }
  };

  public Calendar() {
    this(CalendarViews.DAY);
  }

  public Calendar(CalendarView view) {
    super();
    setView(view);
  }

  public Calendar(CalendarViews view) {
    super();
    setView(view);
  }

  public void onResize() {
    resizeTimer.schedule(500);
  }

  public void setView(CalendarViews view) {
    setView(view, getDays());
  }

  public void setView(CalendarViews view, int days) {
    switch (view) {
      case DAY: {
        if (dayView == null) {
          dayView = new DayView();
        }
        dayView.setDisplayedDays(days);
        setView(dayView);
        break;
      }

      case AGENDA: {
        Assert.unsupported("Agenda View is not yet supported");
        break;
      }

      case MONTH: {
        if (monthView == null) {
          monthView = new MonthView();
        }
        setView(monthView);
        break;
      }
    }
  }
}
