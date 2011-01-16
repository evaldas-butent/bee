package com.butent.bee.egg.shared.data.function;

import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public class Difference implements ScalarFunction {
  private static final String FUNCTION_NAME = "difference";
  private static final Difference INSTANCE = new Difference();

  public static Difference getInstance() {
    return INSTANCE;
  }

  private Difference() {
  }

  public Value evaluate(List<Value> values) {
    if (values.get(0).isNull() || values.get(1).isNull()) {
      return NumberValue.getNullValue();
    }
    double difference = ((NumberValue) values.get(0)).getValue() -
        ((NumberValue) values.get(1)).getValue();
    return new NumberValue(difference);
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return "(" + argumentsQueryStrings.get(0) + " - " + argumentsQueryStrings.get(1) + ")"; 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("The function " + FUNCTION_NAME
          + " requires 2 parmaeters ");
    }
    for (ValueType type : types) {
      if (type != ValueType.NUMBER) {
        throw new InvalidQueryException("Can't perform the function "
            + FUNCTION_NAME + " on values that are not numbers");
      }
    }
  }
}
