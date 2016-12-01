package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum AnalysisValueType implements HasLocalizedCaption {

  ACTUAL('a') {
    @Override
    public String getAbbreviation() {
      return Localized.dictionary().finAnalysisValueActualShort();
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueActual();
    }
  },

  BUDGET('b') {
    @Override
    public String getAbbreviation() {
      return Localized.dictionary().finAnalysisValueBudgetShort();
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueBudget();
    }
  },

  DIFFERENCE('d') {
    @Override
    public String getAbbreviation() {
      return Localized.dictionary().finAnalysisValueDifferenceShort();
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValueDifference();
    }
  },

  PERCENTAGE('p', 1) {
    @Override
    public String getAbbreviation() {
      return Localized.dictionary().finAnalysisValuePercentageShort();
    }

    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finAnalysisValuePercentage();
    }
  };

  public static AnalysisValueType parse(char c) {
    for (AnalysisValueType av : values()) {
      if (av.code == c) {
        return av;
      }
    }
    return null;
  }

  public static AnalysisValueType parse(String s) {
    if (!BeeUtils.isEmpty(s) && s.trim().length() == 1) {
      return parse(s.trim().charAt(0));
    } else {
      return null;
    }
  }

  private final char code;
  private final int defaultScale;

  AnalysisValueType(char code) {
    this(code, BeeConst.UNDEF);
  }

  AnalysisValueType(char code, int defaultScale) {
    this.code = code;
    this.defaultScale = defaultScale;
  }

  public abstract String getAbbreviation();

  public char getCode() {
    return code;
  }

  public int getDefaultScale() {
    return defaultScale;
  }

  public boolean hasScale() {
    return !BeeConst.isUndef(defaultScale);
  }
}
