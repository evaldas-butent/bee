package com.butent.bee.client.calendar.resourceview;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.time.HasDateValue;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class ResourceViewHeader extends Composite {

  private static final String CALENDAR_HEADER_STYLE = "bee-calendar-header";
  private static final String CELL_CONTAINER_STYLE = "day-cell-container";
  private static final String DATE_CELL_STYLE = "year-cell";
  private static final String SPLITTER_STYLE = "splitter";
  
  private final FlexTable header = new FlexTable();

  private final AbsolutePanel panel = new AbsolutePanel();
  private final AbsolutePanel splitter = new AbsolutePanel();

  public ResourceViewHeader() {
    initWidget(header);
    header.setStyleName(CALENDAR_HEADER_STYLE);

    panel.setStyleName(CELL_CONTAINER_STYLE);
    splitter.setStylePrimaryName(SPLITTER_STYLE);

    header.insertRow(0);
    header.insertRow(0);
  
    header.insertCell(0, 0);
    header.insertCell(0, 0);
    header.insertCell(0, 0);
    
    header.setWidget(0, 1, panel);
    
    header.getCellFormatter().setStyleName(0, 0, DATE_CELL_STYLE);
    header.getCellFormatter().setWidth(0, 2, DomUtils.getScrollBarWidth() + "px");

    header.getFlexCellFormatter().setColSpan(1, 0, 3);
    
    header.setCellPadding(0);
    header.setCellSpacing(0);
    header.setBorderWidth(0);

    header.setWidget(1, 0, splitter);
  }

  public void setAttendees(List<Long> attendees) {
    panel.clear();
    if (attendees.isEmpty()) {
      return;
    }

    int width = 100 / attendees.size();
    for (int i = 0; i < attendees.size(); i++) {
      Label label = new Label(CalendarKeeper.getAttendeeName(attendees.get(i)));
      label.setStylePrimaryName("day-cell");

      StyleUtils.setLeft(label, width * i, Unit.PCT);
      StyleUtils.setWidth(label, width, Unit.PCT);

      panel.add(label);
    }
  }

  public void setDate(HasDateValue date) {
    Assert.notNull(date);
    header.setText(0, 0, BeeUtils.concat('-', date.getMonth(), date.getDom()));
  }
}
