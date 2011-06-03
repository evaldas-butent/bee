package com.butent.bee.client.utils;

import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.ui.Calculation;

public class Evaluator<T extends Value> extends Calculation {
  
  public static <T extends Value> Evaluator<T> create(Calculation calc) {
    if (calc == null || calc.isEmpty()) {
      return null;
    }
    return new Evaluator<T>(calc.getType(), calc.getExpression(), calc.getFunction());
  }

  private Evaluator(ValueType type, String expression, String function) {
    super(type, expression, function);
  }
  
  public T eval(IsRow row) {
    if (row == null) {
      return null;
    }
    return null;
  }
}
