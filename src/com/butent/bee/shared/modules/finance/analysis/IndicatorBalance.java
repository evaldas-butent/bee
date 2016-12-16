package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.FinanceConstants;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum IndicatorBalance implements HasLocalizedCaption {
  TURNOVER {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finTurnover();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      return AnalysisUtils.getFilter(column, range);
    }
  },

  OPENING_BALANCE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finOpeningBalance();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      if (range == null) {
        return null;
      }

      YearMonth ym = range.getMinMonth();
      if (ym != null && BeeUtils.isMore(ym, FinanceConstants.ANALYSIS_MIN_YEAR_MONTH)) {
        return Filter.isLess(column, new DateTimeValue(ym.getDate().getDateTime()));
      } else {
        return null;
      }
    }
  },

  CLOSING_BALANCE {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finClosingBalance();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      if (range == null) {
        return null;
      }

      YearMonth ym = range.getMaxMonth();
      if (ym != null && BeeUtils.isLess(ym, FinanceConstants.ANALYSIS_MAX_YEAR_MONTH)) {
        return Filter.isLess(column, new DateTimeValue(ym.nextMonth().getDate().getDateTime()));
      } else {
        return null;
      }
    }
  };

  public static final IndicatorBalance DEFAULT = TURNOVER;

  public abstract Filter getFilter(String column, MonthRange range);
}
