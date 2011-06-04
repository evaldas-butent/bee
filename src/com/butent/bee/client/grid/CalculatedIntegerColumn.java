package com.butent.bee.client.grid;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.value.IntegerValue;

import java.util.List;

public class CalculatedIntegerColumn extends AbstractCalculatedColumn<IntegerValue> {

  public CalculatedIntegerColumn(String columnId,
      Evaluator<IntegerValue> evaluator, List<? extends IsColumn> dataColumns) {
    super(new ValueCell<IntegerValue>(), columnId, evaluator, dataColumns);
  }

}
