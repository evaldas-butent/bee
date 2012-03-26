package com.butent.bee.client.calendar.dayview;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.calendar.CalendarFormat;
import com.butent.bee.client.calendar.DateUtils;
import com.butent.bee.client.dom.DomUtils;

import java.util.Date;

public class DayViewHeader extends Composite {

  private static final String GWT_CALENDAR_HEADER_STYLE = "bee-calendar-header";
  private static final String DAY_CELL_CONTAINER_STYLE = "day-cell-container";
  private static final String YEAR_CELL_STYLE = "year-cell";
  private static final String SPLITTER_STYLE = "splitter";
  
  private FlexTable header = new FlexTable();
  private AbsolutePanel dayPanel = new AbsolutePanel();
  private AbsolutePanel splitter = new AbsolutePanel();

  public DayViewHeader() {
    initWidget(header);
    header.setStyleName(GWT_CALENDAR_HEADER_STYLE);
    dayPanel.setStyleName(DAY_CELL_CONTAINER_STYLE);

    header.insertRow(0);
    header.insertRow(0);
    header.insertCell(0, 0);
    header.insertCell(0, 0);
    header.insertCell(0, 0);
    header.setWidget(0, 1, dayPanel);
    header.getCellFormatter().setStyleName(0, 0, YEAR_CELL_STYLE);
    header.getCellFormatter().setWidth(0, 2, DomUtils.getScrollBarWidth() + "px");

    header.getFlexCellFormatter().setColSpan(1, 0, 3);
    header.setCellPadding(0);
    header.setBorderWidth(0);
    header.setCellSpacing(0);

    splitter.setStylePrimaryName(SPLITTER_STYLE);
    header.setWidget(1, 0, splitter);
  }

  public void setDays(Date date, int days) {
    dayPanel.clear();
    double dayWidth = 100.0 / days;
    double dayLeft;

    for (int i = 0; i < days; i++) {
      if (i > 0) {
        DateUtils.moveOneDayForward(date);
      }

      dayLeft = dayWidth * i;

      String headerTitle = CalendarFormat.INSTANCE.getDateFormat().format(date);

      Label dayLabel = new Label();
      dayLabel.setStylePrimaryName("day-cell");
      dayLabel.setWidth(dayWidth + "%");
      dayLabel.setText(headerTitle);
      DOM.setStyleAttribute(dayLabel.getElement(), "left", dayLeft + "%");

      if (DateUtils.areOnTheSameDay(new Date(), date)) {
        dayLabel.setStyleName("day-cell-today");
      } else if (DateUtils.isWeekend(date)) {
        dayLabel.setStyleName("day-cell-weekend");
      }

      dayPanel.add(dayLabel);
    }
  }

  public void setYear(Date date) {
    setYear(DateUtils.year(date));
  }

  public void setYear(int year) {
    header.setText(0, 0, String.valueOf(year));
  }
}
