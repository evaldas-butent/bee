package com.butent.bee.shared.time;

import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.HasCaption;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public final class CronExpression {

  public static class Builder {

    private enum Field implements HasCaption {
      SECONDS("seconds", 0, 59, true, true, false, false),
      MINUTES("minutes", 0, 59, true, true, false, false),
      HOURS("hours", 0, 23, true, true, false, false),
      DAY_OF_MONTH("day of month", 1, 31, true, true, true, true),
      MONTH("month", 1, 12, true, true, false, true),
      DAY_OF_WEEK("day of weak", 1, 7, true, true, true, true),
      YEAR("year", MIN_YEAR, MAX_YEAR, false, false, false, false);

      private final String caption;

      private final int min;
      private final int max;

      private final boolean allowLast;
      private final boolean allowBackward;
      private final boolean allowOrdinal;

      private final boolean allowAbsoluteStep;

      private Field(String caption, int min, int max, boolean allowLast, boolean allowBackward,
          boolean allowOrdinal, boolean allowAbsoluteStep) {

        this.caption = caption;

        this.min = min;
        this.max = max;

        this.allowLast = allowLast;
        this.allowBackward = allowBackward;
        this.allowOrdinal = allowOrdinal;
        this.allowAbsoluteStep = allowAbsoluteStep;
      }

      @Override
      public String getCaption() {
        return caption;
      }

      private boolean accepts(int value) {
        if (value >= 0) {
          return value >= min && value <= max;
        } else if (allowBackward) {
          return value >= -max && value <= -min;
        } else {
          return false;
        }
      }
    }

    private static final class Item {

      private static Item absoluteStep(int step) {
        return new Item(null, null, step);
      }

      private static Item incrementalRange(int lower, int upper, int step) {
        return new Item(lower, upper, step);
      }

      private static Item last() {
        return new Item(null, null, null, null, true, false);
      }

      private static Item last(int value) {
        return new Item(value, null, null, null, true, false);
      }

      private static Item lastWorkday() {
        return new Item(null, null, null, null, true, true);
      }

      private static Item of(int value) {
        return new Item(value, null);
      }

      private static Item range(int lower, int upper) {
        return new Item(lower, upper);
      }

      private static Item workday(int value) {
        return new Item(value, null, null, null, false, true);
      }

      private final Integer lower;
      private final Integer upper;

      private final Integer step;
      private final Integer ordinal;

      private final boolean last;
      private final boolean workday;

      private Item(Integer lower, Integer upper) {
        this(lower, upper, null);
      }

      private Item(Integer lower, Integer upper, Integer step) {
        this(lower, upper, step, null);
      }

      private Item(Integer lower, Integer upper, Integer step, Integer ordinal) {
        this(lower, upper, step, ordinal, false, false);
      }

      private Item(Integer lower, Integer upper, Integer step, Integer ordinal, boolean last,
          boolean workday) {

        this.lower = lower;
        this.upper = upper;

        this.step = step;
        this.ordinal = ordinal;

        this.last = last;
        this.workday = workday;
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

      private boolean isSimple() {
        return lower != null && !last && !workday;
      }
    }

    private static final String LAST = "LAST";

    private static final List<String> ORDINAL_NUMBERS =
        Lists.newArrayList("1ST", "2ND", "3RD", "4TH", "5TH");

    private static final List<String> MONTHS = Lists.newArrayList(
        "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
        "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER");

    private static final List<String> WEEKDAYS = Lists.newArrayList("MONDAY", "TUESDAY",
        "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

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

    private static String append(String value, String input) {
      if (BeeUtils.isEmpty(value)) {
        return BeeUtils.isEmpty(input) ? null : normalize(input);
      } else if (BeeUtils.isEmpty(input)) {
        return value;
      } else {
        return value + String.valueOf(ITEM_SEPARATOR) + normalize(input);
      }
    }

    private static Set<Integer> getSimpleValues(Collection<Item> items) {
      Set<Integer> values = Sets.newHashSet();

      for (Item item : items) {
        Set<Integer> itemValues = item.getSimpleValues();
        if (!itemValues.isEmpty()) {
          values.addAll(itemValues);
        }
      }

      return values;
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
        Boolean ok = null;

        char firstChar = s.charAt(0);

        if (s.length() == 1) {
          switch (firstChar) {
            case CHAR_ALL:
              item = Item.range(field.min, field.max);
              break;

            case CHAR_BLANK:
              break;

            case CHAR_LAST:
              if (field.allowLast) {
                item = Item.last();
              } else {
                ok = false;
              }
              break;

            default:
              if (BeeUtils.isDigit(firstChar) && field.accepts(firstChar)) {
                item = Item.of(firstChar);
              } else {
                ok = false;
              }
          }
        }

        if (item != null) {
          items.add(item);
        } else if (BeeUtils.isFalse(ok)) {
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
      if (!items.isEmpty()) {
        Set<Integer> values = getSimpleValues(items);
        if (!values.isEmpty()) {
          result.setSeconds(values);
        }
      }

      items = parse(Field.MINUTES, minutes);
      if (!items.isEmpty()) {
        Set<Integer> values = getSimpleValues(items);
        if (!values.isEmpty()) {
          result.setMinutes(values);
        }
      }

      items = parse(Field.HOURS, hours);
      if (!items.isEmpty()) {
        Set<Integer> values = getSimpleValues(items);
        if (!values.isEmpty()) {
          result.setHours(values);
        }
      }

      items = parse(Field.DAY_OF_MONTH, dayOfMonth);
      if (!items.isEmpty()) {
        Set<Integer> values = getSimpleValues(items);
        if (!values.isEmpty()) {
          Set<Integer> forward = Sets.newHashSet();
          Set<Integer> backward = Sets.newHashSet();

          for (int value : values) {
            if (value < 0) {
              backward.add(-value);
            } else {
              forward.add(value);
            }
          }

          if (!forward.isEmpty()) {
            result.setDomForward(forward);
          }
          if (!backward.isEmpty()) {
            result.setDomBackward(backward);
          }
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

  public enum WorkdayTransition {
    NONE, NEAREST, FORWARD, BACKWARD;

    private static WorkdayTransition DEFAULT = NEAREST;
  }

  private static BeeLogger logger = LogUtils.getLogger(CronExpression.class);

  private final JustDate base;

  private final Set<Integer> seconds = Sets.newHashSet();
  private final Set<Integer> minutes = Sets.newHashSet();
  private final Set<Integer> hours = Sets.newHashSet();

  private final Set<Integer> domForward = Sets.newHashSet();
  private final Set<Integer> domBackward = Sets.newHashSet();
  private final Set<Integer> daySteps = Sets.newHashSet();

  private final Set<Integer> months = Sets.newHashSet();
  private final Set<Integer> monthSteps = Sets.newHashSet();

  private final Set<Integer> daysOfWeek = Sets.newHashSet();
  private final Multimap<Integer, Integer> weekOrdinals = HashMultimap.create();
  private final Multimap<Integer, Integer> weekSteps = HashMultimap.create();

  private final Set<Integer> lastWeek = Sets.newHashSet();
  private boolean lastWorkday;

  private final Set<Integer> years = Sets.newHashSet();

  private final WorkdayTransition workdayTransition;

  private final Set<DateRange> exclude = Sets.newHashSet();
  private final Set<DateRange> include = Sets.newHashSet();

  private CronExpression(JustDate base, WorkdayTransition workdayTransition) {
    this.base = base;
    this.workdayTransition = workdayTransition;
  }

  private void setDaysOfWeek(Set<Integer> daysOfWeek) {
    this.daysOfWeek.addAll(daysOfWeek);
  }

  private void setDaySteps(Set<Integer> daySteps) {
    this.daySteps.addAll(daySteps);
  }

  private void setDomBackward(Set<Integer> domBackward) {
    this.domBackward.addAll(domBackward);
  }

  private void setDomForward(Set<Integer> domForward) {
    this.domForward.addAll(domForward);
  }

  private void setExclude(Set<DateRange> exclude) {
    this.exclude.addAll(exclude);
  }

  private void setHours(Set<Integer> hours) {
    this.hours.addAll(hours);
  }

  private void setInclude(Set<DateRange> include) {
    this.include.addAll(include);
  }

  private void setLastWeek(Set<Integer> lastWeek) {
    this.lastWeek.addAll(lastWeek);
  }

  private void setLastWorkday(boolean lastWorkday) {
    this.lastWorkday = lastWorkday;
  }

  private void setMinutes(Set<Integer> minutes) {
    this.minutes.addAll(minutes);
  }

  private void setMonths(Set<Integer> months) {
    this.months.addAll(months);
  }

  private void setMonthSteps(Set<Integer> monthSteps) {
    this.monthSteps.addAll(monthSteps);
  }

  private void setSeconds(Set<Integer> seconds) {
    this.seconds.addAll(seconds);
  }

  private void setWeekOrdinals(Multimap<Integer, Integer> weekOrdinals) {
    this.weekOrdinals.putAll(weekOrdinals);
  }

  private void setWeekSteps(Multimap<Integer, Integer> weekSteps) {
    this.weekSteps.putAll(weekSteps);
  }

  private void setYears(Set<Integer> years) {
    this.years.addAll(years);
  }
}
