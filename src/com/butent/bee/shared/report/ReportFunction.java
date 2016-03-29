package com.butent.bee.shared.report;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum ReportFunction implements HasLocalizedCaption {
  MIN() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.least();
    }
  },
  MAX() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.greatest();
    }
  },
  SUM() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.amount();
    }
  },
  COUNT() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.quantity();
    }
  },
  LIST() {
    @Override
    public String getCaption(Dictionary constants) {
      return constants.list();
    }
  };
}