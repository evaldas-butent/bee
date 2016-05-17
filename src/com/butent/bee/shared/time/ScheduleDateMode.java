package com.butent.bee.shared.time;

import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasCaption;

public enum ScheduleDateMode implements HasCaption {
  INCLUDE(Localized.dictionary().scheduleDateInclude()),
  EXCLUDE(Localized.dictionary().scheduleDateExclude()),
  WORK(Localized.dictionary().scheduleDateWork()),
  NON_WORK(Localized.dictionary().scheduleDateNonWork());

  private final String caption;

  ScheduleDateMode(String caption) {
    this.caption = caption;
  }

  @Override
  public String getCaption() {
    return caption;
  }
}
