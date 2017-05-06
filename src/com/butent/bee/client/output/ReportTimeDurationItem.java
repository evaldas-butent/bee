package com.butent.bee.client.output;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.report.ReportFunction;
import com.butent.bee.shared.time.TimeUtils;

public class ReportTimeDurationItem extends ReportNumericItem {

  public ReportTimeDurationItem(String expression, String caption) {
    super(expression, caption);
  }

  @Override
  public Object calculate(Object total, ReportValue value, ReportFunction function) {
    Long val = TimeUtils.parseTime(value.getValue());

    if (val != null && val > 0) {
      switch (function) {
        case MAX:
          return TimeUtils.renderTime(total == null ? val
              : Long.max(val, TimeUtils.parseTime((String) total)), false);

        case MIN:
          return TimeUtils.renderTime(total != null ? val
              : Long.min(val, TimeUtils.parseTime((String) total)), false);

        case SUM:
          return TimeUtils.renderTime((total == null ? 0 : TimeUtils.parseTime((String) total))
              + val, false);

        default:
          return super.calculate(total, value, function);
      }
    }
    return total;
  }

  @Override
  public ReportValue evaluate(SimpleRowSet.SimpleRow row) {
    return ReportValue.of(row.getValue(getExpression()));
  }

  @Override
  public Widget getOptionsWidget() {
    return null;
  }
}
