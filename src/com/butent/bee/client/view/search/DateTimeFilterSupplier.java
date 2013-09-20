package com.butent.bee.client.view.search;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputTimeOfDay;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.filter.ComparisonFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.i18n.Localized;
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
  
  private static final String STYLE_NOT_NULL = STYLE_PREFIX + "notNull";
  private static final String STYLE_NULL = STYLE_PREFIX + "null";

  private static final int START_ROW = 0;
  private static final int END_ROW = 1;
  private static final int EMPTINESS_ROW = 2;

  private static final int LABEL_COL = 0;
  private static final int DATE_COL = 1;
  private static final int TIME_COL = 2;

  private static final int NOT_NULL_COL = 1;
  private static final int NULL_COL = 2;

  private Range<DateTime> range;
  private Boolean emptiness;

  public DateTimeFilterSupplier(String viewName, BeeColumn column, String label, String options) {
    super(viewName, column, label, options);
  }

  @Override
  public FilterValue getFilterValue() {
    if (getRange() != null) {
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

      return FilterValue.of(sb.toString());

    } else if (getEmptiness() != null) {
      return FilterValue.of(null, getEmptiness());

    } else {
      return null;
    }
  }

  @Override
  public String getLabel() {
    if (getRange() != null) {
      DateTime start = getStart();
      DateTime end = getEnd();

      if (start == null && end == null) {
        return null;

      } else if (start == null) {
        return Localized.getConstants().dateToShort().toLowerCase() + " "
            + end.toCompactString();

      } else if (end == null) {
        return Localized.getConstants().dateFromShort().toLowerCase() + " "
            + start.toCompactString();

      } else {
        return BeeUtils.join(" - ", start.toCompactString(), end.toCompactString());
      }

    } else if (getEmptiness() != null) {
      return getEmptinessLabel(getEmptiness());
    
    } else {
      return null;
    }
  }

  @Override
  public void onRequest(Element target, Scheduler.ScheduledCommand onChange) {
    openDialog(target, createWidget(), onChange);
  }

  @Override
  public Filter parse(FilterValue input) {
    if (input == null) {
      return null;

    } else if (input.hasValue()) {
      Range<DateTime> r = parseRange(input.getValue());
      if (r == null) {
        return null;
      }

      DateTime start = r.hasLowerBound() ? r.lowerEndpoint() : null;
      DateTime end = r.hasUpperBound() ? r.upperEndpoint() : null;

      return buildFilter(start, end);

    } else {
      return getEmptinessFilter(getColumnId(), input.getEmptyValues());
    }
  }

  @Override
  public void setFilterValue(FilterValue filterValue) {
    if (filterValue == null) {
      clearValue();
    } else {
      setValue(parseRange(filterValue.getValue()), filterValue.getEmptyValues());
    }
  }

  @Override
  protected void doClear() {
    super.doClear();

    HtmlTable display = getDisplayAsTable();
    if (display == null) {
      return;
    }

    getInputDate(display, START_ROW).clearValue();
    getInputDate(display, END_ROW).clearValue();

    if (isDateTime()) {
      getInputTimeOfDay(display, START_ROW).clearValue();
      getInputTimeOfDay(display, END_ROW).clearValue();
    }
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
    boolean changed = !Objects.equal(getRange(), newRange) || getEmptiness() != null;
    
    if (changed) {
      setValue(newRange, null);
    }
    update(changed);
  }

  @Override
  protected List<SupplierAction> getActions() {
    return Lists.newArrayList(SupplierAction.COMMIT, SupplierAction.CLEAR, SupplierAction.CANCEL);
  }

  @Override
  protected String getDisplayStyle() {
    return STYLE_PREFIX + "display";
  }

  protected boolean isDateTime() {
    return true;
  }

  private Filter buildFilter(DateTime start, DateTime end) {
    if (start == null && end == null) {
      return null;
    } else if (end == null) {
      return ComparisonFilter.isMoreEqual(getColumnId(), getComparisonValue(start));
    } else if (start == null) {
      return ComparisonFilter.isLess(getColumnId(), getComparisonValue(end));
    } else {
      return Filter.and(ComparisonFilter.isMoreEqual(getColumnId(), getComparisonValue(start)),
          ComparisonFilter.isLess(getColumnId(), getComparisonValue(end)));
    }
  }

  private static Range<DateTime> buildRange(DateTime start, DateTime end) {
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

  private void clearValue() {
    setValue(null, null);
  }

  private Widget createWidget() {
    HtmlTable display = createDisplay(false);

    Html labelFrom = new Html(Localized.getConstants().dateFromShort());
    display.setWidgetAndStyle(START_ROW, LABEL_COL, labelFrom, STYLE_LABEL);

    InputDate dateFrom = new InputDate();
    display.setWidgetAndStyle(START_ROW, DATE_COL, dateFrom, STYLE_DATE);

    if (isDateTime()) {
      InputTimeOfDay timeFrom = new InputTimeOfDay();
      display.setWidgetAndStyle(START_ROW, TIME_COL, timeFrom, STYLE_TIME);
    }

    Html labelTo = new Html(Localized.getConstants().dateToShort());
    display.setWidgetAndStyle(END_ROW, LABEL_COL, labelTo, STYLE_LABEL);

    InputDate dateTo = new InputDate();
    display.setWidgetAndStyle(END_ROW, DATE_COL, dateTo, STYLE_DATE);

    if (isDateTime()) {
      InputTimeOfDay timeTo = new InputTimeOfDay();
      display.setWidgetAndStyle(END_ROW, TIME_COL, timeTo, STYLE_TIME);
    }

    if (getRange() != null) {
      DateTime start = getStart();
      if (start != null) {
        getInputDate(display, START_ROW).setDate(start);
        if (isDateTime() && TimeUtils.minutesSinceDayStarted(start) > 0) {
          getInputTimeOfDay(display, START_ROW).setTime(start);
        }
      }

      DateTime end = getEnd();
      if (end != null) {
        getInputDate(display, END_ROW).setDate(end);
        if (isDateTime() && TimeUtils.minutesSinceDayStarted(end) > 0) {
          getInputTimeOfDay(display, END_ROW).setTime(end);
        }
      }
    }
    
    if (hasEmptiness()) {
      Button notEmpty = new Button(NOT_NULL_VALUE_LABEL, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onEmptiness(false);
        }
      });
      display.setWidgetAndStyle(EMPTINESS_ROW, NOT_NULL_COL, notEmpty, STYLE_NOT_NULL);

      Button empty = new Button(NULL_VALUE_LABEL, new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          onEmptiness(true);
        }
      });
      display.setWidgetAndStyle(EMPTINESS_ROW, NULL_COL, empty, STYLE_NULL);
    }

    Widget wrapper = wrapDisplay(display, false);
    wrapper.addStyleName(STYLE_PREFIX + "container");

    return wrapper;
  }
  
  private Value getComparisonValue(DateTime dt) {
    if (isDateTime()) {
      return new DateTimeValue(dt);
    } else {
      return new DateValue(JustDate.get(dt));
    }
  }

  private Boolean getEmptiness() {
    return emptiness;
  }

  private DateTime getEnd() {
    return (getRange() != null && getRange().hasUpperBound()) ? getRange().upperEndpoint() : null;
  }

  private static InputDate getInputDate(HtmlTable display, int row) {
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

    if (isDateTime()) {
      Widget timeWidget = display.getWidget(END_ROW, TIME_COL);
      if (timeWidget instanceof InputTimeOfDay) {
        timeMillis = ((InputTimeOfDay) timeWidget).getMillis();
      }
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

    if (isDateTime()) {
      Widget timeWidget = display.getWidget(START_ROW, TIME_COL);
      if (timeWidget instanceof InputTimeOfDay) {
        timeMillis = ((InputTimeOfDay) timeWidget).getMillis();
      }
    }

    return TimeUtils.combine(datePart, timeMillis);
  }

  private static InputTimeOfDay getInputTimeOfDay(HtmlTable display, int row) {
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

  private void onEmptiness(Boolean value) {
    boolean changed = getRange() != null || !Objects.equal(getEmptiness(), value);
    setValue(null, value);
    update(changed);
  }

  private static Range<DateTime> parseRange(String value) {
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

  private void setValue(Range<DateTime> rng, Boolean empt) {
    this.range = rng;
    this.emptiness = empt;
  }
}
