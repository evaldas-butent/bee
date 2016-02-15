package com.butent.bee.client.dialog;

@FunctionalInterface
public interface DecisionCallback extends ConfirmationCallback {
  default void onDeny() {
  }
}
