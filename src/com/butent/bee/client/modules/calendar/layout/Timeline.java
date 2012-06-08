package com.butent.bee.client.modules.calendar.layout;

import com.butent.bee.client.calendar.CalendarFormat;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.modules.calendar.CalendarStyleManager;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.modules.calendar.CalendarSettings;

public class Timeline extends Flow {

  public Timeline() {
    super();
  }

  public void build(CalendarSettings settings) {
    clear();
    
    int labelHeight = settings.getIntervalsPerHour() * settings.getPixelsPerInterval();

    for (int i = 0; i < 24; i++) {
      String hour = CalendarFormat.getHourLabels()[i];

      Simple hourWrapper = new Simple();
      hourWrapper.addStyleName(CalendarStyleManager.HOUR_PANEL);
      StyleUtils.setHeight(hourWrapper, labelHeight - 1);

      Flow flowPanel = new Flow();
      flowPanel.addStyleName(CalendarStyleManager.HOUR_LAYOUT);

      BeeLabel hourLabel = new BeeLabel(hour);
      hourLabel.addStyleName(CalendarStyleManager.HOUR_LABEL);
      flowPanel.add(hourLabel);
      
      hourWrapper.add(flowPanel);
      add(hourWrapper);
    }
  }
}
