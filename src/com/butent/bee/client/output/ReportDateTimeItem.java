package com.butent.bee.client.output;

import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputDateTime;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.EnumSet;

public class ReportDateTimeItem extends ReportDateItem {

  private InputDateTime filterFrom;
  private InputDateTime filterTo;

  public ReportDateTimeItem(String name, String caption) {
    super(name, caption);
  }

  @Override
  public void clearFilter() {
    if (filterFrom != null) {
      filterFrom.clearValue();
      filterTo.clearValue();
    }
  }

  @Override
  public String evaluate(SimpleRow row) {
    String value = null;
    DateTime dateTime = row.getDateTime(getName());

    if (dateTime != null) {
      switch (getFormat()) {
        case DATETIME:
          value = dateTime.toCompactString();
          break;
        case DATE:
          value = dateTime.toDateString();
          break;
        case HOUR:
          value = TimeUtils.padTwo(dateTime.getHour());
          break;
        case MINUTE:
          value = TimeUtils.padTwo(dateTime.getMinute());
          break;
        case TIME:
          value = dateTime.toCompactTimeString();
          break;
        default:
          value = super.evaluate(row);
          break;
      }
    }
    return value;
  }

  @Override
  public String getFilter() {
    if (filterFrom == null) {
      return null;
    }
    return Codec.beeSerialize(Pair.of(filterFrom.getDateTime(), filterTo.getDateTime()));
  }

  @Override
  public Widget getFilterWidget() {
    Flow container = new Flow(getStyle() + "-filter");

    if (filterFrom == null) {
      filterFrom = new InputDateTime();
    }
    container.add(filterFrom);
    container.add(new InlineLabel("-"));

    if (filterTo == null) {
      filterTo = new InputDateTime();
    }
    container.add(filterTo);
    return container;
  }

  @Override
  public String getStyle() {
    return STYLE_DATETIME;
  }

  @Override
  public ReportItem setFilter(String data) {
    getFilterWidget();

    if (BeeUtils.allNotNull(filterFrom, filterTo, data)) {
      Pair<String, String> pair = Pair.restore(data);
      filterFrom.setDate(TimeUtils.toDateTimeOrNull(pair.getA()));
      filterTo.setDate(TimeUtils.toDateTimeOrNull(pair.getB()));
    }
    return this;
  }

  @Override
  public boolean validate(SimpleRow row) {
    if (filterFrom == null || !row.getRowSet().hasColumn(getName())) {
      return true;
    }
    DateTime from = filterFrom.getDateTime();
    DateTime to = filterTo.getDateTime();

    if (from != null && to != null && TimeUtils.isMeq(from, to)) {
      return false;
    }
    return TimeUtils.isBetweenExclusiveNotRequired(row.getDateTime(getName()), from, to);
  }

  @Override
  protected EnumSet<DateTimeFunction> getSupportedFunctions() {
    return EnumSet.allOf(DateTimeFunction.class);
  }
}
