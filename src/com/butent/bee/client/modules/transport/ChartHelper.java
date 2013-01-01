package com.butent.bee.client.modules.transport;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.datepicker.DatePicker;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.dialog.Popup.OutsideClick;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;

class ChartHelper {

  private static final BeeLogger logger = LogUtils.getLogger(ChartHelper.class);

  private static final String STYLE_PREFIX = "bee-tr-";

  private static final String STYLE_MOHTH_SEPARATOR = STYLE_PREFIX + "monthSeparator";
  private static final String STYLE_DAY_SEPARATOR = STYLE_PREFIX + "daySeparator";
  private static final String STYLE_RIGHT_SEPARATOR = STYLE_PREFIX + "rightSeparator";

  private static final String STYLE_DAY_LABEL = STYLE_PREFIX + "dayLabel";

  private static final String STYLE_PAST = STYLE_PREFIX + "past";
  private static final String STYLE_TODAY = STYLE_PREFIX + "today";
  private static final String STYLE_WEEKDAY = STYLE_PREFIX + "weekday";
  private static final String STYLE_WEEKEND = STYLE_PREFIX + "weekend";

  private static final String STYLE_V_R_PREFIX = STYLE_PREFIX + "visibleRange-";
  private static final String STYLE_VISIBLE_RANGE_PANEL = STYLE_V_R_PREFIX + "panel";
  private static final String STYLE_VISIBLE_RANGE_START = STYLE_V_R_PREFIX + "start";
  private static final String STYLE_VISIBLE_RANGE_END = STYLE_V_R_PREFIX + "end";

  private static final String STYLE_M_R_PREFIX = STYLE_PREFIX + "maxRange-";
  private static final String STYLE_MAX_RANGE_PANEL = STYLE_M_R_PREFIX + "panel";
  private static final String STYLE_MAX_RANGE_START = STYLE_M_R_PREFIX + "start";
  private static final String STYLE_MAX_RANGE_END = STYLE_M_R_PREFIX + "end";

  private static final String STYLE_DAY_BACKGROUND = STYLE_PREFIX + "dayBackground";

  private static final int DAY_SEPARATOR_WIDTH = 1;
  private static final int MIN_DAY_WIDTH_FOR_SEPARATOR = 15;

  static void addColumnSeparator(HasWidgets panel, String styleName, int left, int height) {
    CustomDiv separator = new CustomDiv(styleName);

    if (left >= 0) {
      StyleUtils.setLeft(separator, left);
    }
    if (height > 0) {
      StyleUtils.setHeight(separator, height);
    }

    panel.add(separator);
  }

  static void addLegendWidget(HasWidgets panel, Widget widget, int left, int width,
      int firstRow, int lastRow, int rowHeight, int leftMargin, int topMargin) {

    if (left >= 0) {
      StyleUtils.setLeft(widget, left + leftMargin);
    }
    if (width > 0) {
      StyleUtils.setWidth(widget, width - leftMargin);
    }

    if (firstRow >= 0 && lastRow >= firstRow && rowHeight > 0) {
      StyleUtils.setTop(widget, firstRow * rowHeight + topMargin);
      StyleUtils.setHeight(widget, (lastRow - firstRow + 1) * rowHeight - topMargin);
    }

    panel.add(widget);
  }

  static void addRowSeparator(HasWidgets panel, String styleName, int top, int left, int width) {
    CustomDiv separator = new CustomDiv(styleName);

    if (top >= 0) {
      StyleUtils.setTop(separator, top);
    }
    if (left >= 0) {
      StyleUtils.setLeft(separator, left);
    }
    if (width > 0) {
      StyleUtils.setWidth(separator, width);
    }

    panel.add(separator);
  }

  static JustDate clamp(JustDate date, Range<JustDate> range) {
    return TimeUtils.clamp(date, range.lowerEndpoint(), range.upperEndpoint());
  }

