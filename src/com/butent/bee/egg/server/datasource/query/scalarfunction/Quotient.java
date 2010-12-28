package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.NumberValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;

import java.util.List;

public class Quotient implements ScalarFunction {
  private static final String FUNCTION_NAME = "quotient";
  private static final Quotient INSTANCE = new Quotient();

  public static Quotient getInstance() {
    return INSTANCE;
  }

  private Quotient() {
  }

  public Value evaluate(List<Value> values) {
    if (values.get(0).isNull() || values.get(1).isNull()
        || (((NumberValue) values.get(1)).getValue() == 0)) {
      return NumberValue.getNullValue();
    }
    double quotient = ((NumberValue) values.get(0)).getValue() /
        ((NumberValue) values.get(1)).getValue();
    return new NumberValue(quotient);
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return "(" + argumentsQueryStrings.get(0) + " / " + argumentsQueryStrings.get(1) + ")"; 
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
