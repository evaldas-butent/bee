package com.butent.bee.shared.data.column;

import com.butent.bee.shared.data.Calculation;
import com.butent.bee.shared.data.TableColumn;
import com.butent.bee.shared.data.value.ValueType;

public class CalculatedColumn extends TableColumn {
  private Calculation calc; 

  private CalculatedColumn(String id, ValueType type, Calculation calc) {
    super(id, type);
    setCalc(calc);
  }

  public CalculatedColumn(String id, ValueType type, String label, Calculation calc) {
    super(id, type, label);
    setCalc(calc);
  }

  public Calculation getCalc() {
    return calc;
  }

  public void setCalc(Calculation calc) {
    this.calc = calc;
  }
}
