package com.butent.bee.egg.shared.data.function;

import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public interface ScalarFunction {
  Value evaluate(List<Value> values);

  String getFunctionName();

  ValueType getReturnType(List<ValueType> types);
  
  String toQueryString(List<String> argumentQueryStrings);
  
  void validateParameters(List<ValueType> types) throws InvalidQueryException;
}
