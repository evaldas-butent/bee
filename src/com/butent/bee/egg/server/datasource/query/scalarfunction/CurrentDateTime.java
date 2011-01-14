package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.data.value.DateTimeValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public class CurrentDateTime implements ScalarFunction {
  private static final String FUNCTION_NAME = "now";
  private static final CurrentDateTime INSTANCE = new CurrentDateTime();

  public static CurrentDateTime getInstance() {
    return INSTANCE;
  }

  private CurrentDateTime() {
  }

  public Value evaluate(List<Value> values) {
    return new DateTimeValue(new BeeDate());
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.DATETIME;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "()"; 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 0) {
      throw new InvalidQueryException("The " + FUNCTION_NAME + " function should not get "
          + "any parameters");
    }
  }
}
