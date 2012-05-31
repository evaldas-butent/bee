package com.butent.bee.client.calendar.dayview;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.calendar.CalendarFormat;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;

public class DayViewHeader extends Composite {

  private static final String GWT_CALENDAR_HEADER_STYLE = "bee-calendar-header";
  private static final String DAY_CELL_CONTAINER_STYLE = "day-cell-container";
  private static final String YEAR_CELL_STYLE = "year-cell";
  private static final String SPLITTER_STYLE = "splitter";
  
  private final FlexTable header = new FlexTable();

  private final AbsolutePanel dayPanel = new AbsolutePanel();
  private final AbsolutePanel splitter = new AbsolutePanel();

  public DayViewHeader() {
    initWidget(header);
    header.setStyleName(GWT_CALENDAR_HEADER_STYLE);

    dayPanel.setStyleName(DAY_CELL_CONTAINER_STYLE);
    splitter.setStylePrimaryName(SPLITTER_STYLE);

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
    header.setCellSpacing(0);
    header.setBorderWidth(0);

    header.setWidget(1, 0, splitter);
  }

  public void setDays(JustDate date, int days) {
    dayPanel.clear();

    int dayWidth = 100 / days;
    JustDate tmp = JustDate.copyOf(date);

    for (int i = 0; i < days; i++) {
      Label dayLabel = new Label(CalendarFormat.format(tmp));
      dayLabel.setStylePrimaryName("day-cell");

      StyleUtils.setLeft(dayLabel, dayWidth * i, Unit.PCT);
      StyleUtils.setWidth(dayLabel, dayWidth, Unit.PCT);

      if (TimeUtils.isToday(tmp)) {
        dayLabel.setStyleName("day-cell-today");
      } else if (TimeUtils.isWeekend(tmp)) {
        dayLabel.setStyleName("day-cell-weekend");
      }

      dayPanel.add(dayLabel);
      TimeUtils.moveOneDayForward(tmp);
    }
  }

  public void setYear(HasDateValue date) {
    Assert.notNull(date);
    setYear(date.getYear());
  }

  public void setYear(int year) {
    header.setText(0, 0, String.valueOf(year));
  }
}
