package com.butent.bee.client.modules.calendar.layout;

import com.google.gwt.dom.client.Style.Unit;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.modules.calendar.CalendarUtils;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

public class AppointmentGrid extends Absolute {
  
  private int nowIndex = BeeConst.UNDEF; 

  public AppointmentGrid() {
    super();
  }

  public void build(int columnCount, CalendarSettings settings,
      int todayStartColumn, int todayEndColumn) {
    clear();

    int intervalsPerHour = settings.getIntervalsPerHour();
    int intervalSize = settings.getPixelsPerInterval();
    
    StyleUtils.setHeight(this, intervalsPerHour * intervalSize * TimeUtils.HOURS_PER_DAY);

    for (int i = 0; i < TimeUtils.HOURS_PER_DAY; i++) {
      boolean isWork = (i >= settings.getWorkingHourStart() && i < settings.getWorkingHourEnd());

      Html major = new Html();
      major.addStyleName(CalendarStyleManager.MAJOR_TIME_INTERVAL);
      major.addStyleName(isWork 
          ? CalendarStyleManager.WORKING_HOURS : CalendarStyleManager.NON_WORKING);

      StyleUtils.setHeight(major, intervalSize - 1);
      add(major);

      for (int x = 0; x < intervalsPerHour - 1; x++) {
        Html minor = new Html();
        minor.addStyleName(CalendarStyleManager.MINOR_TIME_INTERVAL);
        minor.addStyleName(isWork 
            ? CalendarStyleManager.WORKING_HOURS : CalendarStyleManager.NON_WORKING);

        StyleUtils.setHeight(minor, intervalSize - 1);
        add(minor);
      }
    }
    
    CalendarLayoutManager.addColumnSeparators(this, columnCount);
    
    if (BeeUtils.betweenExclusive(todayStartColumn, 0, columnCount)) {
      Html now = new Html();
      now.addStyleName(CalendarStyleManager.NOW_MARKER);
      
      int width = 100 / columnCount;
      StyleUtils.setLeft(now, todayStartColumn * width, Unit.PCT);
      
      int endColumn = BeeUtils.clamp(todayEndColumn, todayStartColumn, columnCount - 1);
      StyleUtils.setWidth(now, (endColumn - todayStartColumn + 1) * width, Unit.PCT);
      
      add(now);
      setNowIndex(getWidgetCount() - 1);
      
      onClock(settings);
    } else {
      setNowIndex(BeeConst.UNDEF);
    }
  }

  public void onClock(CalendarSettings settings) {
    if (getNowIndex() >= 0) {
      int y = CalendarUtils.getNowY(settings); 
      StyleUtils.setTop(getWidget(getNowIndex()), y);
    }
  }

  private int getNowIndex() {
    return nowIndex;
  }

  private void setNowIndex(int nowIndex) {
    this.nowIndex = nowIndex;
  }
}
