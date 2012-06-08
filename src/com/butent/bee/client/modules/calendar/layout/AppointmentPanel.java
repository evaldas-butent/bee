package com.butent.bee.client.modules.calendar.layout;

import com.google.gwt.user.client.ui.Composite;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.shared.modules.calendar.CalendarSettings;

public class AppointmentPanel extends Composite {

  public AppointmentPanel() {
    Simple scrollArea = new Simple();
    scrollArea.addStyleName(CalendarStyleManager.SCROLL_AREA);

    Timeline timeline = new Timeline();
    timeline.addStyleName(CalendarStyleManager.TIME_STRIP);

    AppointmentGrid grid = new AppointmentGrid();
    grid.addStyleName(CalendarStyleManager.APPOINTMENT_GRID);

    Flow layout = new Flow();
    layout.addStyleName(CalendarStyleManager.APPOINTMENT_PANEL);

    layout.add(timeline);
    layout.add(grid);

    scrollArea.setWidget(layout);
    initWidget(scrollArea);
  }

  public void build(int columnCount, CalendarSettings settings) {
    getTimeline().build(settings);
    getGrid().build(columnCount, settings);
  }
  
  public AppointmentGrid getGrid() {
    return (AppointmentGrid) getLayoutPanel().getWidget(1);
  }

  public Simple getScrollArea() {
    return (Simple) getWidget();
  }

  public Timeline getTimeline() {
    return (Timeline) getLayoutPanel().getWidget(0);
  }
 
  private Flow getLayoutPanel() {
    return (Flow) getScrollArea().getWidget(); 
  }
}
