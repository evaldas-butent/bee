package com.butent.bee.shared.report;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum DateTimeFunction implements HasLocalizedCaption {
  YEAR() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.year();
    }
  },
  QUARTER() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.quarter();
    }
  },
  MONTH() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.month();
    }
  },
  DAY() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.day();
    }
  },
  DAY_OF_WEEK() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.dayOfWeek();
    }
  },
  DATE() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.date();
    }
  },
  DATETIME() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.dateTime();
    }
  },
  HOUR() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.hour();
    }
  },
  MINUTE() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.minute();
    }
  }
}