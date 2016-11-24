package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum AnalysisSplit implements HasLocalizedCaption {
  YEAR(Type.PERIOD) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.year();
    }
  },

  QUARTER(Type.PERIOD) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.quarter();
    }
  },

  MONTH(Type.PERIOD) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.month();
    }
  },

  EMPLOYEE(Type.FILTER) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.employee();
    }
  },

  DIMENSION_01(Type.DIMENSION, 1),
  DIMENSION_02(Type.DIMENSION, 2),
  DIMENSION_03(Type.DIMENSION, 3),
  DIMENSION_04(Type.DIMENSION, 4),
  DIMENSION_05(Type.DIMENSION, 5),
  DIMENSION_06(Type.DIMENSION, 6),
  DIMENSION_07(Type.DIMENSION, 7),
  DIMENSION_08(Type.DIMENSION, 8),
  DIMENSION_09(Type.DIMENSION, 9),
  DIMENSION_10(Type.DIMENSION, 10);

  private enum Type {
    PERIOD, FILTER, DIMENSION
  }

  private final Type type;
  private final int index;

  AnalysisSplit(Type type) {
    this(type, BeeConst.UNDEF);
  }

  AnalysisSplit(Type type, int index) {
    this.type = type;
    this.index = index;
  }

  @Override
  public String getCaption(Dictionary dictionary) {
    if (type == Type.DIMENSION) {
      return Dimensions.singular(index);
    } else {
      return null;
    }
  }
}
