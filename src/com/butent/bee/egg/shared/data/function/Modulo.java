package com.butent.bee.egg.shared.data.function;

import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public class Modulo implements ScalarFunction {
  private static final String FUNCTION_NAME = "modulo";
  private static final Modulo INSTANCE = new Modulo();

  public static Modulo getInstance() {
    return INSTANCE;
  }

  private Modulo() {
  }

  public Value evaluate(List<Value> values) {
    if (values.get(0).isNull() || values.get(1).isNull()) {
      return NumberValue.getNullValue();
    }
    double modulo = ((NumberValue) values.get(0)).getValue() %
        ((NumberValue) values.get(1)).getValue();
    return new NumberValue(modulo);
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return "(" + argumentsQueryStrings.get(0) + " % " + argumentsQueryStrings.get(1) + ")"; 
  }

  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("The function " + FUNCTION_NAME + " requires 2 parmaeters ");
    }
    for (ValueType type : types) {
      if (type != ValueType.NUMBER) {
        throw new InvalidQueryException("Can't perform the function "
            + FUNCTION_NAME + " on values that are not numbers");
      }
    }
  }
}