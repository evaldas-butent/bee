package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.Dimensions;
import com.butent.bee.shared.ui.HasLocalizedCaption;

public enum AnalysisSplit implements HasLocalizedCaption {
  MONTH(Type.PERIOD, 1) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.month();
    }
  },

  QUARTER(Type.PERIOD, 3) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.quarter();
    }
  },

  YEAR(Type.PERIOD, 12) {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.year();
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
    PERIOD(true, false),
    FILTER(true, true),
    DIMENSION(true, true);

    private final boolean columns;
    private final boolean rows;

    Type(boolean columns, boolean rows) {
      this.columns = columns;
      this.rows = rows;
    }
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

  public boolean visibleForColumns() {
    if (type.columns) {
      if (type == Type.DIMENSION) {
        return Dimensions.isObserved(index);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public boolean visibleForRows() {
    if (type.rows) {
      if (type == Type.DIMENSION) {
        return Dimensions.isObserved(index);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
}
