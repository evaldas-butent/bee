package com.butent.bee.shared.modules.transport;

import com.google.common.collect.Range;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Objects;

public final class TransportUtils {

  public static GridInterceptor.DeleteMode checkExported(GridPresenter presenter, IsRow row) {
    if (row.getDateTime(DataUtils.getColumnIndex(TradeConstants.COL_TRADE_EXPORTED,
        presenter.getDataColumns())) != null
        && !BeeKeeper.getUser().isWidgetVisible(RegulatedWidget.DROP_EXPORTED)) {

      presenter.getGridView().notifyWarning(Localized.dictionary().rowIsNotRemovable());
      return GridInterceptor.DeleteMode.CANCEL;
    }
    return GridInterceptor.DeleteMode.SINGLE;
  }

  public static Range<Value> getChartPeriod(JustDate from, JustDate to) {
    DateValue lower = (from == null) ? null : new DateValue(from);
    DateValue upper = (to == null) ? null : new DateValue(to);

    Range<Value> period;

    if (from == null && to == null) {
      JustDate min = TimeUtils.startOfMonth(TimeUtils.today(), -1);
      period = Range.atLeast(new DateValue(min));

    } else if (from == null) {
      period = Range.lessThan(upper);

    } else if (to == null) {
      period = Range.atLeast(lower);

    } else if (Objects.equals(from, to)) {
      period = Range.singleton(lower);

    } else if (BeeUtils.isMore(from, to)) {
      period = Range.closedOpen(upper, lower);

    } else {
      period = Range.closedOpen(lower, upper);
    }

    return period;
  }

  private TransportUtils() {
  }
}
