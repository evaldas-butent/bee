package com.butent.bee.client.ui;

import com.butent.bee.client.utils.Evaluator;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.ui.ConditionalStyleDeclaration;

public class ConditionalStyle {
  
  public static ConditionalStyle create(ConditionalStyleDeclaration declaration) {
    if (declaration == null || !declaration.validState()) {
      return null;
    }
    return new ConditionalStyle(StyleDescriptor.copyOf(declaration.getStyle()),
        Evaluator.<BooleanValue>create(declaration.getCondition()));
  }
  
  private final StyleDescriptor styleDescriptor;
  private final Evaluator<BooleanValue> evaluator;

  private ConditionalStyle(StyleDescriptor styleDescriptor, Evaluator<BooleanValue> evaluator) {
    this.styleDescriptor = styleDescriptor;
    this.evaluator = evaluator;
  }

  private StyleDescriptor getStyleDescriptor() {
    return styleDescriptor;
  }

  private Evaluator<BooleanValue> getEvaluator() {
    return evaluator;
  }
}
