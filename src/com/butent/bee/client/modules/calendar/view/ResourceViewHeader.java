package com.butent.bee.client.modules.calendar.view;

import com.google.gwt.user.client.ui.HasWidgets;

import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.modules.calendar.CalendarFormat;
import com.butent.bee.client.modules.calendar.CalendarKeeper;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.Html;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.CssUnit;

import java.util.List;

public class ResourceViewHeader extends Horizontal {

  private static final int DATE_CELL_INDEX = 0;
  private static final int CAPTION_CONTAINER_INDEX = 1;

  public ResourceViewHeader() {
    super();
    addStyleName(CalendarStyleManager.CALENDAR_HEADER);

    BeeLabel dateLabel = new BeeLabel();
    add(dateLabel);
    addStyleToCell(dateLabel, CalendarStyleManager.DATE_CELL);
    
    Flow captionPanel = new Flow();
    captionPanel.addStyleName(CalendarStyleManager.RESOURCE_CAPTION_CONTAINER);
    add(captionPanel);
    
    Html filler = new Html();
    add(filler);
    setCellWidth(filler, DomUtils.getScrollBarWidth());
  }

  public void setAttendees(long calendarId, List<Long> attendees) {
    HasWidgets panel = (HasWidgets) getWidget(CAPTION_CONTAINER_INDEX); 
    panel.clear();
    if (attendees.isEmpty()) {
      return;
    }

    int width = 100 / attendees.size();
    for (int i = 0; i < attendees.size(); i++) {
      String caption = CalendarKeeper.getAttendeeCaption(calendarId, attendees.get(i));
      BeeLabel label = new BeeLabel(caption);
      label.addStyleName(CalendarStyleManager.RESOURCE_CAPTION_CELL);

      StyleUtils.setLeft(label, width * i, CssUnit.PCT);
      StyleUtils.setWidth(label, width, CssUnit.PCT);

      panel.add(label);
    }
  }

  public void setDate(JustDate date) {
    getWidget(DATE_CELL_INDEX).getElement().setInnerHTML(CalendarFormat.formatWeekOfYear(date));
    setStyleName(CalendarStyleManager.TODAY, TimeUtils.isToday(date));
  }
}
