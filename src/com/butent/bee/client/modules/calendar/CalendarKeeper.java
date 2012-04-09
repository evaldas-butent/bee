package com.butent.bee.client.modules.calendar;

import com.butent.bee.client.Global;
import com.butent.bee.client.calendar.CalendarSettings;
import com.butent.bee.shared.modules.calendar.CalendarConstants;

public class CalendarKeeper {

  public static void register() {
    Global.registerCaptions(CalendarConstants.AppointmentStatus.class);    
    Global.registerCaptions(CalendarConstants.ReminderMethod.class);    
    Global.registerCaptions(CalendarConstants.ResponseStatus.class);    
    Global.registerCaptions(CalendarConstants.Transparency.class);    
    Global.registerCaptions("Calendar_Visibility", CalendarConstants.Visibility.class);    

    Global.registerCaptions(CalendarSettings.TimeBlockClick.class);    
  }

  private CalendarKeeper() {
  }
}
