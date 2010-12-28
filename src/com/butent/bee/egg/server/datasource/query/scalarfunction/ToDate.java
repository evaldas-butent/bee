package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.DateTimeValue;
import com.butent.bee.egg.server.datasource.datatable.value.DateValue;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;
import com.butent.bee.egg.shared.Assert;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.util.Date;
import java.util.List;

public class ToDate implements ScalarFunction {
  private static final String FUNCTION_NAME = "toDate";
  private static final ToDate INSTANCE = new ToDate();

  public static ToDate getInstance() {
    return INSTANCE;
  }

  private ToDate() {
  }

  public Value evaluate(List<Value> values) {
    Value value = values.get(0);
    Date date;
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    if (value.isNull()) {
      return DateValue.getNullValue();
    }
    DateValue dateValue;
    switch(value.getType()) {
      case DATE:
        dateValue = (DateValue) value;
        break;
      case DATETIME:
        dateValue = new DateValue((GregorianCalendar)
            (((DateTimeValue) value).getObjectToFormat()));
        break;
      case NUMBER:
        date = new Date((long) ((NumberValue) value).getValue());
        gc.setTime(date);
        dateValue = new DateValue(gc);
        break;
      default:
        Assert.untouchable("Value type was not found: " + value.getType());
        dateValue = null;
    }
    return dateValue;
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.DATE;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException("Number of parameters for the date "
          + "function is wrong: " + types.size());
    } else if ((types.get(0) != ValueType.DATETIME)
        && (types.get(0) != ValueType.DATE)
        && (types.get(0) != ValueType.NUMBER)) {
      throw new InvalidQueryException("Can't perform the function 'date' "
          + "on values that are not date, dateTime or number values");
    }
  }
}
