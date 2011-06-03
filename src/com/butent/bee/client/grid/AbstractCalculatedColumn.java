package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;

public abstract class AbstractCalculatedColumn<C extends Value> extends Column<IsRow, C> {
  
  private final Evaluator<C> evaluator;

  public AbstractCalculatedColumn(Cell<C> cell, Evaluator<C> evaluator) {
    super(cell);
    this.evaluator = evaluator;
  }

  @Override
  public C getValue(IsRow row) {
    if (row == null) {
      return null;
    }
    return getEvaluator().eval(row);
  }

  private Evaluator<C> getEvaluator() {
    return evaluator;
  }
}
