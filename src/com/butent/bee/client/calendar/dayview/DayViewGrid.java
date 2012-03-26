package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.HasSettings;

public class DayViewGrid extends Composite {

  class Div extends ComplexPanel {

    public Div() {
      setElement(DOM.createDiv());
    }

    @Override
    public void add(Widget w) {
      super.add(w, getElement());
    }

    @Override
    public boolean remove(Widget w) {
      boolean removed = super.remove(w);
      return removed;
    }
  }

  private static final int HOURS_PER_DAY = 24;

  protected AbsolutePanel grid = new AbsolutePanel();
  protected SimplePanel gridOverlay = new SimplePanel();

  private HasSettings settings = null;

  public DayViewGrid(HasSettings settings) {
    initWidget(grid);
    this.settings = settings;
  }

  public void build(int workingHourStart, int workingHourStop, int days) {
    grid.clear();

    int intervalsPerHour = settings.getSettings().getIntervalsPerHour();
    double intervalSize = settings.getSettings().getPixelsPerInterval();

    this.setHeight((intervalsPerHour * (intervalSize) * 24) + "px");

    double dayWidth = 100f / days;
    double dayLeft = 0;

    for (int i = 0; i < HOURS_PER_DAY; i++) {
      boolean isWorkingHours = (i >= workingHourStart && i <= workingHourStop);

      SimplePanel sp1 = new SimplePanel();
      sp1.setStyleName("major-time-interval");
      sp1.addStyleName(isWorkingHours ? "working-hours" : "non-working");

      sp1.setHeight(intervalSize - 1 + "px");
      grid.add(sp1);

      for (int x = 0; x < intervalsPerHour - 1; x++) {
        SimplePanel sp2 = new SimplePanel();
        sp2.setStyleName("minor-time-interval");
        sp2.addStyleName(isWorkingHours ? "working-hours" : "non-working");

        sp2.setHeight(intervalSize - 1 + "px");
        grid.add(sp2);
      }
    }

    for (int day = 0; day < days; day++) {
      dayLeft = dayWidth * day;

      SimplePanel dayPanel = new SimplePanel();
      dayPanel.setStyleName("day-separator");
      grid.add(dayPanel);
      DOM.setStyleAttribute(dayPanel.getElement(), "left", dayLeft + "%");
    }

    gridOverlay.setHeight("100%");
    gridOverlay.setWidth("100%");
    DOM.setStyleAttribute(gridOverlay.getElement(), "position", "absolute");
    DOM.setStyleAttribute(gridOverlay.getElement(), "left", "0px");
    DOM.setStyleAttribute(gridOverlay.getElement(), "top", "0px");
    grid.add(gridOverlay);
  }
}
