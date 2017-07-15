package com.butent.bee.client.modules.trade;

import com.butent.bee.client.view.search.DateFilterSupplier;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.Operator;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.DateTime;

public class NoPaymentPeriodFilterSuppler extends DateFilterSupplier {

  public NoPaymentPeriodFilterSuppler(String viewName, BeeColumn column, String label,
      String options) {
    super(viewName, column, label, options);
  }

  @Override
  protected Filter buildFilter(DateTime start, DateTime end) {
    Filter filter = null;
    Filter filter1 = null;
    Filter filter2 = Filter.isNull(TradeConstants.COL_SALE_LASTEST_PAYMENT);
    Filter filterFrom = null;
    Filter filterTill = null;

    if (end != null) {
      filter = Filter.compareWithValue(TradeConstants.COL_SALE_LASTEST_PAYMENT, Operator.GT,
          Value.getValue(end));
    }

    if (start != null) {
      filter1 = Filter.compareWithValue(TradeConstants.COL_SALE_LASTEST_PAYMENT, Operator.LT,
          Value.getValue(start));
    }

    if (start != null) {
      filterFrom = Filter.compareWithValue(TradeConstants.COL_SALE_LASTEST_PAYMENT, Operator.LT,
          Value.getValue(start));

      if (filter != null) {
        filterFrom = Filter.or(filterFrom, filter);
      }

      filterFrom = Filter.or(filterFrom, filter2);
    }

    if (end != null) {
      filterTill = Filter.compareWithValue(TradeConstants.COL_SALE_LASTEST_PAYMENT, Operator.GT,
          Value.getValue(end));

      if (filter1 != null) {
        filterTill = Filter.or(filterTill, filter1);
      }

      filterTill = Filter.or(filterTill, filter2);
    }

    if (filterFrom != null && filterTill != null) {
      return Filter.and(filterFrom, filterTill);
    } else if (filterFrom != null) {
      return filterFrom;
    } else {
      return filterTill;
    }
  }
}
