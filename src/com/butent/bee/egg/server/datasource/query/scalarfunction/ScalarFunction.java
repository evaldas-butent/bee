package com.butent.bee.egg.server.datasource.query.scalarfunction;

import com.butent.bee.egg.server.datasource.base.InvalidQueryException;
import com.butent.bee.egg.server.datasource.datatable.value.Value;
import com.butent.bee.egg.server.datasource.datatable.value.ValueType;

import java.util.List;

public interface ScalarFunction {
  Value evaluate(List<Value> values);

  String getFunctionName();

  ValueType getReturnType(List<ValueType> types);
  
  String toQueryString(List<String> argumentQueryStrings);
  
  void validateParameters(List<ValueType> types) throws InvalidQueryException;
}
