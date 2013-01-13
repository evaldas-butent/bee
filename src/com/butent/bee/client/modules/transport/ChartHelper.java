package com.butent.bee.client.modules.transport;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
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
import com.butent.bee.client.dom.Edges;
import com.butent.bee.client.dom.Rectangle;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.Html;
import com.butent.bee.client.widget.Mover;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.HasDateRange;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;

class ChartHelper {

  static final int DEFAULT_MOVER_WIDTH = 3;
  static final int DEFAULT_MOVER_HEIGHT = 3;

  static final int DAY_SEPARATOR_WIDTH = 1;
  static final int ROW_SEPARATOR_HEIGHT = 1;

  private static final BeeLogger logger = LogUtils.getLogger(ChartHelper.class);

  private static final String STYLE_PREFIX = "bee-tr-chart-";

  private static final String STYLE_MOHTH_SEPARATOR = STYLE_PREFIX + "monthSeparator";
  private static final String STYLE_DAY_SEPARATOR = STYLE_PREFIX + "daySeparator";
  private static final String STYLE_RIGHT_SEPARATOR = STYLE_PREFIX + "rightSeparator";

  private static final String STYLE_DAY_LABEL = STYLE_PREFIX + "dayLabel";
  private static final String STYLE_DAY_NARROW = STYLE_PREFIX + "dayNarrow";
  private static final String STYLE_DAY_PICTURE = STYLE_PREFIX + "dayPicture";
  private static final String STYLE_DAY_TENS = STYLE_PREFIX + "dayTens";
  private static final String STYLE_DAY_ONES = STYLE_PREFIX + "dayOnes";

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

  private static final String STYLE_CONTENT_ROW_SEPARATOR = STYLE_PREFIX + "row-sep";
  private static final String STYLE_CONTENT_BOTTOM_SEPARATOR = STYLE_PREFIX + "bottom-sep";

  private static final String STYLE_HORIZONTAL_MOVER = STYLE_PREFIX + "horizontalMover";
  private static final String STYLE_VERTICAL_MOVER = STYLE_PREFIX + "verticalMover";

  private static final int MIN_DAY_WIDTH_FOR_SEPARATOR = 10;

  static void addBottomSeparator(HasWidgets panel, int top, int left, int width) {
    addRowSeparator(panel, STYLE_CONTENT_BOTTOM_SEPARATOR, top, left, width);
  }

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

  static void addHorizontalMover(HasWidgets panel, int right, int height) {
    Rectangle rectangle = new Rectangle();

    rectangle.setLeft(right - DEFAULT_MOVER_WIDTH);
    if (height > 0) {
      rectangle.setHeight(height);
    }

    addHorizontalMover(panel, rectangle);
  }

  static void addHorizontalMover(HasWidgets panel, Rectangle rectangle) {
    Mover mover = new Mover(Orientation.HORIZONTAL, STYLE_HORIZONTAL_MOVER);

    if (rectangle != null) {
      rectangle.applyTo(mover);
    }

    panel.add(mover);
  }

  static void addRowSeparator(HasWidgets panel, int top, int left, int width) {
    addRowSeparator(panel, STYLE_CONTENT_ROW_SEPARATOR, top, left, width);
  }

  static void addRowSeparator(HasWidgets panel, String styleName, int top, int left, int width) {
    CustomDiv separator = new CustomDiv(styleName);

    if (top >= 0) {
      StyleUtils.setTop(separator, top - ROW_SEPARATOR_HEIGHT);
    }
    if (left >= 0) {
      StyleUtils.setLeft(separator, left);
    }
    if (width > 0) {
      StyleUtils.setWidth(separator, width);
    }

    panel.add(separator);
  }

  static void apply(Widget widget, Rectangle rectangle, Edges margins) {
    Style style = widget.getElement().getStyle();

    if (rectangle.getLeftValue() != null) {
      int left = BeeUtils.toInt(rectangle.getLeftValue());
      if (margins.getLeftValue() != null) {
        left += BeeUtils.toInt(margins.getLeftValue());
      }

      StyleUtils.setLeft(style, left);
    }

    if (rectangle.getTopValue() != null) {
      int top = BeeUtils.toInt(rectangle.getTopValue());
      if (margins.getTopValue() != null) {
        top += BeeUtils.toInt(margins.getTopValue());
      }

      StyleUtils.setTop(style, top);
    }

    if (rectangle.getWidthValue() != null) {
      int width = BeeUtils.toInt(rectangle.getWidthValue());

      if (margins.getLeftValue() != null) {
        width -= BeeUtils.toInt(margins.getLeftValue());
      }
      if (margins.getRightValue() != null) {
        width -= BeeUtils.toInt(margins.getRightValue());
      }

      if (width > 0) {
        StyleUtils.setWidth(style, width);
      }
    }

    if (rectangle.getHeightValue() != null) {
      int height = BeeUtils.toInt(rectangle.getHeightValue());

      if (margins.getTopValue() != null) {
        height -= BeeUtils.toInt(margins.getTopValue());
      }
      if (margins.getBottomValue() != null) {
        height -= BeeUtils.toInt(margins.getBottomValue());
      }

      if (height > 0) {
        StyleUtils.setHeight(style, height);
      }
    }
  }

