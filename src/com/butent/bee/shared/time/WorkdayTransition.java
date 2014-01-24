package com.butent.bee.shared.time;

import com.butent.bee.shared.ui.HasCaption;

public enum WorkdayTransition implements HasCaption {
  NONE("none"),
  NEAREST("nearest"),
  FORWARD("forward"),
  BACKWARD("backward");

  public static final WorkdayTransition DEFAULT = NEAREST;
  
  private final String caption;
  
  private WorkdayTransition(String caption) {
    this.caption = caption;
  }
 
  @Override
  public String getCaption() {
    return caption;
  }
}
