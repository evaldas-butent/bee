package com.butent.bee.shared.time;

import com.google.common.base.Ascii;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.List;
import java.util.Set;

public final class CronExpression {

  public static class Builder {

    private static String append(String value, String input) {
      if (BeeUtils.isEmpty(value)) {
        return BeeUtils.isEmpty(input) ? null : normalize(input);
      } else if (BeeUtils.isEmpty(input)) {
        return value;
      } else {
        return value + String.valueOf(ITEM_SEPARATOR) + normalize(input);
      }
    }

    private static Item getWeekdayOrdinalItem(Field field, Token t1, Token t2) {
      Integer weekday;
      int ordinal;

      if (t1.isWeekdayOrdinal()) {
        weekday = Field.DAY_OF_WEEK.getValue(t2);
        ordinal = WEEKDAY_ORDINALS.indexOf(t1.value);
      } else {
        weekday = Field.DAY_OF_WEEK.getValue(t1);
        ordinal = WEEKDAY_ORDINALS.indexOf(t2.value);
      }

      if (field.supportsWeekdayOrdinals && weekday != null && ordinal >= 0) {
        return Item.weekdayOrdinal(weekday, ordinal);
      } else {
        return null;
      }
    }

    private static void itemFailure(Field field, Item item) {
      logger.warning(field.caption, "invalid item state", item);
    }

    private static String normalize(String input) {
      return input.trim().toUpperCase();
    }

    private static List<Item> parse(Field field, String input) {
      List<Item> items = Lists.newArrayList();
      if (BeeUtils.isEmpty(input)) {
        return items;
      }

      List<String> inputList = itemSplitter.splitToList(input);
      for (String s : inputList) {
        Item item = null;
        boolean ok = false;

        char firstChar = s.charAt(0);

        if (s.length() == 1) {
          switch (firstChar) {
            case CHAR_ALL:
            case CHAR_BLANK:
              ok = true;
              break;

            case CHAR_LAST:
              if (field.supportsLast) {
                item = Item.of(field.max);
              }
              break;

            default:
              if (BeeUtils.isDigit(firstChar) && field.accepts((int) firstChar)) {
                item = Item.of(firstChar);
              }
          }

        } else if (firstChar == STEP_SEPARATOR) {
          Integer increment = BeeUtils.toIntOrNull(s.substring(2));
          if (field.supportsBaseIncrement && BeeUtils.isPositive(increment) && increment > 1) {
            item = Item.baseIncrement(increment);
          }

        } else if (firstChar == BeeConst.CHAR_MINUS) {
          if (s.endsWith(String.valueOf(CHAR_WORKDAY)) && field.supportsWorkday) {
            Integer value = BeeUtils.toIntOrNull(s.substring(0, s.length() - 1));
            if (field.accepts(value)) {
              item = Item.nearestWorkday(value);
            }

          } else {
            Integer value = BeeUtils.toIntOrNull(s);
            if (field.accepts(value)) {
              item = Item.of(value);
            }
          }

        } else if (BeeUtils.isDigit(s)) {
          Integer value = BeeUtils.toIntOrNull(s);
          if (field.accepts(value)) {
            item = Item.of(value);
          }

        } else {
          List<Token> tokens = tokenize(s);

          Token t1 = tokens.get(0);
          Token t2;
          Token t3;

          switch (tokens.size()) {
            case 1:
              switch (t1.kind) {
                case ALPHA:
                case INT:
                  Integer value = field.getValue(t1);
                  if (value != null) {
                    item = Item.of(value);
                  }
                  break;

                case LAST:
                  if (field.supportsLast) {
                    item = Item.of(field.max);
                  }
                  break;

                case LAST_WORK:
                  if (field.supportsLast && field.supportsWorkday) {
                    item = Item.lastWorkday();
                  }
                  break;

                default:
              }
              break;

            case 2:
              t2 = tokens.get(1);

              if (t1.isWeekdayOrdinal() || t2.isWeekdayOrdinal()) {
                item = getWeekdayOrdinalItem(field, t1, t2);

              } else if (t1.isLast() || t2.isLast()) {
                Integer weekday = Field.DAY_OF_WEEK.getValue(t1.isLast() ? t2 : t1);

                if (weekday != null && (field.supportsWeekdayOrdinals
                    || field == Field.DAY_OF_WEEK && field.supportsLast)) {
                  item = Item.lastWeekday(weekday);
                }

              } else if (t1.isWorkday() || t2.isWorkday()) {
                Integer dom = Field.DAY_OF_MONTH.getValue(t1.isWorkday() ? t2 : t1);

                if (dom != null && field.supportsWorkday) {
                  item = Item.nearestWorkday(dom);
                }
              }

              break;

            case 3:
              t2 = tokens.get(1);
              t3 = tokens.get(2);

              if (t2.isRangeSeparator()) {
                Integer lower = field.getValue(t1);
                Integer upper = field.getValue(t3);

                if (lower != null && upper != null) {
                  if (lower <= upper) {
                    item = Item.range(lower, upper);
                  } else if (field.supportsOverflowingRanges) {
                    items.add(Item.range(lower, field.max));
                    item = Item.range(field.min, upper);
                  }
                }

              } else if (t2.isStepSeparator() && t3.isInt()) {
                Integer step = BeeUtils.toIntOrNull(t3.value);

                if (BeeUtils.isPositive(step)) {
                  if (t1.isAll()) {
                    item = Item.incrementalRange(field.min, field.max, step);
                  } else {
                    Integer value = field.getValue(t1);

                    if (value != null) {
                      if (field == Field.DAY_OF_WEEK) {
                        item = Item.weekdayBaseIncrement(value, step);
                      } else {
                        item = Item.of(value);
                      }
                    }
                  }
                }

              } else if (t2.isOrdinalSeparator()
                  && (t1.isWeekdayOrdinal() || t3.isWeekdayOrdinal())) {
                item = getWeekdayOrdinalItem(field, t1, t3);
              }

              break;

            case 5:
              if (tokens.get(1).isRangeSeparator() && tokens.get(3).isStepSeparator()
                  && tokens.get(4).isInt()) {

                Integer lower = field.getValue(t1);
                Integer upper = field.getValue(tokens.get(2));
                Integer step = BeeUtils.toIntOrNull(tokens.get(4).value);

                if (lower != null && upper != null && BeeUtils.isPositive(step)) {
                  if (lower <= upper) {
                    item = Item.incrementalRange(lower, upper, step);

                  } else if (field.supportsOverflowingRanges) {
                    item = Item.incrementalRange(lower, field.max, step);

                    int overflow = (field.max - lower + 1) % step;
                    if (overflow > 0) {
                      overflow = step - overflow;
                    }
                    overflow += field.min;

                    if (overflow <= upper) {
                      items.add(item);
                      item = Item.incrementalRange(overflow, upper, step);
                    }
                  }
                }
              }
              break;
          }
        }

        if (item != null) {
          items.add(item);
        } else if (!ok) {
          parseFailure(field, input, s);
        }
      }

      return items;
    }

