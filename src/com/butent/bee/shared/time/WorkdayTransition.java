package com.butent.bee.shared.time;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum WorkdayTransition implements HasCaption {
  NONE(Localized.dictionary().workdayTransitionNone()),
  NEAREST(Localized.dictionary().workdayTransitionNearest()),
  FORWARD(Localized.dictionary().workdayTransitionForward()),
  BACKWARD(Localized.dictionary().workdayTransitionBackward());

  public static final WorkdayTransition DEFAULT = NEAREST;

  private final String caption;

  WorkdayTransition(String caption) {
    this.caption = caption;
  }

  @Override
  public String getCaption() {
    return caption;
  }
}
