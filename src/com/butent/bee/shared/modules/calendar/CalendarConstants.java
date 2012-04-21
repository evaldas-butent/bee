package com.butent.bee.shared.modules.calendar;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

public class CalendarConstants {
  
  public static enum AppointmentStatus implements HasCaption {
    TENTATIVE("Planuojamas"),
    CONFIRMED("Patvirtintas"),
    DELAYED("Atidėtas"),
    CANCELED("Atšauktas");
    
    private final String caption;
    
    private AppointmentStatus(String caption) {
      this.caption = caption;
    }

    public String getCaption() {
      return caption;
    }
  }

  public static enum ReminderMethod implements HasCaption {
    EMAIL, SMS, POPUP;

    public String getCaption() {
      return this.name().toLowerCase();
    } 
  }

  public static enum ResponseStatus implements HasCaption {
    NEEDS_ACTION, DECLINED, TENTATIVE, ACCEPTED;

    public String getCaption() {
      return BeeUtils.proper(this.name(), BeeConst.CHAR_UNDER);
    }
  }
  
  public static enum Transparency implements HasCaption {
    OPAQUE, TRANSPARENT;

    public String getCaption() {
      return this.name().toLowerCase();
    }
  }

  public static enum Visibility implements HasCaption {
    DEFAULT, PUBLIC, PRIVATE, CONFIDENTIAL;

    public String getCaption() {
      return this.name().toLowerCase();
    }
  }
  
  public static final String CALENDAR_MODULE = "Calendar";
  public static final String CALENDAR_METHOD = CALENDAR_MODULE + "Method";

  private CalendarConstants() {
  }
}
