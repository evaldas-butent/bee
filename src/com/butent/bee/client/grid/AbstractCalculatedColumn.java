package com.butent.bee.client.grid;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.value.Value;

import java.util.List;

public abstract class AbstractCalculatedColumn<C extends Value> extends Column<IsRow, C> {
  
  private final Evaluator<C> evaluator;

  public AbstractCalculatedColumn(Cell<C> cell, String columnId, Evaluator<C> evaluator,
      List<? extends IsColumn> dataColumns) {
    super(cell);
    this.evaluator = evaluator;
    this.evaluator.init(columnId, dataColumns);
  }

  @Override
  public C getValue(IsRow object) {
    return null;
  }

  @Override
  public void render(Context context, IsRow rowValue, SafeHtmlBuilder sb) {
    if (rowValue == null) {
      return;
    }
    if (context == null) {
      getEvaluator().update(rowValue);
    } else {
      getEvaluator().update(rowValue, context.getIndex(), context.getColumn());
    }
    
    C value = getEvaluator().evaluate();
    if (value != null) {
      sb.appendEscaped(value.getString());
///      getCell().render(context, value, sb);
    }
  }

  private Evaluator<C> getEvaluator() {
    return evaluator;
  }
}