  static String buildTitle(Object... labelsAndValues) {
    Assert.notNull(labelsAndValues);
    int c = labelsAndValues.length;
    Assert.parameterCount(c, 2);
    Assert.isEven(c);

    String valueSeparator = BeeConst.STRING_COLON + BeeConst.STRING_SPACE;
    char lineSeparator = BeeConst.CHAR_EOL;

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < c - 1; i += 2) {
      Object label = labelsAndValues[i];
      Object value = labelsAndValues[i + 1];

      if (label instanceof String && value != null) {
        if (sb.length() > 0) {
          sb.append(lineSeparator);
        }
        sb.append(BeeUtils.join(valueSeparator, label, value));
      }
    }
    return sb.toString();
  }

  static JustDate clamp(JustDate date, Range<JustDate> range) {
    return TimeUtils.clamp(date, range.lowerEndpoint(), range.upperEndpoint());
  }

  static Mover createVerticalMover() {
    return new Mover(Orientation.VERTICAL, STYLE_VERTICAL_MOVER);
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

  static Rectangle getLegendRectangle(int left, int width, int firstRow, int lastRow,
      int rowHeight) {

    Rectangle rectangle = new Rectangle();

    if (left >= 0) {
      rectangle.setLeft(left);
    }
    if (width > 0) {
      rectangle.setWidth(width);
    }

    if (firstRow >= 0 && lastRow >= firstRow && rowHeight > 0) {
      rectangle.setTop(firstRow * rowHeight);
      rectangle.setHeight((lastRow - firstRow + 1) * rowHeight);
    }

    return rectangle;
  }

  static Double getOpacity(BeeRowSet settings, String colName) {
    if (DataUtils.isEmpty(settings)) {
      return null;
    }

    int index = settings.getColumnIndex(colName);
    if (BeeConst.isUndef(index)) {
      logger.severe(settings.getViewName(), colName, "column not found");
      return null;
    }

    Integer value = settings.getInteger(0, index);
    return (BeeUtils.isPositive(value) && value < 100) ? value / 100.0 : null;
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

  static int getPixels(BeeRowSet settings, String colName, int def, int min, int max) {
    return BeeUtils.clamp(getPixels(settings, colName, def), min, max);
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
      int dayWidth, int height) {

    JustDate date = JustDate.copyOf(range.lowerEndpoint());
    int count = getSize(range);

    int left = startLeft;
    int separatorWidth;

    JustDate next = TimeUtils.nextDay(date);

    for (int i = 0; i < count; i++) {
      separatorWidth = getDaySeparatorWidth(next, dayWidth, i, count);

      Widget background = createDayBackground(date);
      StyleUtils.setLeft(background, left);
      StyleUtils.setWidth(background, dayWidth - separatorWidth);
      if (height > 0) {
        StyleUtils.setHeight(background, height);
      }

      panel.add(background);

      if (separatorWidth > 0) {
        Widget separator = createDaySeparator(next, i, count);
        StyleUtils.setLeft(separator, left + dayWidth - separatorWidth);
        if (height > 0) {
          StyleUtils.setHeight(separator, height);
        }

        panel.add(separator);

      } else {
        background.setTitle(getDateTitle(date));
      }

      TimeUtils.addDay(date, 1);
      TimeUtils.addDay(next, 1);

      left += dayWidth;
    }
  }

  static void renderDayLabels(HasWidgets panel, Range<JustDate> range, int startLeft,
      int dayWidth, int height) {

    JustDate date = JustDate.copyOf(range.lowerEndpoint());
    int count = getSize(range);

    int left = startLeft;
    int separatorWidth;

    JustDate next = TimeUtils.nextDay(date);

    for (int i = 0; i < count; i++) {
      separatorWidth = getDaySeparatorWidth(next, dayWidth, i, count);

      Widget label = createDayLabel(date, dayWidth - separatorWidth, height);
      StyleUtils.setLeft(label, left);
      StyleUtils.setWidth(label, dayWidth - separatorWidth);

      label.setTitle(getDateTitle(date));
      panel.add(label);

      if (separatorWidth > 0) {
        Widget separator = createDaySeparator(next, i, count);
        StyleUtils.setLeft(separator, left + dayWidth - separatorWidth);
        panel.add(separator);
      }

      TimeUtils.addDay(date, 1);
      TimeUtils.addDay(next, 1);

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
            popup.close();
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

  private static Widget createDayLabel(JustDate date, int width, int height) {
    Html widget;
    int day = date.getDom();

    if (width >= 15 && height >= 10 || width >= 8 && height >= 24) {
      widget = new Html(BeeUtils.toString(day));

      widget.addStyleName(STYLE_DAY_LABEL);
      if (width < 15) {
        widget.addStyleName(STYLE_DAY_NARROW);
      }
      
      addDayStyle(widget, date);

    } else {
      int tens = day / 10;
      int ones = day % 10;

      int tensWidth = 1;
      int tensHeight = 1;
      int onesWidth = 1;
      int onesHeight = 1;

      int tensCount = 1;
      int onesCount = 1;

      if (width >= 2 && height >= 9) {
        tensWidth = width / 2;
        tensHeight = height / 3 * tens;

        onesWidth = width / 2;
        onesHeight = height / 9 * ones;

      } else if (width >= 9 && height >= 2) {
        tensWidth = width / 3 * tens;
        tensHeight = height / 2;

        onesWidth = width / 9 * ones;
        onesHeight = height / 2;

      } else if (width >= 12) {
        tensWidth = (width - 9) / 3 * tens;
        onesWidth = ones;

      } else if (height >= 12) {
        tensHeight = (height - 9) / 3 * tens;
        onesHeight = ones;

      } else if (width >= 6 && height >= 2) {
        tensWidth = (width * height - 9) / 3;
        onesCount = ones;

      } else if (width >= 2 && height >= 6) {
        tensHeight = (width * height - 9) / 3;
        onesCount = ones;

      } else {
        tensCount = tens;
        onesCount = ones;
      }

      Element root = Document.get().createDivElement();
      String styleSuffix = BeeConst.STRING_MINUS + BeeUtils.toString(tens * 10);

      if (tens > 0) {
        for (int i = 0; i < tensCount; i++) {
          Element tensElement = Document.get().createDivElement();
          tensElement.addClassName(STYLE_DAY_TENS);
          tensElement.addClassName(STYLE_DAY_TENS + styleSuffix);
          StyleUtils.setSize(tensElement, tensWidth, tensHeight);

          root.appendChild(tensElement);
        }
      }

      if (ones > 0) {
        for (int i = 0; i < onesCount; i++) {
          Element onesElement = Document.get().createDivElement();
          onesElement.addClassName(STYLE_DAY_ONES);
          onesElement.addClassName(STYLE_DAY_ONES + styleSuffix);
          StyleUtils.setSize(onesElement, onesWidth, onesHeight);

          root.appendChild(onesElement);
        }
      }

      widget = new Html(root);
      widget.addStyleName(STYLE_DAY_PICTURE);
      widget.addStyleName(STYLE_DAY_PICTURE + styleSuffix);
    }

    return widget;
  }

  private static Widget createDaySeparator(JustDate date, int index, int count) {
    String styleName;
    if (index == count - 1) {
      styleName = STYLE_RIGHT_SEPARATOR;
    } else if (date.getDom() == 1) {
      styleName = STYLE_MOHTH_SEPARATOR;
    } else {
      styleName = STYLE_DAY_SEPARATOR;
    }

    return new CustomDiv(styleName);
  }

  private static int getDaySeparatorWidth(JustDate date, int dayWidth, int index, int count) {
    if (DAY_SEPARATOR_WIDTH > 0
        && dayWidth > DAY_SEPARATOR_WIDTH * 2
        && (index == count - 1
            || date.getDom() == 1
            || TimeUtils.isMore(date, TimeUtils.today())
            && dayWidth >= MIN_DAY_WIDTH_FOR_SEPARATOR)) {
      return DAY_SEPARATOR_WIDTH;
    } else {
      return 0;
    }
  }

  private ChartHelper() {
  }
}
