package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum IndicatorKind implements HasLocalizedCaption {
  PRIMARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorPrimary();
    }
  },

  SECONDARY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finIndicatorSecondary();
    }
  }
}
