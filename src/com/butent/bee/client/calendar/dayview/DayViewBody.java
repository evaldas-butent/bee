package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.calendar.HasSettings;

public class DayViewBody extends Composite {

  private FlexTable layout = new FlexTable();
  private ScrollPanel scrollPanel = new ScrollPanel();
  private DayViewTimeline timeline = null;
  private DayViewGrid grid = null;
  private HasSettings settings = null;

  public DayViewBody(HasSettings settings) {
    initWidget(scrollPanel);
    this.settings = settings;
    this.timeline = new DayViewTimeline(settings);
    this.grid = new DayViewGrid(settings);
    scrollPanel.setStylePrimaryName("scroll-area");
    DOM.setStyleAttribute(scrollPanel.getElement(), "overflowX", "hidden");
    DOM.setStyleAttribute(scrollPanel.getElement(), "overflowY", "scroll");

    layout.setCellPadding(0);
    layout.setBorderWidth(0);
    layout.setCellSpacing(0);
    layout.getColumnFormatter().setWidth(1, "99%");

    VerticalAlignmentConstant valign = HasVerticalAlignment.ALIGN_TOP;
    layout.getCellFormatter().setVerticalAlignment(0, 0, valign);
    layout.getCellFormatter().setVerticalAlignment(0, 1, valign);

    grid.setStyleName("bee-appointment-panel");

    layout.getCellFormatter().setWidth(0, 0, "50px");
    DOM.setStyleAttribute(layout.getElement(), "tableLayout", "fixed");

    layout.setWidget(0, 0, timeline);
    layout.setWidget(0, 1, grid);
    scrollPanel.add(layout);
  }

  public void add(Widget w) {
    scrollPanel.add(w);
  }

  public DayViewGrid getDayViewGrid() {
    return grid;
  }

  public DayViewTimeline getDayViewTimeline() {
    return timeline;
  }

  public DayViewGrid getGrid() {
    return grid;
  }

  public ScrollPanel getScrollPanel() {
    return scrollPanel;
  }

  public DayViewTimeline getTimeline() {
    return timeline;
  }

  public void setDays(int days) {
    grid.build(settings.getSettings().getWorkingHourStart(),
        settings.getSettings().getWorkingHourEnd(), days);
  }
}
