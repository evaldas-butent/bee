package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import com.butent.bee.client.calendar.CalendarFormat;
import com.butent.bee.client.calendar.HasSettings;

public class DayViewTimeline extends Composite {

  private static final String TIME_LABEL_STYLE = "hour-label";

  private AbsolutePanel timelinePanel = new AbsolutePanel();
  private HasSettings settings = null;

  public DayViewTimeline(HasSettings settings) {
    initWidget(timelinePanel);
    timelinePanel.setStylePrimaryName("time-strip");
    this.settings = settings;
    prepare();
  }

  public void prepare() {
    timelinePanel.clear();
    double labelHeight =
        settings.getSettings().getIntervalsPerHour()
            * settings.getSettings().getPixelsPerInterval();

    int i = 0;
    if (settings.getSettings().isOffsetHourLabels()) {
      i = 1;
      SimplePanel sp = new SimplePanel();
      sp.setHeight((labelHeight / 2) + "px");
      timelinePanel.add(sp);
    }

    while (i < CalendarFormat.HOURS_IN_DAY) {
      String hour = CalendarFormat.INSTANCE.getHourLabels()[i];
      i++;

      SimplePanel hourWrapper = new SimplePanel();
      hourWrapper.setStylePrimaryName(TIME_LABEL_STYLE);
      hourWrapper.setHeight(labelHeight - 1 + "px");

      FlowPanel flowPanel = new FlowPanel();
      flowPanel.setStyleName("hour-layout");

      String amPm = " ";
      if (i < 13) {
        amPm += CalendarFormat.INSTANCE.getAm();
      } else if (i > 13) {
        amPm += CalendarFormat.INSTANCE.getPm();
      } else {
        hour = CalendarFormat.INSTANCE.getNoon();
        amPm = "";
      }

      Label hourLabel = new Label(hour);
      hourLabel.setStylePrimaryName("hour-text");
      flowPanel.add(hourLabel);

      Label ampmLabel = new Label(amPm);
      ampmLabel.setStylePrimaryName("ampm-text");
      flowPanel.add(ampmLabel);

      hourWrapper.add(flowPanel);

      timelinePanel.add(hourWrapper);
    }
  }
}
