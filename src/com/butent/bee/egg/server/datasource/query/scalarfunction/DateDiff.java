package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.DateTimeValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateValue;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.util.Date;
import java.util.List;

public class DateDiff implements ScalarFunction {
  private static final String FUNCTION_NAME = "dateDiff";
  private static final DateDiff INSTANCE = new DateDiff();

  public static DateDiff getInstance() {
    return INSTANCE;
  }

  private DateDiff() {
  }

  public Value evaluate(List<Value> values) {
    Value firstValue = values.get(0);
    Value secondValue = values.get(1);

    if (firstValue.isNull() || secondValue.isNull()) {
      return NumberValue.getNullValue();
    }
    Date firstDate = getDateFromValue(firstValue);
    Date secondDate = getDateFromValue(secondValue);

    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    calendar.setTime(secondDate);
    return new NumberValue(calendar.fieldDifference(firstDate, Calendar.DATE));
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ", " + argumentsQueryStrings.get(1)
        + ")";
  }

  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("Number of parameters for the dateDiff "
          + "function is wrong: " + types.size());
    } else if ((!isDateOrDateTimeValue(types.get(0)))
        || (!isDateOrDateTimeValue(types.get(1)))) {
      throw new InvalidQueryException("Can't perform the function 'dateDiff' "
          + "on values that are not a Date or a DateTime values");
    }
  }

  private Date getDateFromValue(Value value) {
    Calendar calendar;
    if (value.getType() == ValueType.DATE) {
      calendar = ((DateValue) value).getObjectToFormat();
    } else {
      calendar = ((DateTimeValue) value).getObjectToFormat();
    }
    return calendar.getTime();
  }
  
  private boolean isDateOrDateTimeValue(ValueType type) {
    return ((type == ValueType.DATE) || (type == ValueType.DATETIME));
  }
}
