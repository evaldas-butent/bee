package com.butent.bee.shared.data.column;

import com.butent.bee.shared.data.Calculation;
import com.butent.bee.shared.data.TableColumn;
import com.butent.bee.shared.data.value.ValueType;

public class CalculatedColumn extends TableColumn {
  private Calculation calc; 

  public CalculatedColumn(ValueType type, String label, String id, Calculation calc) {
    super(type, label, id);
    setCalc(calc);
  }

  public Calculation getCalc() {
    return calc;
  }

  public void setCalc(Calculation calc) {
    this.calc = calc;
  }
}