  static int getColorIndex(Long id, int count) {
    if (id == null || count <= 0) {
      return BeeConst.UNDEF;
    } else {
      return Math.abs(Codec.crc32(BeeUtils.toString(id)).hashCode()) % count;
    }
  }

  static JustDate getDate(JustDate start, int position, double daySize) {
    return TimeUtils.nextDay(start, BeeUtils.round(position / daySize));
  }

  static String getDateTitle(JustDate date) {
    if (date == null) {
      return null;
    } else {
      return BeeUtils.buildLines(date.toString(), Format.renderDayOfWeek(date));
    }
  }

  static Range<JustDate> getDefaultRange(Range<JustDate> span, int chartWidth, int dayWidth) {
    int spanSize = getSize(span);
    if (spanSize <= 1) {
      return span;
    }

    int days = BeeUtils.clamp(chartWidth / dayWidth, 2, spanSize);

    JustDate start = span.lowerEndpoint();
    JustDate end = TimeUtils.nextDay(start, days - 1);

    return Range.closed(start, end);
  }

  static int getPixels(BeeRowSet settings, String colName, int def) {
    if (DataUtils.isEmpty(settings)) {
      return def;
    }

    int index = settings.getColumnIndex(colName);
    if (BeeConst.isUndef(index)) {
      logger.severe(settings.getViewName(), colName, "column not found");
      return def;
    }

    Integer value = settings.getInteger(0, index);
    return BeeUtils.isPositive(value) ? value : def;
  }

  static int getPixels(BeeRowSet settings, String colName, int def, int max) {
    return Math.min(getPixels(settings, colName, def), max);
  }
  
  static int getPosition(JustDate start, JustDate date, double daySize) {
    return BeeUtils.round(TimeUtils.dayDiff(start, date) * daySize);
  }

  static String getRangeLabel(JustDate start, JustDate end) {
    if (start == null && end == null) {
      return BeeConst.STRING_EMPTY;
    } else if (start == null) {
      return end.toString();
    } else if (end == null || start.equals(end)) {
      return start.toString();
    } else {
      return BeeUtils.joinWords(start, end);
    }
  }

  static int getSize(Range<JustDate> range) {
    if (range == null) {
      return BeeConst.UNDEF;
    }

    int start = range.lowerEndpoint().getDays();
    if (range.lowerBoundType() == BoundType.OPEN) {
      start--;
    }

    int end = range.upperEndpoint().getDays();
    if (range.lowerBoundType() == BoundType.CLOSED) {
      end++;
    }

    return end - start;
  }

  static Range<JustDate> getSpan(Collection<? extends HasDateRange> items) {
    JustDate min = null;
    JustDate max = null;

    for (HasDateRange item : items) {
      if (min == null || TimeUtils.isLess(item.getRange().lowerEndpoint(), min)) {
        min = item.getRange().lowerEndpoint();
      }

      if (max == null || TimeUtils.isMore(item.getRange().upperEndpoint(), max)) {
        max = item.getRange().upperEndpoint();
      }
    }

    if (min == null || max == null) {
      return null;
    } else {
      return Range.closed(min, max);
    }
  }

  static boolean intersects(Collection<? extends HasDateRange> items, Range<JustDate> range) {
    if (items == null || range == null) {
      return false;
    }

    for (HasDateRange item : items) {
      if (BeeUtils.intersects(item.getRange(), range)) {
        return true;
      }
    }
    return false;
  }
  
  static Range<JustDate> normalize(Range<JustDate> range) {
    if (range.lowerBoundType() == BoundType.CLOSED && range.upperBoundType() == BoundType.CLOSED) {
      return range;
    }

    int start = range.lowerEndpoint().getDays();
    if (range.lowerBoundType() == BoundType.OPEN) {
      start--;
    }

    int end = range.upperEndpoint().getDays();
    if (range.lowerBoundType() == BoundType.OPEN) {
      end--;
    }

    return Range.closed(new JustDate(start), new JustDate(end));
  }

