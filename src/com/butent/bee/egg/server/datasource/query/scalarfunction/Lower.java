package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.TextValue;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;

import java.util.List;

public class Lower implements ScalarFunction {
  private static final String FUNCTION_NAME = "lower";
  private static final Lower INSTANCE = new Lower();

  public static Lower getInstance() {
    return INSTANCE;
  }
  
  private Lower() {
  }

  public Value evaluate(List<Value> values) {
    return new TextValue(((TextValue) values.get(0)).getValue().toLowerCase());
  }

  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.TEXT;
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException(FUNCTION_NAME + " requires 1 parmaeter");
    }
    if (types.get(0) != ValueType.TEXT) {
      throw new InvalidQueryException(FUNCTION_NAME + " takes a text parameter");
    }
  }
}
