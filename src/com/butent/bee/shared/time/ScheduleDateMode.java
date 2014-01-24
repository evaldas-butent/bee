package com.butent.bee.shared.time;

import com.butent.bee.shared.ui.HasCaption;

public enum ScheduleDateMode implements HasCaption {
  INCLUDE("include"),
  EXCLUDE("exclude"),
  WORK("work"),
  NON_WORK("fiesta");
  
  private final String caption;
  
  private ScheduleDateMode(String caption) {
    this.caption = caption;
  }
 
  @Override
  public String getCaption() {
    return caption;
  }
}