  static void renderDayColumns(HasWidgets panel, Range<JustDate> range, int startLeft,
      int dayWidth, int height, boolean firstSeparator, boolean lastSeparator) {

    JustDate date = JustDate.copyOf(range.lowerEndpoint());
    int count = getSize(range);

    int left = startLeft;
    
    int separatorWidth;
    boolean hasSeparator;
    
    for (int i = 0; i < count; i++) {
      if ((i > 0 || firstSeparator) && renderDaySeparator(date, dayWidth)) {
        Widget separator = createDaySeparator(date);
        StyleUtils.setLeft(separator, left);
        if (height > 0) {
          StyleUtils.setHeight(separator, height);
        }
        panel.add(separator);
        
        separatorWidth = DAY_SEPARATOR_WIDTH;
        hasSeparator = true;

      } else {
        separatorWidth = (i > 0 || firstSeparator) ? 0 : DAY_SEPARATOR_WIDTH;
        hasSeparator = false;
      }

      Widget background = createDayBackground(date);
      StyleUtils.setLeft(background, left + separatorWidth);
      StyleUtils.setWidth(background, dayWidth);
      if (height > 0) {
        StyleUtils.setHeight(background, height);
      }
      
      if (!hasSeparator) {
        background.setTitle(getDateTitle(date));
      }
      
      panel.add(background);

      TimeUtils.addDay(date, 1);
      left += dayWidth;
    }
    
    if (count > 0 && lastSeparator) {
      CustomDiv separator = new CustomDiv(STYLE_RIGHT_SEPARATOR);
      StyleUtils.setLeft(separator, startLeft + count * dayWidth);
      if (height > 0) {
        StyleUtils.setHeight(separator, height);
      }
      panel.add(separator);
    }
  }

  static void renderDayLabels(HasWidgets panel, Range<JustDate> range, int startLeft,
      int dayWidth) {

    JustDate date = JustDate.copyOf(range.lowerEndpoint());
    int count = getSize(range);

    int left = startLeft;
    int separatorWidth;

    for (int i = 0; i < count; i++) {
      if (renderDaySeparator(date, dayWidth)) {
        Widget separator = createDaySeparator(date);
        StyleUtils.setLeft(separator, left);
        panel.add(separator);
        
        separatorWidth = DAY_SEPARATOR_WIDTH;
      } else {
        separatorWidth = 0;
      }

      Widget label = createDayLabel(date);
      StyleUtils.setLeft(label, left + separatorWidth);
      StyleUtils.setWidth(label, dayWidth);

      label.setTitle(getDateTitle(date));
      
      panel.add(label);

      TimeUtils.addDay(date, 1);
      left += dayWidth;
    }
  }

  static void renderMaxRange(Range<JustDate> range, HasWidgets container, int width) {
    if (range == null || range.isEmpty()) {
      return;
    }

    Flow panel = new Flow();
    panel.addStyleName(STYLE_MAX_RANGE_PANEL);
    if (width > 0) {
      StyleUtils.setWidth(panel, width);
    }

    BeeLabel startWidget = new BeeLabel(range.lowerEndpoint().toString());
    startWidget.addStyleName(STYLE_MAX_RANGE_START);
    panel.add(startWidget);

    if (getSize(range) > 1) {
      BeeLabel endWidget = new BeeLabel(range.upperEndpoint().toString());
      endWidget.addStyleName(STYLE_MAX_RANGE_END);
      panel.add(endWidget);
    }

    container.add(panel);
  }

