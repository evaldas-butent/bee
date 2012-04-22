package com.butent.bee.client.render;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.utils.NameUtils;

public class EvalRenderer extends AbstractCellRenderer {

  private final Evaluator evaluator;
  private final boolean hasColumn;
  
  public EvalRenderer(int dataIndex, IsColumn dataColumn, Evaluator evaluator) {
    super(dataIndex, dataColumn);

    Assert.notNull(evaluator, NameUtils.getName(this) + ": evaluator is required");
    this.evaluator = evaluator;
    hasColumn = dataIndex >= 0 && dataColumn != null; 
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    
    if (hasColumn) {
      evaluator.update(row, getDataType(), getString(row));
    } else {
      evaluator.update(row);
    }

    return evaluator.evaluate();
  }
}
