package com.butent.bee.shared.modules.trade;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum TradeVatMode implements HasLocalizedCaption {
  PLUS {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.vatModePlus();
    }

    @Override
    public double computePercent(double x, double p) {
      return x * p / 100d;
    }
  },

  INCLUSIVE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.vatModeInclusive();
    }

    @Override
    public double computePercent(double x, double p) {
      return x * p / (p + 100d);
    }
  };

  public abstract double computePercent(double x, double p);
}
