package com.butent.bee.client.render;

import com.butent.bee.client.utils.Evaluator.Evaluation;
import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.client.utils.HasEvaluation;
import com.butent.bee.client.utils.JreEmulation;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;

public class EvalRenderer extends AbstractCellRenderer implements HasEvaluation {

  private final Evaluator evaluator;
  
  public EvalRenderer(int dataIndex, IsColumn dataColumn, Evaluator evaluator) {
    super(dataIndex, dataColumn);

    Assert.notNull(evaluator, JreEmulation.getSimpleName(this) + ": evaluator is required");
    this.evaluator = evaluator;
  }

  @Override
  public String render(IsRow row) {
    if (row == null) {
      return null;
    }
    evaluator.update(row);
    return evaluator.evaluate();
  }

  public void setEvaluation(Evaluation evaluation) {
    evaluator.setEvaluation(evaluation);
  }
}
