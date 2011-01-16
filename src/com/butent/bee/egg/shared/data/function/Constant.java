package com.butent.bee.egg.shared.data.function;

import com.butent.bee.egg.shared.data.InvalidQueryException;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

import java.util.List;

public class Constant implements ScalarFunction {
  private Value value;

  public Constant(Value value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Constant) {
      Constant other = (Constant) o;
      return value.equals(other.value);
    }
    return false;
  }

  public Value evaluate(List<Value> values) {
    return value;
  }

  public String getFunctionName() {
    return value.toQueryString();
  }

  public ValueType getReturnType(List<ValueType> types) {
    return value.getType();
  }

  @Override
  public int hashCode() {
    return (value == null) ? 0 : value.hashCode(); 
  }

  public String toQueryString(List<String> argumentsQueryStrings) {
    return value.toQueryString(); 
  }
  
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 0) {
      throw new InvalidQueryException("The constant function should not get "
          + "any parameters");
    }
  }
}
