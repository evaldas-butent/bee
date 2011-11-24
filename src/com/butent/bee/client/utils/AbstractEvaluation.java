package com.butent.bee.client.utils;

public abstract class AbstractEvaluation implements Evaluator.Evaluation {

  public abstract String eval(Evaluator.Parameters parameters);

  public void setOptions(String options) {
  }
}