  static void renderVisibleRange(HasVisibleRange owner, HasWidgets container, int width) {
    Range<JustDate> range = owner.getVisibleRange();
    if (range == null || range.isEmpty()) {
      return;
    }

    Flow panel = new Flow();
    panel.addStyleName(STYLE_VISIBLE_RANGE_PANEL);
    if (width > 0) {
      StyleUtils.setWidth(panel, width);
    }

    BeeLabel startWidget = new BeeLabel(range.lowerEndpoint().toString());
    startWidget.addStyleName(STYLE_VISIBLE_RANGE_START);
    addVisibleRangeDatePicker(owner, startWidget, true);
    panel.add(startWidget);

    if (getSize(range) > 1) {
      BeeLabel endWidget = new BeeLabel(range.upperEndpoint().toString());
      endWidget.addStyleName(STYLE_VISIBLE_RANGE_END);
      addVisibleRangeDatePicker(owner, endWidget, false);
      panel.add(endWidget);
    }

    container.add(panel);
  }

  private static void addDayStyle(Widget widget, JustDate date) {
    if (TimeUtils.isMore(TimeUtils.today(), date)) {
      widget.addStyleName(STYLE_PAST);
    } else if (TimeUtils.today().equals(date)) {
      widget.addStyleName(STYLE_TODAY);
    } else if (TimeUtils.isWeekend(date)) {
      widget.addStyleName(STYLE_WEEKEND);
    } else {
      widget.addStyleName(STYLE_WEEKDAY);
    }
  }

  private static void addVisibleRangeDatePicker(final HasVisibleRange owner,
      final HasClickHandlers widget, final boolean isStart) {

    widget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {

        final JustDate startBound = owner.getMaxRange().lowerEndpoint();
        final JustDate endBound = owner.getMaxRange().upperEndpoint();

        JustDate oldStart = owner.getVisibleRange().lowerEndpoint();
        JustDate oldEnd = owner.getVisibleRange().upperEndpoint();

        final JustDate oldValue = isStart ? oldStart : oldEnd;

        final Popup popup = new Popup(OutsideClick.CLOSE);
        DatePicker datePicker = new DatePicker(oldValue, startBound, endBound);

        datePicker.addValueChangeHandler(new ValueChangeHandler<JustDate>() {
          @Override
          public void onValueChange(ValueChangeEvent<JustDate> vce) {
            popup.hide();
            JustDate newValue = vce.getValue();

            if (newValue != null && !newValue.equals(oldValue)
                && owner.getMaxRange().contains(newValue)) {

              int size = getSize(owner.getVisibleRange());
              JustDate newStart;
              JustDate newEnd;

              if (isStart) {
                newStart = newValue;
                newEnd = TimeUtils.min(TimeUtils.nextDay(newValue, size), endBound);
              } else {
                newStart = TimeUtils.max(TimeUtils.nextDay(newValue, -size), startBound);
                newEnd = newValue;
              }

              owner.setVisibleRange(newStart, newEnd);
            }
          }
        });

        popup.setWidget(datePicker);
        popup.showRelativeTo(EventUtils.getTargetElement(event.getNativeEvent().getEventTarget()));
      }
    });
  }

  private static Widget createDayBackground(JustDate date) {
    CustomDiv widget = new CustomDiv();

    widget.addStyleName(STYLE_DAY_BACKGROUND);
    addDayStyle(widget, date);

    return widget;
  }

  private static Widget createDayLabel(JustDate date) {
    BeeLabel widget = new BeeLabel(BeeUtils.toString(date.getDom()));

    widget.addStyleName(STYLE_DAY_LABEL);
    addDayStyle(widget, date);

    return widget;
  }

  private static Widget createDaySeparator(JustDate date) {
    CustomDiv widget = new CustomDiv();

    widget.addStyleName((date.getDom() == 1) ? STYLE_MOHTH_SEPARATOR : STYLE_DAY_SEPARATOR);

    return widget;
  }

  private static boolean renderDaySeparator(JustDate date, int dayWidth) {
    return date.getDom() == 1
        || TimeUtils.isMore(date, TimeUtils.today()) && dayWidth >= MIN_DAY_WIDTH_FOR_SEPARATOR;
  }

  private ChartHelper() {
  }
}
