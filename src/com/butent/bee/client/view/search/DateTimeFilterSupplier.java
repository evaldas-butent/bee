package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Callback;
import com.butent.bee.client.Global;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputTimeOfDay;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.NotificationListener;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;

public class DateTimeFilterSupplier extends AbstractFilterSupplier {

  private static final String STYLE_PREFIX = DEFAULT_STYLE_PREFIX + "dateTime-";

  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_DATE = STYLE_PREFIX + "date";
  private static final String STYLE_TIME = STYLE_PREFIX + "time";

  private static final String STYLE_SUFFIX_CELL = "Cell";

  private static final int START_ROW = 0;
  private static final int END_ROW = 1;

  private static final int LABEL_COL = 0;
  private static final int DATE_COL = 1;
  private static final int TIME_COL = 2;

  private Range<DateTime> range = null;

  public DateTimeFilterSupplier(String viewName, BeeColumn column, String options) {
    super(viewName, column, options);
  }

  @Override
  public String getLabel() {
    if (getRange() == null) {
      return null;
    }

    DateTime start = getStart();
    DateTime end = getEnd();

    if (start == null && end == null) {
      return null;

    } else if (start == null) {
      return "iki " + end.toCompactString();

    } else if (end == null) {
      return "nuo " + start.toCompactString();

    } else {
      return BeeUtils.join(" - ", start.toCompactString(), end.toCompactString());
    }
  }

  @Override
  public String getValue() {
    if (getRange() == null) {
      return null;
    }

    DateTime start = getStart();
    DateTime end = getEnd();
    if (start == null && end == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    if (start != null) {
      sb.append(start.getTime());
    }
    if (end != null) {
      sb.append(BeeConst.CHAR_COMMA).append(end.getTime());
    }

    return sb.toString();
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      Callback<Boolean> callback) {
    openDialog(target, createWidget(), callback);
  }

  @Override
  public Filter parse(String value) {
    Range<DateTime> r = parseRange(value);
    if (r == null) {
      return null;
    }

    DateTime start = r.hasLowerBound() ? r.lowerEndpoint() : null;
    DateTime end = r.hasUpperBound() ? r.upperEndpoint() : null;

    return buildFilter(start, end);
  }

  @Override
  public boolean reset() {
    setRange(null);
    return super.reset();
  }

  @Override
  public void setValue(String value) {
    setRange(parseRange(value));
  }

  @Override
  protected void doClear() {
    super.doClear();

    HtmlTable display = getDisplayAsTable();
    if (display == null) {
      return;
    }

    getInputDate(display, START_ROW).clearValue();
    getInputTimeOfDay(display, START_ROW).clearValue();

    getInputDate(display, END_ROW).clearValue();
    getInputTimeOfDay(display, END_ROW).clearValue();

    setRange(null);
  }

  @Override
  protected void doCommit() {
    DateTime start = getInputStartValue();
    DateTime end = getInputEndValue(start);

    if (start != null && end != null && TimeUtils.isMeq(start, end)) {
      List<String> messages = Lists.newArrayList("Neteisingas intervalas",
          start.toString(), end.toString());
      Global.showError(messages);
      return;
    }

    Range<DateTime> newRange = buildRange(start, end);
    boolean changed = !Objects.equal(getRange(), newRange);

    setRange(newRange);
    update(changed);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR);
  }

  private Filter buildFilter(DateTime start, DateTime end) {
    if (start == null && end == null) {
      return null;
    } else if (end == null) {
      return ComparisonFilter.isMoreEqual(getColumnId(), new DateTimeValue(start));
    } else if (start == null) {
      return ComparisonFilter.isLess(getColumnId(), new DateTimeValue(end));
    } else {
      return Filter.and(ComparisonFilter.isMoreEqual(getColumnId(), new DateTimeValue(start)),
          ComparisonFilter.isLess(getColumnId(), new DateTimeValue(end)));
    }
  }

  private Range<DateTime> buildRange(DateTime start, DateTime end) {
    if (start == null && end == null) {
      return null;
    } else if (start == null) {
      return Range.lessThan(end);
    } else if (end == null) {
      return Range.atLeast(start);
    } else {
      return Range.closedOpen(start, end);
    }
  }

