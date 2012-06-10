package com.butent.bee.client.datepicker;

import com.google.common.collect.Maps;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.HasHighlightHandlers;
import com.google.gwt.event.logical.shared.HasShowRangeHandlers;
import com.google.gwt.event.logical.shared.HighlightEvent;
import com.google.gwt.event.logical.shared.HighlightHandler;
import com.google.gwt.event.logical.shared.ShowRangeEvent;
import com.google.gwt.event.logical.shared.ShowRangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.time.YearMonth;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Map;

public class DatePicker extends Composite implements HasHighlightHandlers<JustDate>,
    HasShowRangeHandlers<JustDate>, HasValue<JustDate>, HasKeyDownHandlers {

  public static class CssClasses {

    private final String widgetStyleName;
    private final String baseStyleName;

    public CssClasses(String widgetStyleName, String baseStyleName) {
      this.widgetStyleName = widgetStyleName;
      this.baseStyleName = baseStyleName;
    }

    public String datePicker() {
      return getWidgetStyleName();
    }

    public String day() {
      return wrap("day");
    }

    public String day(String dayModifier) {
      return day() + "Is" + dayModifier;
    }

    public String dayIsDisabled() {
      return day("Disabled");
    }

    public String dayIsFiller() {
      return day("Filler");
    }

    public String dayIsHighlighted() {
      return day("Highlighted");
    }

    public String dayIsToday() {
      return day("Today");
    }

    public String dayIsValue() {
      return day("Value");
    }

    public String dayIsValueAndHighlighted() {
      return dayIsValue() + "AndHighlighted";
    }

    public String dayIsWeekend() {
      return day("Weekend");
    }

    public String days() {
      return wrap("days");
    }

    public String daysLabel() {
      return wrap("daysLabel");
    }

    public String getBaseStyleName() {
      return baseStyleName;
    }

    public String getWidgetStyleName() {
      return widgetStyleName;
    }

    public String month() {
      return wrap("month");
    }

    public String monthNavigation() {
      return wrap("monthNavigation");
    }

    public String monthSelector() {
      return wrap("monthSelector");
    }

    public String weekdayLabel() {
      return wrap("weekdayLabel");
    }

    public String weekendLabel() {
      return wrap("weekendLabel");
    }

    protected String wrap(String style) {
      return baseStyleName + style;
    }
  }

  private static class DateStyler {
    private final Map<JustDate, String> styles = Maps.newHashMap();

    private DateStyler() {
    }

    public String getStyleName(JustDate date) {
      Assert.notNull(date);
      return styles.get(date);
    }

    public void setStyleName(JustDate date, String styleName, boolean add) {
      Assert.notNull(date);
      Assert.notEmpty(styleName);

      String current = styles.get(date);

      if (add) {
        if (current == null) {
          styles.put(date, styleName.trim());
        } else {
          styles.put(date, StyleUtils.addClassName(current, styleName));
        }

      } else if (current != null) {
        String newValue = StyleUtils.removeClassName(current, styleName);
        if (BeeUtils.isEmpty(newValue)) {
          styles.remove(date);
        } else {
          styles.put(date, newValue);
        }
      }
    }
  }

  private static final CssClasses DEFAULT_CSS_CLASSES =
      new CssClasses("bee-DatePicker", "bee-DatePicker-");

  private final DateStyler dateStyler = new DateStyler();

  private final CssClasses cssClasses;

  private final MonthSelector monthSelector;
  private final MonthView view;
  private final Model model;

  private JustDate value;
  private JustDate highlighted;

  public DatePicker(JustDate date) {
    this(date, DEFAULT_CSS_CLASSES);
  }

  public DatePicker(JustDate date, CssClasses cssClasses) {
    this(date, cssClasses, new MonthSelector(cssClasses), new MonthView(cssClasses),
        new Model(date));
  }

  public DatePicker(JustDate date, CssClasses cssClasses, MonthSelector monthSelector,
      MonthView view, Model model) {

    Assert.notNull(date);
    Assert.notNull(cssClasses);
    Assert.notNull(monthSelector);
    Assert.notNull(view);
    Assert.notNull(model);

    this.cssClasses = cssClasses;
    this.monthSelector = monthSelector;
    this.view = view;
    this.model = model;

    monthSelector.setDatePicker(this);
    view.setDatePicker(this);

    view.setup();
    monthSelector.setup();
    this.setup();

    addStyleToDate(cssClasses.dayIsToday(), new JustDate());
    setDate(date);
  }

  public HandlerRegistration addHighlightHandler(HighlightHandler<JustDate> handler) {
    return addHandler(handler, HighlightEvent.getType());
  }

  public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
    return view.addKeyDownHandler(handler);
  }

  public HandlerRegistration addShowRangeHandler(ShowRangeHandler<JustDate> handler) {
    return addHandler(handler, ShowRangeEvent.getType());
  }

  public void addStyleToDate(String styleName, JustDate date) {
    dateStyler.setStyleName(date, styleName, true);
    if (isDateVisible(date)) {
      getView().addStyleToDate(styleName, date);
    }
  }

  public void addStyleToDates(String styleName, Iterable<JustDate> dates) {
    for (JustDate date : dates) {
      addStyleToDate(styleName, date);
    }
  }

  public void addTransientStyleToDate(String styleName, JustDate date) {
    Assert.state(isDateVisible(date), "date must be visible");
    getView().addStyleToDate(styleName, date);
  }

  public void addTransientStyleToDates(String styleName, Iterable<JustDate> dates) {
    for (JustDate date : dates) {
      addTransientStyleToDate(styleName, date);
    }
  }

  public HandlerRegistration addValueChangeHandler(ValueChangeHandler<JustDate> handler) {
    return addHandler(handler, ValueChangeEvent.getType());
  }

  public CssClasses getCssClasses() {
    return cssClasses;
  }

  public YearMonth getCurrentMonth() {
    return getModel().getCurrentMonth();
  }

  public JustDate getFirstDate() {
    return getView().getFirstDate();
  }

  public JustDate getHighlightedDate() {
    return JustDate.copyOf(highlighted);
  }

  public JustDate getLastDate() {
    return getView().getLastDate();
  }

  public Model getModel() {
    return model;
  }

  public MonthSelector getMonthSelector() {
    return monthSelector;
  }

  public String getStyleOfDate(JustDate date) {
    return dateStyler.getStyleName(date);
  }

  public JustDate getValue() {
    return JustDate.copyOf(value);
  }

  public MonthView getView() {
    return view;
  }

  public boolean isDateEnabled(JustDate date) {
    Assert.state(isDateVisible(date), "date is not visible");
    return getView().isDateEnabled(date);
  }

  public boolean isDateVisible(JustDate date) {
    Assert.notNull(date);
    return TimeUtils.isBetweenInclusiveRequired(date, getFirstDate(), getLastDate());
  }

  public void refreshAll() {
    this.highlighted = null;

    getView().refresh();
    getMonthSelector().refresh();
    if (isAttached()) {
      ShowRangeEvent.fire(this, getFirstDate(), getLastDate());
    }
  }

  public void removeStyleFromDate(String styleName, JustDate date) {
    dateStyler.setStyleName(date, styleName, false);
    if (isDateVisible(date)) {
      getView().removeStyleFromDate(styleName, date);
    }
  }

  public void removeStyleFromDates(String styleName, Iterable<JustDate> dates) {
    for (JustDate date : dates) {
      removeStyleFromDate(styleName, date);
    }
  }

  public void setDate(JustDate newValue) {
    setDate(newValue, false);
  }

  public void setDate(JustDate newValue, boolean fireEvents) {
    Assert.notNull(newValue);

    if (!newValue.equals(value)) {
      setValue(newValue, fireEvents);
      setCurrentMonth(YearMonth.get(newValue));
    }
  }

  public void setFocus(boolean focus) {
    view.setFocus(focus);
  }

  public void setHighlightedDate(JustDate highlighted) {
    this.highlighted = highlighted;
    HighlightEvent.fire(this, highlighted);
  }

  public void setTransientEnabledOnDate(boolean enabled, JustDate date) {
    Assert.state(isDateVisible(date), "date must be visible");
    getView().setEnabledOnDate(enabled, date);
  }

  public void setTransientEnabledOnDates(boolean enabled, Iterable<JustDate> dates) {
    for (JustDate date : dates) {
      setTransientEnabledOnDate(enabled, date);
    }
  }

  public void setValue(JustDate newValue) {
    setValue(newValue, false);
  }

  public void setValue(JustDate newValue, boolean fireEvents) {
    if (!BeeUtils.equals(value, newValue)) {
      JustDate oldValue = value;
      if (oldValue != null) {
        removeStyleFromDate(getCssClasses().dayIsValue(), oldValue);
      }

      value = JustDate.copyOf(newValue);
      if (value != null) {
        addStyleToDate(getCssClasses().dayIsValue(), value);
      }
    }

    if (fireEvents) {
      ValueChangeEvent.fire(this, newValue);
    }
  }

  @Override
  protected void onLoad() {
    ShowRangeEvent.fire(this, getFirstDate(), getLastDate());
  }

  void setCurrentMonth(YearMonth ym) {
    getModel().setCurrentMonth(ym);
    refreshAll();
  }

  private void setup() {
    VerticalPanel panel = new VerticalPanel();
    initWidget(panel);

    setStyleName(panel.getElement(), getCssClasses().datePicker());

    panel.add(getMonthSelector());
    panel.add(getView());
  }
}
