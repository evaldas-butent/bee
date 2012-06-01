package com.butent.bee.client.calendar;

import com.google.common.collect.Maps;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;

import com.butent.bee.client.calendar.dayview.DayView;
import com.butent.bee.client.calendar.monthview.MonthView;
import com.butent.bee.client.calendar.resourceview.ResourceView;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.modules.calendar.CalendarSettings;

import java.util.Map;

public class Calendar extends CalendarWidget implements RequiresResize, ProvidesResize {
  
  private final Map<CalendarView.Type, CalendarView> viewCache = Maps.newHashMap();

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

  public Calendar(CalendarSettings settings) {
    this(settings, CalendarView.Type.DAY);
  }

  public Calendar(CalendarSettings settings, CalendarView view) {
    super(settings);
    setView(view);
  }

  public Calendar(CalendarSettings settings, CalendarView.Type viewType) {
    super(settings);
    setType(viewType);
  }

  public CalendarView.Type getType() {
    if (getView() == null) {
      return null;
    } else {
      return getView().getType();
    }
  }
  
  public void onResize() {
    resizeTimer.schedule(500);
  }

  public void setType(CalendarView.Type viewType) {
    setType(viewType, getDays());
  }

  public void setType(CalendarView.Type viewType, int days) {
    Assert.notNull(viewType);
    CalendarView cached = viewCache.get(viewType);
    
    switch (viewType) {
      case DAY:
        DayView dayView = (cached instanceof DayView) ? (DayView) cached : new DayView();
        if (days > 0) {
          dayView.setDisplayedDays(days);
        }
        if (!(cached instanceof DayView)) {
          viewCache.put(viewType, dayView);
        }  
        setView(dayView);
        break;

      case MONTH:
        MonthView monthView = (cached instanceof MonthView) ? (MonthView) cached : new MonthView();
        if (!(cached instanceof MonthView)) {
          viewCache.put(viewType, monthView);
        }  
        setView(monthView);
        break;

      case RESOURCE:
        ResourceView resourceView = (cached instanceof ResourceView) ? (ResourceView) cached : new ResourceView();
        if (days > 0) {
          resourceView.setDisplayedDays(days);
        }
        if (!(cached instanceof ResourceView)) {
          viewCache.put(viewType, resourceView);
        }  
        setView(resourceView);
        break;
        
      default:
        Assert.unsupported(viewType.name() + " view is not yet supported");
        break;
    }
  }
}
