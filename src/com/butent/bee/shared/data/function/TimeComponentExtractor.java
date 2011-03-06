package com.butent.bee.shared.data.function;

import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.InvalidQueryException;
import com.butent.bee.shared.data.value.DateTimeValue;
import com.butent.bee.shared.data.value.DateValue;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TimeOfDayValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;

import java.util.List;
import java.util.Map;

public class TimeComponentExtractor implements ScalarFunction {
  public static enum TimeComponent {
    YEAR("year"),
    MONTH("month"),
    WEEK("week"),
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    SECOND("second"),
    MILLISECOND("millisecond"),
    QUARTER("quarter"),
    DAY_OF_WEEK("dayofweek");

    private String name;

    private TimeComponent(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private static Map<TimeComponent, TimeComponentExtractor> timeComponentsPool;

  static {
    timeComponentsPool = Maps.newHashMap();
    for (TimeComponent component : TimeComponent.values()) {
      timeComponentsPool.put(component, new TimeComponentExtractor(component));
    }
  }

  public static TimeComponentExtractor getInstance(TimeComponent timeComponent) {
    return timeComponentsPool.get(timeComponent);
  }

  private TimeComponent timeComponent;

  private TimeComponentExtractor(TimeComponent timeComponent) {
    this.timeComponent = timeComponent;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TimeComponentExtractor) {
      TimeComponentExtractor other = (TimeComponentExtractor) o;
      return timeComponent.equals(other.timeComponent);
    }
    return false;
  }

  public Value evaluate(List<Value> values) {
    Value value = values.get(0);
    ValueType valueType = value.getType();
    int component;

    if (value.isNull()) {
      return NumberValue.getNullValue();
    }

    switch(timeComponent) {
      case YEAR:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getYear();
        } else {
          component = ((DateTimeValue) value).getYear();
        }
        break;
      case MONTH:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getMonth();
        } else {
          component = ((DateTimeValue) value).getMonth();
        }
        break;
      case DAY:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getDayOfMonth();
        } else {
          component = ((DateTimeValue) value).getDayOfMonth();
        }
        break;
      case HOUR:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getHours();
        } else {
          component = ((DateTimeValue) value).getHourOfDay();
        }
        break;
      case MINUTE:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getMinutes();
        } else {
          component = ((DateTimeValue) value).getMinute();
        }
        break;
      case SECOND:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getSeconds();
        } else {
          component = ((DateTimeValue) value).getSecond();
        }
        break;
      case MILLISECOND:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getMilliseconds();
        } else {
          component = ((DateTimeValue) value).getMillisecond();
        }
        break;
      case QUARTER:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getMonth();
        } else {
          component = ((DateTimeValue) value).getMonth();
        }
        component = component / 3 + 1; 
        break;
      case WEEK:
      case DAY_OF_WEEK:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getObjectValue().getDow();
        } else {
          component = ((DateTimeValue) value).getObjectValue().getDow();
        }
        break;
      default:
        Assert.untouchable("An invalid time component.");
        component = 0;
    }

    return new NumberValue(component);
  }

  public String getFunctionName() {
    return timeComponent.getName();
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  @Override
  public int hashCode() {
    return (timeComponent == null) ? 0 : timeComponent.hashCode();
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return getFunctionName() + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException("Number of parameters for "
          + timeComponent.getName() + "function is wrong: " + types.size());
    }
    switch (timeComponent) {
      case YEAR:
      case MONTH:
      case WEEK:
      case DAY:
      case QUARTER:
      case DAY_OF_WEEK:
        if ((types.get(0) != ValueType.DATE)
            && (types.get(0) != ValueType.DATETIME)) {
          throw new InvalidQueryException("Can't perform the function "
              + timeComponent.getName() + " on a column that is not a Date or"
              + " a DateTime column");
        }
        break;
      case HOUR:
      case MINUTE:
      case SECOND:
      case MILLISECOND:
        if ((types.get(0) != ValueType.TIMEOFDAY)
            && (types.get(0) != ValueType.DATETIME)) {
          throw new InvalidQueryException("Can't perform the function "
              + timeComponent.getName() + " on a column that is not a "
              + "TimeOfDay or a DateTime column");
        }
        break;
    }
  }
}
