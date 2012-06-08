package com.butent.bee.client.modules.calendar.layout;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Absolute;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.modules.calendar.CalendarSettings;
import com.butent.bee.shared.time.TimeUtils;

public class AppointmentGrid extends Absolute {
  
  private int overlayIndex = BeeConst.UNDEF;

  public AppointmentGrid() {
    super();
  }

  public void build(int columnCount, CalendarSettings settings) {
    clear();

    int intervalsPerHour = settings.getIntervalsPerHour();
    int intervalSize = settings.getPixelsPerInterval();
    
    StyleUtils.setHeight(this, intervalsPerHour * intervalSize * TimeUtils.HOURS_PER_DAY);

    for (int i = 0; i < TimeUtils.HOURS_PER_DAY; i++) {
      boolean isWork = (i >= settings.getWorkingHourStart() && i < settings.getWorkingHourEnd());

      Simple major = new Simple();
      major.addStyleName(CalendarStyleManager.MAJOR_TIME_INTERVAL);
      major.addStyleName(isWork 
          ? CalendarStyleManager.WORKING_HOURS : CalendarStyleManager.NON_WORKING);

      StyleUtils.setHeight(major, intervalSize - 1);
      add(major);

      for (int x = 0; x < intervalsPerHour - 1; x++) {
        Simple minor = new Simple();
        minor.addStyleName(CalendarStyleManager.MINOR_TIME_INTERVAL);
        minor.addStyleName(isWork 
            ? CalendarStyleManager.WORKING_HOURS : CalendarStyleManager.NON_WORKING);

        StyleUtils.setHeight(minor, intervalSize - 1);
        add(minor);
      }
    }
    
    CalendarLayoutManager.addColumnSeparators(this, columnCount);

    Simple overlay = new Simple();    
    overlay.addStyleName(StyleUtils.NAME_OCCUPY);
    add(overlay);
    
    setOverlayIndex(getWidgetIndex(overlay));
  }

  public Widget getOverlay() {
    return getWidget(getOverlayIndex());
  }

  private int getOverlayIndex() {
    return overlayIndex;
  }

  private void setOverlayIndex(int overlayIndex) {
    this.overlayIndex = overlayIndex;
  }
}
