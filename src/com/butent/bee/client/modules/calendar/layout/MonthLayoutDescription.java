package com.butent.bee.client.modules.calendar.layout;

import com.butent.bee.client.modules.calendar.layout.WeekLayoutDescription.WidgetPart;
import com.butent.bee.shared.modules.calendar.CalendarItem;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

import java.util.List;

public class MonthLayoutDescription {

  private final JustDate firstDate;

  private final WeekLayoutDescription[] weeks;

  public MonthLayoutDescription(JustDate firstDate, int weekCount, List<CalendarItem> items,
      int maxLayer) {
    this.firstDate = firstDate;
    this.weeks = new WeekLayoutDescription[weekCount];

    placeItems(items, maxLayer);
  }

  public WeekLayoutDescription[] getWeekDescriptions() {
    return weeks;
  }

  private int calculateWeekFor(DateTime dateTime, boolean end) {
    int diff = TimeUtils.dayDiff(firstDate, dateTime);
    if (end && TimeUtils.minutesSinceDayStarted(dateTime) == 0) {
      diff--;
    }

    if (diff > 0) {
      return Math.min(diff / TimeUtils.DAYS_PER_WEEK, weeks.length - 1);
    } else {
      return 0;
    }
  }

  private void distributeOverWeeks(int startWeek, int endWeek, CalendarItem item, int maxLayer) {
    initWeek(startWeek, maxLayer);
    weeks[startWeek].addMultiWeekItem(item, WidgetPart.FIRST_WEEK);

    for (int week = startWeek + 1; week < endWeek; week++) {
      initWeek(week, maxLayer);
      weeks[week].addMultiWeekItem(item, WidgetPart.IN_BETWEEN);
    }

    if (startWeek < endWeek) {
      initWeek(endWeek, maxLayer);
      weeks[endWeek].addMultiWeekItem(item, WidgetPart.LAST_WEEK);
    }
  }

  private void initWeek(int weekIndex, int maxLayer) {
    if (weeks[weekIndex] == null) {
      JustDate date = TimeUtils.nextDay(firstDate, weekIndex * TimeUtils.DAYS_PER_WEEK);
      weeks[weekIndex] = new WeekLayoutDescription(date, maxLayer);
    }
  }

  private void placeItems(List<CalendarItem> items, int maxLayer) {
    for (CalendarItem item : items) {
      int startWeek = calculateWeekFor(item.getStartTime(), false);

      if (item.isMultiDay()) {
        positionMultiDayItem(startWeek, item, maxLayer);
      } else {
        initWeek(startWeek, maxLayer);
        weeks[startWeek].addItem(item);
      }
    }
  }

  private void positionMultiDayItem(int startWeek, CalendarItem item, int maxLayer) {
    int endWeek = calculateWeekFor(item.getEndTime(), true);

    if (startWeek < endWeek) {
      distributeOverWeeks(startWeek, endWeek, item, maxLayer);
    } else {
      initWeek(startWeek, maxLayer);
      weeks[startWeek].addMultiDayItem(item);
    }
  }
}
