package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum AnalysisValue implements HasLocalizedCaption {
  ACTUAL('a') {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueActual();
    }
  },

  BUDGET('b') {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueBudget();
    }
  },

  DIFFERENCE('d') {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueDifference();
    }
  },

  PERCENTAGE('p') {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValuePercentage();
    }
  };

  public static AnalysisValue parse(char c) {
    for (AnalysisValue av : values()) {
      if (av.code == c) {
        return av;
      }
    }
    return null;
  }

  private final char code;

  AnalysisValue(char code) {
    this.code = code;
  }
}