    private static void parseFailure(Field field, String input, String item) {
      logger.warning(field.caption, "cannot parse", input,
          input.equals(item) ? null : BeeUtils.bracket(item));
    }

    private JustDate base;
    private String seconds;

    private String minutes;
    private String hours;
    private String dayOfMonth;

    private String month;

    private String dayOfWeek;

    private String year;
    private WorkdayTransition workdayTransition;

    private final Set<DateRange> exclude = Sets.newHashSet();

    private final Set<DateRange> include = Sets.newHashSet();

    public Builder(JustDate base) {
      this.base = base;
    }

    public CronExpression build() {
      CronExpression result = new CronExpression(base, BeeUtils.nvl(workdayTransition,
          WorkdayTransition.DEFAULT));

      List<Item> items = parse(Field.SECONDS, seconds);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addSeconds(item.getSimpleValues());
        } else {
          itemFailure(Field.SECONDS, item);
        }
      }

      items = parse(Field.MINUTES, minutes);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addMinutes(item.getSimpleValues());
        } else {
          itemFailure(Field.MINUTES, item);
        }
      }

      items = parse(Field.HOURS, hours);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addHours(item.getSimpleValues());
        } else {
          itemFailure(Field.HOURS, item);
        }
      }

      items = parse(Field.DAY_OF_MONTH, dayOfMonth);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addDaysOfMonth(item.getSimpleValues());

        } else if (item.isBaseIncrement()) {
          result.addDayStep(item.step);

        } else if (item.isLastWeekday()) {
          result.addLastWeekday(item.lower);

        } else if (item.isLastWorkday()) {
          result.setLastWorkday(true);

        } else if (item.isNearestWorkday()) {
          result.addNearestWorkday(item.lower);

        } else if (item.isWeekdayOrdinal()) {
          result.addWeekdayOrdinal(item.lower, item.weekdayOrdinal);

        } else {
          itemFailure(Field.DAY_OF_MONTH, item);
        }
      }

      items = parse(Field.MONTH, month);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addMonths(item.getSimpleValues());

        } else if (item.isBaseIncrement()) {
          result.addMonthStep(item.step);

        } else {
          itemFailure(Field.MONTH, item);
        }
      }

      items = parse(Field.DAY_OF_WEEK, dayOfWeek);
      for (Item item : items) {
        if (item.lower != null && item.step != null && item.step > 1) {
          int bound = BeeUtils.nvl(item.upper, item.lower); 
          for (int weekday = item.lower; weekday <= bound; weekday++) {
            result.addWeekStep(weekday, item.step);
          }
          
        } else if (item.isSimple()) {
          result.addDaysOfWeek(item.getSimpleValues());

        } else if (item.isLastWeekday()) {
          result.addLastWeekday(item.lower);

        } else if (item.isWeekdayOrdinal()) {
          result.addWeekdayOrdinal(item.lower, item.weekdayOrdinal);

        } else {
          itemFailure(Field.DAY_OF_WEEK, item);
        }
      }

      items = parse(Field.YEAR, year);
      for (Item item : items) {
        if (item.isSimple()) {
          result.addYears(item.getSimpleValues());
        } else {
          itemFailure(Field.YEAR, item);
        }
      }

      if (!exclude.isEmpty()) {
        result.setExclude(exclude);
      }
      if (!include.isEmpty()) {
        result.setInclude(include);
      }

      return result;
    }

    public Builder dayOfMonth(String input) {
      this.dayOfMonth = append(dayOfMonth, input);
      return this;
    }

    public Builder dayOfWeek(String input) {
      this.dayOfWeek = append(dayOfWeek, input);
      return this;
    }

    public Builder exclude(DateRange range) {
      if (range != null) {
        exclude.add(range);
      }
      return this;
    }

    public Builder exclude(JustDate date) {
      if (date != null) {
        exclude.add(DateRange.day(date));
      }
      return this;
    }

    public Builder hours(String input) {
      this.hours = append(hours, input);
      return this;
    }

    public Builder include(DateRange range) {
      if (range != null) {
        include.add(range);
      }
      return this;
    }

    public Builder include(JustDate date) {
      if (date != null) {
        include.add(DateRange.day(date));
      }
      return this;
    }

    public Builder minutes(String input) {
      this.minutes = append(minutes, input);
      return this;
    }

    public Builder month(String input) {
      this.month = append(month, input);
      return this;
    }

    public Builder seconds(String input) {
      this.seconds = append(seconds, input);
      return this;
    }

    public Builder workdayTransition(WorkdayTransition wt) {
      this.workdayTransition = wt;
      return this;
    }

    public Builder year(String input) {
      this.year = append(year, input);
      return this;
    }
  }

  public enum Field implements HasCaption {
    SECONDS("seconds", 0, 59, true, true, true),
    MINUTES("minutes", 0, 59, true, true, true),
    HOURS("hours", 0, 23, true, true, true),
    DAY_OF_MONTH("day of month", 1, 31, true, true, true, true, true, true),
    MONTH("month", 1, 12, true, true, true, false, false, true),
    DAY_OF_WEEK("day of weak", 1, 7, true, false, false, false, true, true),
    YEAR("year", MIN_YEAR, MAX_YEAR, false, false, false);

    private final String caption;

    private final int min;
    private final int max;

    private final boolean supportsLast;
    private final boolean supportsNegativeValues;
    private final boolean supportsOverflowingRanges;

    private final boolean supportsWorkday;
    private final boolean supportsWeekdayOrdinals;
    private final boolean supportsBaseIncrement;

    private Field(String caption, int min, int max, boolean supportsLast,
        boolean supportsNegativeValues, boolean supportsOverflowingRanges) {
      this(caption, min, max, supportsLast, supportsNegativeValues, supportsOverflowingRanges,
          false, false, false);
    }

    private Field(String caption, int min, int max,
        boolean supportsLast, boolean supportsNegativeValues, boolean supportsOverflowingRanges,
        boolean supportsWorkday, boolean supportsWeekdayOrdinals, boolean supportsBaseIncrement) {
      this.caption = caption;
      this.min = min;
      this.max = max;

      this.supportsLast = supportsLast;
      this.supportsNegativeValues = supportsNegativeValues;
      this.supportsOverflowingRanges = supportsOverflowingRanges;

      this.supportsWorkday = supportsWorkday;
      this.supportsWeekdayOrdinals = supportsWeekdayOrdinals;
      this.supportsBaseIncrement = supportsBaseIncrement;
    }

    @Override
    public String getCaption() {
      return caption;
    }

    private boolean accepts(Integer value) {
      if (value == null) {
        return false;

      } else if (value >= 0) {
        return value >= min && value <= max;

      } else if (supportsNegativeValues) {
        return value >= -max && value <= -min;

      } else {
        return false;
      }
    }

    private Integer getValue(Token token) {
      if (token.isInt()) {
        Integer value = BeeUtils.toIntOrNull(token.value);
        return accepts(value) ? value : null;

      } else if (token.isAlpha()) {
        if (this == MONTH) {
          return parseMonth(token.value);
        } else if (this == DAY_OF_WEEK) {
          return parseWeekday(token.value);
        } else {
          return null;
        }

      } else {
        return null;
      }
    }
  }

  public enum WorkdayTransition {
    NONE, NEAREST, FORWARD, BACKWARD;

    private static final WorkdayTransition DEFAULT = NEAREST;
  }

  private static final class Item {

    private static Item baseIncrement(int increment) {
      return new Item(null, null, increment);
    }

    private static Item incrementalRange(int lower, int upper, int step) {
      if (step <= 1) {
        return range(lower, upper);
      } else if (step > upper - lower) {
        return of(lower);
      } else {
        return new Item(lower, upper, step);
      }
    }

    private static Item lastWeekday(int weekday) {
      return new Item(weekday, null, null, null, true, false);
    }

    private static Item lastWorkday() {
      return new Item(null, null, null, null, true, true);
    }

    private static Item nearestWorkday(int value) {
      return new Item(value, null, null, null, false, true);
    }

    private static Item of(int value) {
      return new Item(value, null);
    }

    private static Item range(int lower, int upper) {
      if (lower == upper) {
        return of(lower);
      } else {
        return new Item(lower, upper);
      }
    }

    private static Item weekdayBaseIncrement(int weekday, int increment) {
      return new Item(weekday, null, increment);
    }

    private static Item weekdayOrdinal(int weekday, int ordinal) {
      return new Item(weekday, null, null, ordinal);
    }

    private final Integer lower;
    private final Integer upper;

    private final Integer step;

    private final Integer weekdayOrdinal;

    private final boolean last;
    private final boolean workday;

    private Item(Integer lower, Integer upper) {
      this(lower, upper, null);
    }

    private Item(Integer lower, Integer upper, Integer step) {
      this(lower, upper, step, null);
    }

    private Item(Integer lower, Integer upper, Integer step, Integer weekdayOrdinal) {
      this(lower, upper, step, weekdayOrdinal, false, false);
    }

    private Item(Integer lower, Integer upper, Integer step, Integer weekdayOrdinal,
        boolean last, boolean workday) {

      this.lower = lower;
      this.upper = upper;

      this.step = step;
      this.weekdayOrdinal = weekdayOrdinal;

      this.last = last;
      this.workday = workday;
    }

    @Override
    public String toString() {
      return BeeUtils.joinOptions("lower", String.valueOf(lower),
          "upper", String.valueOf(upper),
          "step", String.valueOf(step),
          "weekdayOrdinal", String.valueOf(weekdayOrdinal),
          "last", String.valueOf(last),
          "workday", String.valueOf(workday));
    }

    private Set<Integer> getSimpleValues() {
      Set<Integer> values = Sets.newHashSet();

      if (isSimple()) {
        if (upper == null) {
          values.add(lower);
        } else {
          int incr = BeeUtils.isPositive(step) ? step : 1;
          for (int v = lower; v <= upper; v += incr) {
            values.add(lower);
          }
        }
      }

      return values;
    }

    private boolean isBaseIncrement() {
      return lower == null && step != null;
    }

    private boolean isLastWeekday() {
      return lower != null && last;
    }

    private boolean isLastWorkday() {
      return last && workday;
    }

    private boolean isNearestWorkday() {
      return lower != null && workday;
    }

    private boolean isSimple() {
      return lower != null && weekdayOrdinal == null && !last && !workday;
    }

    private boolean isWeekdayOrdinal() {
      return lower != null && weekdayOrdinal != null;
    }
  }

  private static final class Token {
    private enum Kind {
      INT, ALPHA, WEEKDAY_ORD, ALL, LAST, WORK, LAST_WORK,
      RANGE_SEP, STEP_SEP, ORDINAL_SEP, UNKNOWN
    }

    private final Kind kind;
    private final String value;

    private Token(Kind kind) {
      this(kind, null);
    }

    private Token(Kind kind, String value) {
      this.kind = kind;
      this.value = value;
    }

    private boolean isAll() {
      return kind == Kind.ALL;
    }

    private boolean isAlpha() {
      return kind == Kind.ALPHA;
    }

    private boolean isInt() {
      return kind == Kind.INT;
    }

    private boolean isLast() {
      return kind == Kind.LAST;
    }

    private boolean isOrdinalSeparator() {
      return kind == Kind.ORDINAL_SEP;
    }

    private boolean isRangeSeparator() {
      return kind == Kind.RANGE_SEP;
    }

    private boolean isStepSeparator() {
      return kind == Kind.STEP_SEP;
    }

    private boolean isWeekdayOrdinal() {
      return kind == Kind.WEEKDAY_ORD;
    }

    private boolean isWorkday() {
      return kind == Kind.WORK;
    }
  }

  private static final String STRING_LAST = "LAST";

  private static final List<String> MONTHS = Lists.newArrayList(
      "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
      "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER");

  private static final List<String> WEEKDAYS = Lists.newArrayList("MONDAY", "TUESDAY",
      "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

  private static final List<String> WEEKDAY_ORDINALS =
      Lists.newArrayList("1ST", "2ND", "3RD", "4TH", "5TH");

  private static final int MIN_YEAR = 2000;
  private static final int MAX_YEAR = 2099;

  private static final char CHAR_ALL = '*';
  private static final char CHAR_BLANK = '?';
  private static final char CHAR_LAST = 'L';
  private static final char CHAR_WORKDAY = 'W';

  private static final char ITEM_SEPARATOR = ',';
  private static final char RANGE_SEPARATOR = '-';
  private static final char STEP_SEPARATOR = '/';
  private static final char ORDINAL_SEPARATOR = '#';

  private static final Splitter itemSplitter =
      Splitter.on(ITEM_SEPARATOR).omitEmptyStrings().trimResults();

  private static BeeLogger logger = LogUtils.getLogger(CronExpression.class);

  private static Integer parseMonth(String input) {
    return parseNames(input, MONTHS, 1);
  }

  private static Integer parseNames(String input, List<String> names, int incr) {
    Integer value = null;

    for (int i = 0; i < names.size(); i++) {
      if (names.get(i).startsWith(input)) {
        if (value == null) {
          value = i + incr;
        } else {
          value = null;
          break;
        }
      }
    }

    return value;
  }

  private static Integer parseWeekday(String input) {
    return parseNames(input, WEEKDAYS, 1);
  }

  private static List<Token> tokenize(String s) {
    List<Token> tokens = Lists.newArrayList();

    int i = 0;
    while (i < s.length()) {
      char ch = s.charAt(i);

      if (BeeUtils.isWhitespace(ch)) {
        i++;
        continue;
      }

      Token token = null;

      switch (ch) {
        case CHAR_ALL:
          token = new Token(Token.Kind.ALL);
          break;

        case RANGE_SEPARATOR:
          token = new Token(Token.Kind.RANGE_SEP);
          break;

        case STEP_SEPARATOR:
          token = new Token(Token.Kind.STEP_SEP);
          break;

        case ORDINAL_SEPARATOR:
          token = new Token(Token.Kind.ORDINAL_SEP);
          break;
      }

      if (token == null) {
        for (String wo : WEEKDAY_ORDINALS) {
          if (s.startsWith(wo, i)) {
            token = new Token(Token.Kind.WEEKDAY_ORD, wo);
            i += wo.length() - 1;
            break;
          }
        }
      }

      if (token == null) {
        if (BeeUtils.isDigit(ch)) {
          StringBuilder sb = new StringBuilder(ch);
          while (i < sb.length() - 1 && BeeUtils.isDigit(s.charAt(i + 1))) {
            sb.append(s.charAt(i + 1));
            i++;
          }

          token = new Token(Token.Kind.INT, sb.toString());

        } else if (Ascii.isUpperCase(ch)) {
          StringBuilder sb = new StringBuilder(ch);
          while (i < sb.length() - 1 && Ascii.isUpperCase(s.charAt(i + 1))) {
            sb.append(s.charAt(i + 1));
            i++;
          }

          String value = sb.toString();

          if (value.equals(STRING_LAST) || value.equals(String.valueOf(CHAR_LAST))) {
            token = new Token(Token.Kind.LAST);

          } else if (value.equals(String.valueOf(CHAR_WORKDAY))) {
            token = new Token(Token.Kind.WORK);

          } else if (value.equals(STRING_LAST + CHAR_WORKDAY)
              || value.equals(String.valueOf(CHAR_LAST) + CHAR_WORKDAY)
              || value.equals(String.valueOf(CHAR_WORKDAY) + STRING_LAST)
              || value.equals(String.valueOf(CHAR_WORKDAY) + CHAR_LAST)) {
            token = new Token(Token.Kind.LAST_WORK);

          } else {
            token = new Token(Token.Kind.ALPHA, value);
          }

        } else {
          token = new Token(Token.Kind.UNKNOWN, String.valueOf(ch));
          logger.warning("character not recognized", ch, BeeUtils.bracket(ch));
        }
      }

      tokens.add(token);
      i++;
    }

    return tokens;
  }

  @SuppressWarnings("unused")
  private final JustDate base;

  private final Set<Integer> seconds = Sets.newHashSet();
  private final Set<Integer> minutes = Sets.newHashSet();
  private final Set<Integer> hours = Sets.newHashSet();

  private final Set<Integer> daysOfMonth = Sets.newHashSet();
  private final Set<Integer> daySteps = Sets.newHashSet();

  private final Set<Integer> months = Sets.newHashSet();
  private final Set<Integer> monthSteps = Sets.newHashSet();

  private final Set<Integer> daysOfWeek = Sets.newHashSet();
  private final Multimap<Integer, Integer> weekdayOrdinals = HashMultimap.create();
  private final Multimap<Integer, Integer> weekSteps = HashMultimap.create();

  private final Set<Integer> lastWeekdays = Sets.newHashSet();

  @SuppressWarnings("unused")
  private boolean lastWorkday;
  private final Set<Integer> nearestWorkdays = Sets.newHashSet();

  private final Set<Integer> years = Sets.newHashSet();

  private final WorkdayTransition workdayTransition;

  private final Set<DateRange> exclude = Sets.newHashSet();
  private final Set<DateRange> include = Sets.newHashSet();

  private CronExpression(JustDate base, WorkdayTransition workdayTransition) {
    this.base = base;
    this.workdayTransition = workdayTransition;
  }

  private void addDayOfMonth(int dom) {
    this.daysOfMonth.add(dom);
  }

  private void addDaysOfMonth(Set<Integer> values) {
    this.daysOfMonth.addAll(values);
  }

  private void addDaysOfWeek(Set<Integer> values) {
    this.daysOfWeek.addAll(values);
  }

  private void addDayStep(int step) {
    this.daySteps.add(step);
  }

  private void addHours(Set<Integer> values) {
    this.hours.addAll(values);
  }

  private void addLastWeekday(int weekday) {
    this.lastWeekdays.add(weekday);
  }

  private void addMinutes(Set<Integer> values) {
    this.minutes.addAll(values);
  }

  private void addMonths(Set<Integer> values) {
    this.months.addAll(values);
  }

  private void addMonthStep(int step) {
    this.monthSteps.add(step);
  }

  private void addNearestWorkday(int dom) {
    if (workdayTransition == WorkdayTransition.NEAREST) {
      addDayOfMonth(dom);
    } else {
      this.nearestWorkdays.add(dom);
    }
  }

  private void addSeconds(Set<Integer> values) {
    this.seconds.addAll(values);
  }

  private void addWeekdayOrdinal(int weekday, int ordinal) {
    this.weekdayOrdinals.put(weekday, ordinal);
  }

  private void addWeekStep(int weekday, int step) {
    this.weekSteps.put(weekday, step);
  }

  private void addYears(Set<Integer> values) {
    this.years.addAll(values);
  }

  private void setExclude(Set<DateRange> exclude) {
    this.exclude.addAll(exclude);
  }

  private void setInclude(Set<DateRange> include) {
    this.include.addAll(include);
  }

  private void setLastWorkday(boolean lastWorkday) {
    this.lastWorkday = lastWorkday;
  }
}
