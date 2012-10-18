package com.butent.bee.client.dialog;

public abstract class DecisionCallback {

  public DecisionCallback() {
    super();
  }

  public void onCancel() {
  }

  public abstract void onConfirm();

  public void onDeny() {
  }
}
