package com.butent.bee.egg.server.datasource.query.engine;

import com.butent.bee.egg.server.datasource.query.AggregationType;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.data.value.NumberValue;
import com.butent.bee.egg.shared.data.value.Value;
import com.butent.bee.egg.shared.data.value.ValueType;

class ValueAggregator {
  private ValueType valueType;

  private Value max;
  private Value min;
  private double sum = 0;
  private int count = 0;

  public ValueAggregator(ValueType valueType) {
    this.valueType = valueType;
    min = max = Value.getNullValueFromValueType(valueType);
  }

  public void aggregate(Value value) {
    if (!value.isNull()) {
      count++;
      if (valueType == ValueType.NUMBER) {
        sum += ((NumberValue) value).getValue();
      }
      if (count == 1) {
        max = min = value;
      } else {
        max = max.compareTo(value) >= 0 ? max : value;
        min = min.compareTo(value) <= 0 ? min : value;
      }
    } else if (count == 0) {
      min = max = value;
    }
  }

  public Value getValue(AggregationType type) {
    Value v;
    switch (type) {
      case AVG:
        v = (count != 0) ? new NumberValue(getAverage()) : NumberValue.getNullValue();
        break;
      case COUNT:
        v = new NumberValue(count);
        break;
      case MAX:
        v = max;
        if (count == 0) {
          v = Value.getNullValueFromValueType(v.getType());
        }
        break;
      case MIN:
        v = min;
        if (count == 0) {
          v = Value.getNullValueFromValueType(v.getType());
        }
        break;
      case SUM:
        v = (count != 0) ? new NumberValue(getSum()) : NumberValue.getNullValue();
        break;
      default:
        Assert.untouchable("Invalid AggregationType");
        v = null;
    }
    return v;
  }

  private Double getAverage() {
    if (valueType != ValueType.NUMBER) {
      throw new UnsupportedOperationException();
    }
    return count > 0 ? sum / count : null;
  }

  private double getSum() {
    if (valueType != ValueType.NUMBER) {
      throw new UnsupportedOperationException();
    }
    return sum;
  }
}
