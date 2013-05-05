package com.butent.bee.client.view.search;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
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

  private DateTime startValue = null;
  private DateTime endValue = null;

  public DateTimeFilterSupplier(String viewName, BeeColumn column, String options) {
    super(viewName, column, options);
  }

  @Override
  public String getDisplayHtml() {
    if (getStartValue() == null && getEndValue() == null) {
      return null;

    } else if (getStartValue() == null) {
      return "iki " + getEndValue().toCompactString();

    } else if (getEndValue() == null) {
      return "nuo " + getStartValue().toCompactString();

    } else {
      return BeeUtils.join(" - ",
          getStartValue().toCompactString(), getEndValue().toCompactString());
    }
  }

  @Override
  public void onRequest(Element target, NotificationListener notificationListener,
      Callback<Boolean> callback) {
    openDialog(target, createWidget(), callback);
  }

  @Override
  public Filter parse(String values) {
    if (BeeUtils.isEmpty(values)) {
      return null;
    }

    DateTime start = null;
    DateTime end = null;

    int i = 0;
    for (String s : Splitter.on(BeeConst.CHAR_COMMA).trimResults().split(values)) {
      if (i == 0) {
        start = TimeUtils.toDateTimeOrNull(s);
      } else if (i == 1) {
        end = TimeUtils.toDateTimeOrNull(s);
      }

      i++;
    }

    return buildFilter(start, end);
  }

  @Override
  public boolean reset() {
    setStartValue(null);
    setEndValue(null);
    return super.reset();
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

    setStartValue(null);
    setEndValue(null);
  }

  @Override
  protected void doCommit() {
    DateTime start = getStart();
    DateTime end = getEnd(start);

    if (start != null && end != null && TimeUtils.isMeq(start, end)) {
      List<String> messages = Lists.newArrayList("Neteisingas intervalas",
          start.toString(), end.toString());
      Global.showError(messages);
      return;
    }

    setStartValue(start);
    setEndValue(end);

    update(buildFilter(start, end));
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

    if (getStartValue() != null) {
      getInputDate(display, START_ROW).setDate(getStartValue());
      getInputTimeOfDay(display, START_ROW).setTime(getStartValue());
    }

    if (getEndValue() != null) {
      getInputDate(display, END_ROW).setDate(getEndValue());
      getInputTimeOfDay(display, END_ROW).setTime(getEndValue());
    }

    Widget wrapper = wrapDisplay(display, false);
    wrapper.addStyleName(STYLE_PREFIX + "container");

    return wrapper;
  }

  private DateTime getEnd(DateTime start) {
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

  private DateTime getEndValue() {
    return endValue;
  }

  private InputDate getInputDate(HtmlTable display, int row) {
    Widget widget = display.getWidget(row, DATE_COL);
    if (widget instanceof InputDate) {
      return (InputDate) widget;
    } else {
      return null;
    }
  }

  private InputTimeOfDay getInputTimeOfDay(HtmlTable display, int row) {
    Widget widget = display.getWidget(row, TIME_COL);
    if (widget instanceof InputTimeOfDay) {
      return (InputTimeOfDay) widget;
    } else {
      return null;
    }
  }

  private DateTime getStart() {
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

  private DateTime getStartValue() {
    return startValue;
  }

  private void setEndValue(DateTime endValue) {
    this.endValue = endValue;
  }

  private void setStartValue(DateTime startValue) {
    this.startValue = startValue;
  }
}
