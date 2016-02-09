package com.butent.bee.client.dialog;

public interface DecisionCallback extends ConfirmationCallback {
  default void onDeny() {
  }
}