  private Widget createWidget() {
    HtmlTable display = createDisplay(false);

    Html labelFrom = new Html("Nuo");
    labelFrom.addStyleName(STYLE_LABEL);
    display.setWidget(START_ROW, LABEL_COL, labelFrom, STYLE_LABEL + STYLE_SUFFIX_CELL);

    InputDate dateFrom = new InputDate();
    dateFrom.addStyleName(STYLE_DATE);
    display.setWidget(START_ROW, DATE_COL, dateFrom, STYLE_DATE + STYLE_SUFFIX_CELL);

    InputTimeOfDay timeFrom = new InputTimeOfDay();
    timeFrom.addStyleName(STYLE_TIME);
    display.setWidget(START_ROW, TIME_COL, timeFrom, STYLE_TIME + STYLE_SUFFIX_CELL);

    Html labelTo = new Html("Iki");
    labelTo.addStyleName(STYLE_LABEL);
    display.setWidget(END_ROW, LABEL_COL, labelTo, STYLE_LABEL + STYLE_SUFFIX_CELL);

    InputDate dateTo = new InputDate();
    dateTo.addStyleName(STYLE_DATE);
    display.setWidget(END_ROW, DATE_COL, dateTo, STYLE_DATE + STYLE_SUFFIX_CELL);

    InputTimeOfDay timeTo = new InputTimeOfDay();
    timeTo.addStyleName(STYLE_TIME);
    display.setWidget(END_ROW, TIME_COL, timeTo, STYLE_TIME + STYLE_SUFFIX_CELL);

    if (getRange() != null) {
      DateTime start = getStart();
      if (start != null) {
        getInputDate(display, START_ROW).setDate(start);
        getInputTimeOfDay(display, START_ROW).setTime(start);
      }

      DateTime end = getEnd();
      if (end != null) {
        getInputDate(display, END_ROW).setDate(end);
        getInputTimeOfDay(display, END_ROW).setTime(end);
      }
    }

    Widget wrapper = wrapDisplay(display, false);
    wrapper.addStyleName(STYLE_PREFIX + "container");

    return wrapper;
  }

  private DateTime getEnd() {
    return (getRange() != null && getRange().hasUpperBound()) ? getRange().upperEndpoint() : null;
  }

  private InputDate getInputDate(HtmlTable display, int row) {
    Widget widget = display.getWidget(row, DATE_COL);
    if (widget instanceof InputDate) {
      return (InputDate) widget;
    } else {
      return null;
    }
  }

  private DateTime getInputEndValue(DateTime start) {
    HtmlTable display = getDisplayAsTable();
    if (display == null) {
      return null;
    }

    JustDate datePart = null;

    Widget dateWidget = display.getWidget(END_ROW, DATE_COL);
    if (dateWidget instanceof InputDate) {
      datePart = ((InputDate) dateWidget).getDate();
    }

    Long timeMillis = null;

    Widget timeWidget = display.getWidget(END_ROW, TIME_COL);
    if (timeWidget instanceof InputTimeOfDay) {
      timeMillis = ((InputTimeOfDay) timeWidget).getMillis();
    }

    if (datePart == null && BeeUtils.isPositive(timeMillis) && start != null) {
      return TimeUtils.combine(start, timeMillis);

    } else if (datePart == null) {
      return null;

    } else {
      return TimeUtils.combine(datePart, timeMillis);
    }
  }

  private DateTime getInputStartValue() {
    HtmlTable display = getDisplayAsTable();
    if (display == null) {
      return null;
    }

    JustDate datePart = null;

    Widget dateWidget = display.getWidget(START_ROW, DATE_COL);
    if (dateWidget instanceof InputDate) {
      datePart = ((InputDate) dateWidget).getDate();
    }
    if (datePart == null) {
      return null;
    }

    Long timeMillis = null;

    Widget timeWidget = display.getWidget(START_ROW, TIME_COL);
    if (timeWidget instanceof InputTimeOfDay) {
      timeMillis = ((InputTimeOfDay) timeWidget).getMillis();
    }

    return TimeUtils.combine(datePart, timeMillis);
  }

  private InputTimeOfDay getInputTimeOfDay(HtmlTable display, int row) {
    Widget widget = display.getWidget(row, TIME_COL);
    if (widget instanceof InputTimeOfDay) {
      return (InputTimeOfDay) widget;
    } else {
      return null;
    }
  }

  private Range<DateTime> getRange() {
    return range;
  }

  private DateTime getStart() {
    return (getRange() != null && getRange().hasLowerBound()) ? getRange().lowerEndpoint() : null;
  }

  private Range<DateTime> parseRange(String value) {
    if (BeeUtils.isEmpty(value)) {
      return null;
    }

    DateTime start = null;
    DateTime end = null;

    int i = 0;
    for (String s : Splitter.on(BeeConst.CHAR_COMMA).trimResults().split(value)) {
      if (i == 0) {
        start = TimeUtils.toDateTimeOrNull(s);
      } else if (i == 1) {
        end = TimeUtils.toDateTimeOrNull(s);
      }

      i++;
    }

    return buildRange(start, end);
  }

  private void setRange(Range<DateTime> range) {
    this.range = range;
  }
}
