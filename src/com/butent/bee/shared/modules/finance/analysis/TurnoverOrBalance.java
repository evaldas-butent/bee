package com.butent.bee.shared.modules.finance.analysis;

import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.modules.finance.FinanceConstants;
import com.butent.bee.shared.time.MonthRange;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.ui.HasLocalizedCaption;
import com.butent.bee.shared.utils.BeeUtils;

public enum TurnoverOrBalance implements HasLocalizedCaption {
  TURNOVER {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finTurnover();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      return AnalysisUtils.getFilter(column, range);
    }

    @Override
    public String getIndicatorName(Dictionary dictionary, String accountName) {
      return dictionary.finIndicatorNameTurnover(accountName);
    }

    @Override
    public String getIndicatorAbbreviation(Dictionary dictionary, String accountCode) {
      return dictionary.finIndicatorAbbreviationTurnover(accountCode);
    }
  },

  DEBIT_ONLY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finDebitOnly();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      return AnalysisUtils.getFilter(column, range);
    }

    @Override
    public String getIndicatorName(Dictionary dictionary, String accountName) {
      return dictionary.finIndicatorNameDebit(accountName);
    }

    @Override
    public String getIndicatorAbbreviation(Dictionary dictionary, String accountCode) {
      return dictionary.finIndicatorAbbreviationDebit(accountCode);
    }
  },

  CREDIT_ONLY {
    @Override
    public String getCaption(Dictionary dictionary) {
      return dictionary.finCreditOnly();
    }

    @Override
    public Filter getFilter(String column, MonthRange range) {
      return AnalysisUtils.getFilter(column, range);
    }

    @Override
    public String getIndicatorName(Dictionary dictionary, String accountName) {
      return dictionary.finIndicatorNameCredit(accountName);
    }

    @Override
    public String getIndicatorAbbreviation(Dictionary dictionary, String accountCode) {
      return dictionary.finIndicatorAbbreviationCredit(accountCode);
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

    @Override
    public String getIndicatorName(Dictionary dictionary, String accountName) {
      return dictionary.finIndicatorNameOpeningBalance(accountName);
    }

    @Override
    public String getIndicatorAbbreviation(Dictionary dictionary, String accountCode) {
      return dictionary.finIndicatorAbbreviationOpeningBalance(accountCode);
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

    @Override
    public String getIndicatorName(Dictionary dictionary, String accountName) {
      return dictionary.finIndicatorNameClosingBalance(accountName);
    }

    @Override
    public String getIndicatorAbbreviation(Dictionary dictionary, String accountCode) {
      return dictionary.finIndicatorAbbreviationClosingBalance(accountCode);
    }
  };

  public static final TurnoverOrBalance DEFAULT = TURNOVER;

  public abstract Filter getFilter(String column, MonthRange range);

  public abstract String getIndicatorName(Dictionary dictionary, String accountName);

  public abstract String getIndicatorAbbreviation(Dictionary dictionary, String accountCode);
}
